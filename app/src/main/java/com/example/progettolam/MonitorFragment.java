package com.example.progettolam;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import mil.nga.grid.features.Bounds;
import mil.nga.grid.features.Point;
import mil.nga.mgrs.MGRS;
import mil.nga.mgrs.grid.GridType;
import mil.nga.mgrs.tile.MGRSTileProvider;

public class MonitorFragment extends Fragment {
    
    private Button btnNoise;
    private Button btn4G;
    private Button btnWIFI;

    private MonitorViewModel mvvm;
    private SharedPreferences sharedPref;
    private boolean noiseFlag = true;
    private boolean LTEFlag = true;
    private LocationManager lm;
    private ActivityResultLauncher<String[]> requestForLTE;
    private ActivityResultLauncher<String[]> requestForNoise;
    private ActivityResultLauncher<String[]> requestForWifi;
    public MonitorFragment() {
        // Required empty public constructor
    }

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        requestForLTE =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
                    if (!isGranted.containsValue(false)) {
                        try {
                            Intent i = new Intent(getContext(), MeasurementService.class);
                            i.putExtra("type", "LTE");
                            getActivity().startService(i);
                        } catch(RuntimeException e) {
                            Toast.makeText(getContext(), "Problems during the measurement", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Non è possibile fare rilevazioni se non si concedono i permessi", Toast.LENGTH_SHORT).show();
                    }
                });
    
        requestForNoise =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
                    if (!isGranted.containsValue(false)) {
                        try {
                            Intent i = new Intent(getContext(), MeasurementService.class);
                            i.putExtra("type", "Noise");
                            getActivity().startService(i);
                        } catch(RuntimeException e) {
                            Toast.makeText(getContext(), "Problems during the measurement", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Non è possibile fare rilevazioni se non si concedono i permessi", Toast.LENGTH_SHORT).show();
                    }
                });
    
