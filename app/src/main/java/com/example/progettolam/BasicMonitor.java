package com.example.progettolam;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import mil.nga.mgrs.grid.GridType;

public abstract class BasicMonitor{
    
    protected Map<String, List<Integer>> measurements;
    protected MeasurementRepository mRepo;
    protected String measurementType;
    protected int notificationID;

    
    public BasicMonitor(Application a, String measurementT, int notID) {
        measurements = new TreeMap<>();
        mRepo = new MeasurementRepository(a);
        measurementType = measurementT;
        notificationID = notID;
    }
    
    
    public int getNotificationID() {
        return notificationID;
    }
    
    public Map<String, List<Integer>> getMeasurements() {
        return this.measurements;
    }
    
    public abstract void setInstruments() throws IOException;
    public abstract void measure(String posizione);
    public abstract void releaseInstruments();


}
