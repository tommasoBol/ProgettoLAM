package com.example.progettolam;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Map;

public class MonitorViewModel extends AndroidViewModel {
    
    private MeasurementRepository mRepo;
    
    public MonitorViewModel(@NonNull Application application) {
        super(application);
        mRepo = new MeasurementRepository(application);
    }
    
    public LiveData<Map<String,Long>> getLastMeasurementByType(String t) {
        return mRepo.getLastMeasurementByType(t);
    }
    
    public long getLastWiFiMeasurement(String t) {
        return mRepo.getLastWifiMeasurement(t);
    }
}
