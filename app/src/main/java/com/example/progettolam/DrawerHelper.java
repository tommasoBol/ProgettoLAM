package com.example.progettolam;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Message;
import android.os.Handler;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


import mil.nga.grid.features.Point;
import mil.nga.mgrs.MGRS;


public class DrawerHelper{
    
    /*private MeasurementRepository mRepo;
    private String mapType;
    private int mapArea;
    private Application app;
    private Handler mainHandler;
    private final int PRECISIONE_MISURAZIONE = 10;
    public DrawerHelper(Application a, String mt, int ma, Handler h) {
        app = a;
        mRepo = new MeasurementRepository(app);
        mapType = mt;
        mapArea = ma;
        mainHandler = h;
    }
    
    @Override
    public void run() {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app.getApplicationContext());
            int limit = Integer.parseInt(sharedPreferences.getString("mForAverage", "1"));
            //List<Measurement> myList = mRepo.getLimitMeasurementsByType(mapType, Integer.parseInt(sharedPreferences.getString("mForAverage", "1")));
            List<String> zones = mRepo.getZonesByType(mapType);
            Map<String, List<Double>> data = new TreeMap<>();
            Map<String, List<Double>> result = new TreeMap<>();
            for (String z : zones) {
                data.put(z, mRepo.getMeasurementsForDraw(z, mapType, limit));
            }
            
            if (PRECISIONE_MISURAZIONE != mapArea) {
                for (String s : data.keySet()) {
                    MGRS mgrs = MGRS.parse(s);
                    long newNorthing = Math.floorDiv(mgrs.getNorthing(), mapArea);
                    long newEasting = Math.floorDiv(mgrs.getEasting(), mapArea);
                    String newArea = mgrs.getZone() + "" + mgrs.getBand() + "" + mgrs.getColumn() + "" + mgrs.getRow() + "" + newEasting + "" + newNorthing;
                    result.computeIfAbsent(newArea, k -> new ArrayList<>()).addAll(data.get(s));
                }
            } else
                result = data;
            
            Map<String, Double> avgForArea = new TreeMap<>();
            for (String s : result.keySet()) {
                avgForArea.put(s, avg(result.get(s)));
            }
            Message m = mainHandler.obtainMessage();
            m.obj = avgForArea;
            mainHandler.sendMessage(m);
        }catch (ParseException e) {
            throw new RuntimeException();
        }
    }
    
    private double avg(List<Double> l) {
        double somma = 0;
        for (double d : l)
            somma += d;
        return somma/l.size();
    }
    
     */
}

/*
                    if (PRECISIONE_MISURAZIONE > mapArea) {
                            for (int i = 0; i < PRECISIONE_MISURAZIONE / mapArea; i++) {
        for (int j = 0; j < PRECISIONE_MISURAZIONE / mapArea; j++) {
        String newArea = mgrs.getZone() + "" + mgrs.getBand() + "" + mgrs.getColumn() + "" + mgrs.getRow() + "" + (newEasting + j) + "" + (newNorthing + i);
        result.computeIfAbsent(newArea, k -> new ArrayList<>()).add(m.getRisultato());
        }
        }
        } else {
*/