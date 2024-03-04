package com.example.progettolam;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.function.Consumer;

import javax.security.auth.callback.Callback;

import mil.nga.mgrs.MGRS;
import mil.nga.mgrs.grid.GridType;
import mil.nga.mgrs.tile.MGRSTileProvider;

public class MyUtils {
    
    private static Location lastLocation;
    private static MGRSTileProvider mgrsTileProvider;
    
    public static void getDeviceLocation(Context c, Consumer<Location> success, Consumer<Exception> error) throws SecurityException{
        Log.d("DEVICE LOCATION", "STARTING");
        FusedLocationProviderClient fused = LocationServices.getFusedLocationProviderClient(c);
        Task<Location> locationResult = fused.getLastLocation();
        locationResult.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location !=null)
                    success.accept(location);
                else
                    error.accept(new Exception());
            }
        });
        locationResult.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                error.accept(e);
            }
        });
    };
    
    public static String locationToMgrs(Location l, Context c, GridType gt) {
        LatLng latLng = new LatLng(l.getLatitude(), l.getLongitude());
        mgrsTileProvider = MGRSTileProvider.create(c);
        return mgrsTileProvider.getCoordinate(latLng, gt);
    }
    
    
    public static double avg(List<Integer> l) {
        if (l.size()>0) {
            double somma = 0;
            double avg;
            for (Integer i : l) {
                somma += i;
            }
            return somma / l.size();
        } else {
            return -1;
        }
    }
}
