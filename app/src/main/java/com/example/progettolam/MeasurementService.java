package com.example.progettolam;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import mil.nga.mgrs.grid.GridType;

public class MeasurementService extends Service {
    
    private Looper looper;
    private LocationManager locationManager;
    private BasicMonitor monitor;
    private Timer timer;
    private NotificationCompat.Builder mBuilder;
    private String measurementType;
    private SharedPreferences sharedPreferences;
    private NotificationManagerCompat nm;
    private MeasurementRepository mRepo;
    
    
    public MeasurementService() {
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> {
            try {
                Looper.prepare();
                looper = Looper.myLooper();
    
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                mRepo = new MeasurementRepository(getApplication());
                timer = new Timer();
                nm = NotificationManagerCompat.from(getApplicationContext());
                
                setMonitor(intent);
                
                createNotificationChannel();
                
                startForeground(monitor.getNotificationID(), createNotification("Measurement started..."));
                monitor.setInstruments();
                
    
                locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 1000, 0, locationListener, null);
    
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        nm.notify(monitor.getNotificationID(),createNotification("End"));
                        locationManager.removeUpdates(locationListener);
                        monitor.releaseInstruments();
                        addMeasurementsToDatabase(monitor.getMeasurements());
                        looper.quit();
                        timer.cancel();
                        stopForeground(STOP_FOREGROUND_DETACH);
                    }
                }, getMeasurementTime() * 1000);
                
                Looper.loop();
            } catch (IOException e) {
                throw new RuntimeException();
            }
            
            
        }).start();
        return START_NOT_STICKY;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    
    private void setMonitor(Intent intent) {
        Bundle intentExtras = intent.getExtras();
        measurementType = intentExtras.get("type").toString();
        Log.d("Measurement type", measurementType);
        if (intentExtras.get("type").equals("Noise"))
            monitor = new NoiseMonitor1(getApplication());
        else if (intentExtras.get("type").equals("LTE"))
            monitor = new LTEMonitor1(getApplication());
        else {
            WIFIMonitor1 wfm = new WIFIMonitor1(getApplication());
            wfm.setChosenSSID(measurementType);
            monitor = wfm;
        }
    }

    
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            String mgrs = MyUtils.locationToMgrs(location, getApplicationContext(), GridType.TEN_METER);
            Log.d("POSIZIONE DURANTE LA MISURAZIONE", mgrs);
            monitor.measure(mgrs);
        }
    };
    
    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Measurement";
            String description = "Measurement";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(name.toString(), name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    @SuppressLint("MissingPermission")
    public Notification createNotification(String content) {
        mBuilder = new NotificationCompat.Builder
                (getApplicationContext(), "Measurement")
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .setSmallIcon(R.mipmap.map_icon)
                .setContentTitle(measurementType + "Measurement")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        return mBuilder.build();
    }
    
    private int getMeasurementTime() {
        return Integer.parseInt(sharedPreferences.getString("time", "20"));
    }
    
    
    private void addMeasurementsToDatabase(Map<String, List<Integer>> mMap) {
        for (String s : mMap.keySet()) {
                Measurement m = new Measurement();
                m.setTipo(measurementType);
                m.setTimestamp(System.currentTimeMillis());
                m.setZona(s);
                m.setRisultato(MyUtils.avg(mMap.get(s)));
                mRepo.insertMeasurement(m);
        }
    }
}