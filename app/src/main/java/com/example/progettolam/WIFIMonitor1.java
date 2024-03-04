package com.example.progettolam;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WIFIMonitor1 extends BasicMonitor{
    
    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    private String chosenSSID;
    
    public WIFIMonitor1(Application a) {
        super(a, "WIFI", 203);
        wifiManager = (WifiManager) a.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }
    
    @Override
    public void setInstruments() throws IOException {
        wifiInfo = wifiManager.getConnectionInfo();
    }
    
    @SuppressLint("MissingPermission")
    @Override
    public void measure(String posizione) {
        for(ScanResult sr : wifiManager.getScanResults()) {
            if (sr.SSID.equals(chosenSSID)) {
                measurements.computeIfAbsent(posizione, k -> new ArrayList<>()).add(sr.level);
            }
        }
    }
    
    @Override
    public void releaseInstruments() {
        return;
    }
    
    @SuppressLint("MissingPermission")
    public List<String> scan() {
        List<String> ssids = new ArrayList<>();
        for(ScanResult sr : wifiManager.getScanResults()) {
            ssids.add(sr.SSID);
        }
        return ssids;
    }
    
    public String getChosenSSID() {
        return chosenSSID;
    }
    
    public void setChosenSSID(String s) {
        chosenSSID = s;
    }
}
