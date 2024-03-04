package com.example.progettolam;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.icu.util.Measure;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.Task;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

import mil.nga.grid.features.Point;
import mil.nga.mgrs.MGRS;
import mil.nga.mgrs.grid.Grid;
import mil.nga.mgrs.grid.GridType;
import mil.nga.mgrs.tile.MGRSTileProvider;

public class MapsFragment extends Fragment {
    
    private GoogleMap mMap;
    private MGRSTileProvider customTileProvider;
    
    private Button btnNoiseMap;
    private Button btnWIFIMap;
    private Button btn10;
    private Button btn100;
    private Button btn1;
    private Button btnLTEMap;
    private Button btnPrevious;
    private Button btnNext;
    private Button btnLegend;
    private TileOverlay currentTileOverlay;
    private MapViewModel mvvm;
    private List<Polygon> poligoni;
    private ActivityResultLauncher<String> requestLocationPermission;
    
    private int pIndex;
    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            if (ActivityCompat.checkSelfPermission(MapsFragment.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                MyUtils.getDeviceLocation(getContext(), (location -> {
                    LatLng lastLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, mvvm.getZoomLevel().getValue()));
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                }), (error -> {
                    LatLng bolognaLatLng = new LatLng(44.494887, 11.3426163);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bolognaLatLng, mvvm.getZoomLevel().getValue()));
                    Toast.makeText(getContext(), "Cannot get your location", Toast.LENGTH_SHORT).show();
                }));
            } else {
                requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
    
            btnLTEMap.setOnClickListener(v -> {
                showDistance();
                mvvm.setMapType("LTE");
            });
            btnNoiseMap.setOnClickListener(v -> {
                showDistance();
                mvvm.setMapType("Noise");
            });
            btnWIFIMap.setOnClickListener(v -> {
                showDevice();
                new Thread(() -> {
                    String[] measuredNetwork = mvvm.getMeasuredNetwork();
                    String[] chosenNetwork = {null};
                    getActivity().runOnUiThread(() -> {
                        new AlertDialog.Builder(getContext())
                                .setTitle("Choose the network")
                                .setPositiveButton("OK", ((dialog, which) -> {
                                    if (chosenNetwork[0]!=null && !chosenNetwork[0].equals("")) {
                                        deleteAllPolygons();
                                        mvvm.setZoomLevel(18);
                                        mvvm.updateChosenGridType(GridType.TEN_METER);
                                        mvvm.setMapType(chosenNetwork[0]);
                                        mvvm.prepareDataForMap();
                                    }
                                }))
                                .setNegativeButton("Cancel", (dialog, which) -> {})
                                .setSingleChoiceItems(measuredNetwork, -1, (dialog, which) -> chosenNetwork[0] = measuredNetwork[which])
                                .show();
                    });
                }).start();
                
            });
    
            btn10.setOnClickListener(v -> {
                pIndex = -1;
                deleteAllPolygons();
                showDevice();
                mvvm.setZoomLevel(18);
                mvvm.updateChosenGridType(GridType.TEN_METER);
                mvvm.prepareDataForMap();
            });
            btn100.setOnClickListener(v -> {
                pIndex = -1;
                deleteAllPolygons();
                showDevice();
                mvvm.setZoomLevel(16);
                mvvm.updateChosenGridType(GridType.HUNDRED_METER);
                mvvm.prepareDataForMap();
            });
            btn1.setOnClickListener(v -> {
                pIndex = -1;
                deleteAllPolygons();
                showDevice();
                mvvm.setZoomLevel(14);
                mvvm.updateChosenGridType(GridType.KILOMETER);
                mvvm.prepareDataForMap();
            });
            
            btnNext.setOnClickListener(v -> {
                pIndex++;
                pIndex = pIndex%poligoni.size();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(poligoni.get(pIndex).getPoints().get(0), mvvm.getZoomLevel().getValue()));
            });
            
            btnPrevious.setOnClickListener(v -> {
                if (pIndex<=0) pIndex=0;
                else pIndex--;
                pIndex = pIndex% poligoni.size();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(poligoni.get(pIndex).getPoints().get(0), mvvm.getZoomLevel().getValue()));
            });
            
            btnLegend.setOnClickListener(v -> showLegend());
    
            mvvm.getChosenGridType().observe(getViewLifecycleOwner(), gridType -> {
                if (currentTileOverlay != null) currentTileOverlay.remove();
                customTileProvider = MGRSTileProvider.create(getContext(), gridType);
                currentTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(customTileProvider));
            });
    
            mvvm.getZoomLevel().observe(getViewLifecycleOwner(), aFloat -> {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMap.getCameraPosition().target, aFloat));
            });
            
            mvvm.getPolygonOptions().observe(getViewLifecycleOwner(), polygonOptions -> {
                for (PolygonOptions po : polygonOptions) {
                    Polygon p = mMap.addPolygon(po);
                    poligoni.add(p);
                }
                btnNext.setVisibility(View.VISIBLE);
                btnPrevious.setVisibility(View.VISIBLE);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(poligoni.get(0).getPoints().get(0), mvvm.getZoomLevel().getValue()));
            });
    
            mvvm.getLiveMeasurements().observe(getViewLifecycleOwner(), measurements -> {
                if (mvvm.getMapArea() != -1 && mvvm.getMapType() != null)
                    mvvm.prepareDataForMap();
            });
        }
    };
    
    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        
        requestLocationPermission =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                MyUtils.getDeviceLocation(getContext(), (location -> {
                    LatLng lastLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, mvvm.getZoomLevel().getValue()));
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                }), (error -> {
                    LatLng bolognaLatLng = new LatLng(44.494887, 11.3426163);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bolognaLatLng, mvvm.getZoomLevel().getValue()));
                }));
            }else {
                LatLng bolognaLatLng = new LatLng(44.494887, 11.3426163);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bolognaLatLng, mvvm.getZoomLevel().getValue()));
            }
        });
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }
    
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    
        mvvm = new ViewModelProvider(getActivity()).get(MapViewModel.class);
        poligoni = new ArrayList<>();
        
        
        btnNoiseMap = getView().findViewById(R.id.btnNoiseMap);
        btnLTEMap = getView().findViewById(R.id.btn4GMap);
        btnWIFIMap = getView().findViewById(R.id.btnWIFIMap);
        
        btn10 = getView().findViewById(R.id.btn10);
        btn100 = getView().findViewById(R.id.btn100);
        btn1 = getView().findViewById(R.id.btn1);
        
        btnPrevious = getView().findViewById(R.id.previous);
        btnNext = getView().findViewById(R.id.next);
        
        btnLegend = getView().findViewById(R.id.btnlegend);
    }
    
    
    private void showDistance() {
        btnLTEMap.setVisibility(View.INVISIBLE);
        btnNoiseMap.setVisibility(View.INVISIBLE);
        btnWIFIMap.setVisibility(View.INVISIBLE);
    
        btn10.setVisibility(View.VISIBLE);
        btn100.setVisibility(View.VISIBLE);
        btn1.setVisibility(View.VISIBLE);
    }
    
    private void showDevice() {
        btnLTEMap.setVisibility(View.VISIBLE);
        btnNoiseMap.setVisibility(View.VISIBLE);
        btnWIFIMap.setVisibility(View.VISIBLE);
    
        btn10.setVisibility(View.INVISIBLE);
        btn100.setVisibility(View.INVISIBLE);
        btn1.setVisibility(View.INVISIBLE);
    }
    
    private void showLegend() {
        new AlertDialog.Builder(getActivity()).setTitle("Legend").setMessage("Red -> Bad signal/Loud noise\nOrange -> Discrete signal/noise\nGreen -> Good signal/silent environment\nGrey -> No LTE signal").show();
    }
    
    
    private void deleteAllPolygons() {
        poligoni.forEach(p -> p.remove());
        poligoni = new ArrayList<>();
    }
    
    
    
}