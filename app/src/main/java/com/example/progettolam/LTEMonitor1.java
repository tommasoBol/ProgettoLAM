package com.example.progettolam;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.TelephonyManager;

import java.util.ArrayList;
import java.util.List;

public class LTEMonitor1 extends BasicMonitor{
    
    private TelephonyManager tm;
    private int network;
    private List<CellInfo> cellInfoList;
    
    public LTEMonitor1(Application a) {
        super(a, "LTE", 201);
        tm = (TelephonyManager) a.getSystemService(Context.TELEPHONY_SERVICE);
    }
    
    @SuppressLint("MissingPermission")
    @Override
    public void setInstruments() {
        network = tm.getNetworkType();
        cellInfoList = tm.getAllCellInfo();
    }
    
    @Override
    public void measure(String posizione) {
        if (network != TelephonyManager.NETWORK_TYPE_UNKNOWN) {
            for (CellInfo cellInfo : cellInfoList) {
                if (cellInfo instanceof CellInfoLte && cellInfo.isRegistered()) {
                    measurements.computeIfAbsent(posizione, k -> new ArrayList<>()).add(((CellInfoLte) cellInfo).getCellSignalStrength().getLevel());
                }
            }
        }
    }
    
    @Override
    public void releaseInstruments() {
        return;
    }
}
