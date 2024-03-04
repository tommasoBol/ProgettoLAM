package com.example.progettolam;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import mil.nga.mgrs.MGRS;
import mil.nga.mgrs.grid.Grid;
import mil.nga.mgrs.grid.GridType;

public class BackgroundLocationService extends Service {
    
    private LocationManager locationManager;
    private String lastLocation = "";
    private NotificationCompat.Builder mBuilder;
    private NotificationManagerCompat notificationManager;
    private Looper looper;
    private MeasurementRepository mRepo;
    private String ACTION_STOP_SERVICE= "STOP";
    private SharedPreferences sharedPreferences;
    private GridType chosenGrid;
    private int pedantism;
    
    public BackgroundLocationService() {
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction()!=null && intent.getAction().equals(ACTION_STOP_SERVICE)) {
            looper.quit();
            stopSelf();
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        pedantism = Integer.parseInt(sharedPreferences.getString("pedantism", "100"));
        if (pedantism==10) chosenGrid=GridType.TEN_METER;
        else if (pedantism==100) chosenGrid=GridType.HUNDRED_METER;
        else chosenGrid=GridType.KILOMETER;
        mRepo = new MeasurementRepository(getApplication());
        new Thread(() -> {
            Looper.prepare();
            looper = Looper.myLooper();
    
            createNotificationChannel();
            createForegroundNotification();
            
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 10000, 10, locationListener, null);
            Looper.loop();
        }).start();
        return START_NOT_STICKY;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
        looper.quit();
    }
    
    
    
    private LocationListener locationListener = new LocationListener() {
        @SuppressLint("MissingPermission")
        @Override
        public void onLocationChanged(@NonNull Location location) {
            try {
                String mgrsNewLocation = MyUtils.locationToMgrs(location, BackgroundLocationService.this, chosenGrid);
                if (!lastLocation.equals(mgrsNewLocation)) {
                    boolean LTEFlag = true;
                    boolean noiseFlag = true;
                    Map<String, List<String>> prova = mRepo.getMeasuredZoneForEveryType();
                    for (String t : prova.keySet()) {
                        for (String s : prova.get(t)) {
                            MGRS mgrs = MGRS.parse(s);
                            long newNorthing = Math.floorDiv(mgrs.getNorthing(), pedantism);
                            long newEasting = Math.floorDiv(mgrs.getEasting(), pedantism);
                            String newArea = mgrs.getZone() + "" + mgrs.getBand() + "" + mgrs.getColumn() + "" + mgrs.getRow() + "" + newEasting + "" + newNorthing;
                            if (newArea.equals(mgrsNewLocation)) {
                                if (t.equals("LTE")) LTEFlag=false;
                                else noiseFlag=false;
                                break;
                            }
                        }
                    }
                    if (LTEFlag || noiseFlag) {
                        NotificationCompat.Builder mBuilder1 = new NotificationCompat.Builder
                                (BackgroundLocationService.this, "Background Location")
                                .setSmallIcon(R.drawable.ic_launcher_background)
                                .setContentTitle("Background Location")
                                .setContentText("No measurements in this area")
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_MAX);

                        if (ActivityCompat.checkSelfPermission(BackgroundLocationService.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(BackgroundLocationService.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                && noiseFlag) {
                            Intent startNoiseMeasurement = new Intent(BackgroundLocationService.this, MeasurementService.class);
                            startNoiseMeasurement.putExtra("type", "Noise");
                            PendingIntent noisePending = PendingIntent.getService(BackgroundLocationService.this, 2, startNoiseMeasurement, PendingIntent.FLAG_MUTABLE);
                            mBuilder1.addAction(R.mipmap.map_icon, "Noise", noisePending);
                        }
                        if (ActivityCompat.checkSelfPermission(BackgroundLocationService.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(BackgroundLocationService.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                                && LTEFlag) {
                            Intent startLTEMeasurement = new Intent(BackgroundLocationService.this, MeasurementService.class);
                            startLTEMeasurement.putExtra("type", "LTE");
                            PendingIntent LTEPending = PendingIntent.getService(BackgroundLocationService.this, 3, startLTEMeasurement, PendingIntent.FLAG_MUTABLE);
                            mBuilder1.addAction(R.mipmap.map_icon, "LTE", LTEPending);
                        }
    
                        notificationManager = NotificationManagerCompat.from(BackgroundLocationService.this);
                        notificationManager.notify(101, mBuilder1.build());
                        
                    }
                    lastLocation = mgrsNewLocation;
                }
            }catch (Exception e) {
            
            }
        }
    };
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Background Location";
            String description = "Background Location";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(name.toString(), name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    
    private void createForegroundNotification() {
        Intent stopSelf = new Intent(this, BackgroundLocationService.class);
        stopSelf.setAction(ACTION_STOP_SERVICE);
    
        PendingIntent pendingIntent = PendingIntent.getService(BackgroundLocationService.this, 2, stopSelf, PendingIntent.FLAG_MUTABLE);
    
        mBuilder = new NotificationCompat.Builder
                (BackgroundLocationService.this, "Background Location")
                .setSmallIcon(R.mipmap.map_icon)
                .setContentTitle("Background Location")
                .setContentText("Monitoring your location")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .addAction(R.mipmap.stop_icon, "STOP", pendingIntent);
                
    
        notificationManager = NotificationManagerCompat.from(BackgroundLocationService.this);
        startForeground(100, mBuilder.build());
    }
}