        requestForWifi =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
                    if (!isGranted.containsValue(false)) {
                        try {
                            chooseWiFiNetwork();
                        } catch(RuntimeException e) {
                            Toast.makeText(getContext(), "Problems during the measurement", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Non è possibile fare rilevazioni se non si concedono i permessi", Toast.LENGTH_SHORT).show();
                    }
                });
        return inflater.inflate(R.layout.fragment_monitor, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mvvm = new ViewModelProvider(getActivity()).get(MonitorViewModel.class);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        btnNoise = getView().findViewById(R.id.btnNoise);
        btn4G = getView().findViewById(R.id.btn4G);
        btnWIFI = getView().findViewById(R.id.btnWIFI);
        lm = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
        

        btnNoise.setOnClickListener(v -> {
            if (!isLocationEnabled()) {
                Toast.makeText(getContext(), "Enable location", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!noiseFlag) {
                Toast.makeText(getContext(), "Wait for the next measurement", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ActivityCompat.checkSelfPermission(MonitorFragment.this.getContext(), Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(MonitorFragment.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(MonitorFragment.this.getContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                try {
                    Intent i = new Intent(getContext(), MeasurementService.class);
                    i.putExtra("type", "Noise");
                    getActivity().startForegroundService(i);
                }catch (RuntimeException e) {
                    Toast.makeText(getContext(), "Problems during the measurement", Toast.LENGTH_SHORT).show();
                }
            } else {
                requestForNoise.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS});
            }
        });
        
        btn4G.setOnClickListener(v -> {
            if (!isLocationEnabled()) {
                Toast.makeText(getContext(), "Enable location", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!LTEFlag) {
                Toast.makeText(getContext(), "Wait for the next measurement", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ActivityCompat.checkSelfPermission(MonitorFragment.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(MonitorFragment.this.getContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(MonitorFragment.this.getContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                try {
                    Intent i = new Intent(getContext(), MeasurementService.class);
                    i.putExtra("type", "LTE");
                    getActivity().startForegroundService(i);
                }catch (RuntimeException e) {
                    Toast.makeText(getContext(), "Problems during the measurement", Toast.LENGTH_SHORT).show();
                }
            } else{
                requestForLTE.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.POST_NOTIFICATIONS});
            }
        });
        
        btnWIFI.setOnClickListener(v -> {
            if (!isLocationEnabled()) {
                Toast.makeText(getContext(), "Enable location", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ActivityCompat.checkSelfPermission(MonitorFragment.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MonitorFragment.this.getContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(MonitorFragment.this.getContext(), Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
                try {
                    chooseWiFiNetwork();
                } catch(RuntimeException e) {
                    Toast.makeText(getContext(), "Problems during the measurement", Toast.LENGTH_SHORT).show();
                }
            } else {
                requestForWifi.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.POST_NOTIFICATIONS});
            }
        });
        
        
        mvvm.getLastMeasurementByType("Noise").observe(getViewLifecycleOwner(), stringLongMap -> {
                MyUtils.getDeviceLocation(getContext(), location -> {
                    String mgrs = MyUtils.locationToMgrs(location, getContext(), GridType.TEN_METER);
                    if (stringLongMap.containsKey(mgrs)) {
                        long currentInstant = System.currentTimeMillis();
                        int delay = Integer.parseInt(sharedPref.getString("period", "0"));
                        if ((currentInstant - stringLongMap.get(mgrs)) / 60000 < delay) {
                            Log.d("BLOCKING BUTTON", "YES");
                            noiseFlag = false;
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    noiseFlag = true;
                                }
                            }, ((delay - (currentInstant - stringLongMap.get(mgrs)) / 60000)) * 60000);
                        }
                    }
                }, error -> {
                });
        });
    
        mvvm.getLastMeasurementByType("LTE").observe(getViewLifecycleOwner(), stringLongMap -> {
                MyUtils.getDeviceLocation(getContext(), location -> {
                    String mgrs = MyUtils.locationToMgrs(location, getContext(), GridType.TEN_METER);
                    if (stringLongMap.containsKey(mgrs)) {
                        long currentInstant = System.currentTimeMillis();
                        int delay = Integer.parseInt(sharedPref.getString("period", "0"));
                        if ((currentInstant - stringLongMap.get(mgrs)) / 60000 < delay) {
                            Log.d("BLOCKING BUTTON", "YES");
                            LTEFlag = false;
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    LTEFlag = true;
                                }
                            }, ((delay - (currentInstant - stringLongMap.get(mgrs)) / 60000)) * 60000);
                        }
                    }
                }, error -> {
                });
        });
        
        
    }
    
    private void chooseWiFiNetwork() {
        WIFIMonitor1 wfm = new WIFIMonitor1(getActivity().getApplication());
        List<String> ssids = wfm.scan();
        String[] ssidsArray = ssids.toArray(new String[0]);
        String[] chosenSSID = {null};
        new AlertDialog.Builder(getContext())
                .setTitle("Choose the network")
                .setPositiveButton("OK", (dialog, which) -> {
                    if (chosenSSID[0]!=null && !chosenSSID[0].equals("")) {
                        new Thread(() -> {
                            long lastMeasurement = mvvm.getLastWiFiMeasurement(chosenSSID[0]);
                            long currentInstant = System.currentTimeMillis();
                            int delay = Integer.parseInt(sharedPref.getString("period", "0"));
                            if ((currentInstant-lastMeasurement)/60000 < delay)
                                Toast.makeText(getContext(), "Wait for the next measurement", Toast.LENGTH_SHORT).show();
                            else {
                                Intent i = new Intent(getContext(), MeasurementService.class);
                                i.putExtra("type", chosenSSID[0]);
                                getActivity().startForegroundService(i);
                            }
                            
                        }).start();
                    } else
                        Toast.makeText(getContext(), "Choose another network", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("CANCEL", (dialog, which) -> {
                
                })
                .setSingleChoiceItems(ssidsArray, -1, (dialog, which) -> {
                    chosenSSID[0] = ssidsArray[which];
                })
                .show();
    }
    
    
    private boolean isLocationEnabled() {
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    
}