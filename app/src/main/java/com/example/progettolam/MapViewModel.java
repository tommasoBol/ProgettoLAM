package com.example.progettolam;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import mil.nga.grid.features.Point;
import mil.nga.mgrs.MGRS;
import mil.nga.mgrs.grid.GridType;
import mil.nga.mgrs.tile.MGRSTileProvider;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class MapViewModel extends AndroidViewModel {
    
    private MeasurementRepository mRepo;
    private MutableLiveData<GridType> chosenGridType;
    private int mapArea;
    private String mapType;
    private MutableLiveData<Integer> zoomLevel;
    private Map<String, Double> dataForMap;
    private MutableLiveData<List<PolygonOptions>> polygonOptions;
    
    
    public MapViewModel(@NonNull Application app) {
        super(app);
        mRepo = new MeasurementRepository(app);
        chosenGridType = new MutableLiveData<>();
        zoomLevel = new MutableLiveData<>(15);
        polygonOptions = new MutableLiveData<>();
    }
    public LiveData<List<Measurement>> getLiveMeasurements() {
        return mRepo.getLiveMeasurements();
    }
    
    public String[] getMeasuredNetwork() {
        return mRepo.getMeasuredNetwork();
    }
    
    public void updateChosenGridType(GridType gt) {
        chosenGridType.setValue(gt);
        if (gt.equals(GridType.TEN_METER)) mapArea=10;
        else if (gt.equals(GridType.HUNDRED_METER)) mapArea= 100;
        else mapArea = 1000;
    }
    
    public MutableLiveData<GridType> getChosenGridType() {
        return chosenGridType;
    }
    
    public Integer getMapArea() {
        return mapArea;
    }
    
    public String getMapType() {
        return mapType;
    }
    public void setMapType(String m) {
        mapType = m;
    }
    
    public void setZoomLevel(int i) {
        zoomLevel.setValue(i);
    }
    
    public MutableLiveData<Integer> getZoomLevel() {
        return zoomLevel;
    }
    
    public MutableLiveData<List<PolygonOptions>> getPolygonOptions() {
        return polygonOptions;
    }
    
    
    
    
    
    
    
    
    
    public void prepareDataForMap() {
        dataForMap = new TreeMap<>();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
        int limit = Integer.parseInt(sharedPreferences.getString("mForAverage", "1"));
        new Thread(() -> {
            try {
                List<String> zones = mRepo.getMeasuredZoneByType(mapType);
                Map<String, List<Double>> data = new TreeMap<>();
                Map<String, List<Double>> result = new TreeMap<>();
                for (String z : zones) {
                    data.put(z, mRepo.getMeasurementsForDraw(z, mapType, limit));
                }
    
                if (10 != mapArea) {
                    for (String s : data.keySet()) {
                        MGRS mgrs = MGRS.parse(s);
                        long newNorthing = Math.floorDiv(mgrs.getNorthing(), mapArea);
                        long newEasting = Math.floorDiv(mgrs.getEasting(), mapArea);
                        String newArea = mgrs.getZone() + "" + mgrs.getBand() + "" + mgrs.getColumn() + "" + mgrs.getRow() + "" + newEasting + "" + newNorthing;
                        result.computeIfAbsent(newArea, k -> new ArrayList<>()).addAll(data.get(s));
                    }
                } else
                    result = data;
                
                
                for (String s : result.keySet()) {
                    dataForMap.put(s, avg(result.get(s)));
                }
                preparePolygonOptions();
            }catch (ParseException e) {
                throw new RuntimeException();
            }
        }).start();
    }
    
    
    private double avg(List<Double> l) {
        double somma = 0;
        for (double d : l)
            somma += d;
        return somma/l.size();
    }
    
    private void preparePolygonOptions() {
        try {
            List<PolygonOptions> poList = new ArrayList<>();
            for (String s : dataForMap.keySet()) {
                MGRS current = MGRS.parse(s);
                Point currentP = current.toPoint();
                Point northP = northSquare(current);
                Point eastP = eastSquare(current);
                Point northEastP = northEastSquare(current);
                PolygonOptions po = new PolygonOptions().add(
                        new LatLng(currentP.getLatitude(), currentP.getLongitude()),
                        new LatLng(northP.getLatitude(), northP.getLongitude()),
                        new LatLng(northEastP.getLatitude(), northEastP.getLongitude()),
                        new LatLng(eastP.getLatitude(), eastP.getLongitude())
                ).fillColor(encodeResult(dataForMap.get(s)));
                poList.add(po);
            }
            polygonOptions.postValue(poList);
        } catch(ParseException e) {
            throw new RuntimeException();
        }
    }
    
    
    
    
    private Point northSquare(MGRS current) {
        long newNorthing = current.getNorthing()+mapArea;
        MGRS north = MGRS.create(current.getZone(), current.getBand(), current.getColumn(), current.getRow(), current.getEasting(), newNorthing);
        Point p = north.toPoint();
        Log.d("NORTH", north.toString());
        return p;
    }
    
    private Point eastSquare(MGRS current) {
        long newEasting = current.getEasting()+mapArea;
        MGRS east = MGRS.create(current.getZone(), current.getBand(), current.getColumn(), current.getRow(), newEasting, current.getNorthing());
        Point p = east.toPoint();
        Log.d("EAST", east.toString());
        return p;
    }
    
    private Point northEastSquare(MGRS current) {
        long newNorthing = current.getNorthing()+mapArea;
        long newEasting = current.getEasting()+mapArea;
        MGRS northEast = MGRS.create(current.getZone(), current.getBand(), current.getColumn(), current.getRow(), newEasting, newNorthing);
        Point p = northEast.toPoint();
        Log.d("NorthEast", northEast.toString());
        return p;
    }
    
    private int encodeResult(Double d) {
        if (mapType=="Noise") {
            if (d < 45) return 0x7c00ff00;
            else if (d>=45 && d<70) return 0x8eff7a00;
            else return 0xa0ff0000;
        } else if (mapType=="LTE") {
            if (d==0) return 0x2d000000;
            else if (d>0 && d<2) return 0xa0ff0000;
            else if (d>=2 && d<3) return 0x8eff7a00;
            else return 0x7c00ff00;
        } else {
            if (d > -50) return 0x7c00ff00;
            else if (d <= -50 && d>-70) return 0x8eff7a00;
            else return 0xa0ff0000;
        }
    }

}
