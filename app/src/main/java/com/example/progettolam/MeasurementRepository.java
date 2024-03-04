package com.example.progettolam;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.Map;

public class MeasurementRepository {
    
    private MeasurementDAO mDao;
    
    public MeasurementRepository(Application app) {
        MeasurementRoomDatabase db = MeasurementRoomDatabase.getDatabase(app);
        mDao = db.mDao();
    }
    
    
    public void insertMeasurement(Measurement m) {
        MeasurementRoomDatabase.databaseWriteExecutor.execute(() -> {
            mDao.insertMeasurement(m);
        });
    }
    
    public void deleteAllMeasurements() {
        MeasurementRoomDatabase.databaseWriteExecutor.execute(() -> {
            mDao.deleteAll();
        });
    }
    
    
    public LiveData<List<Measurement>> getLiveMeasurements() {
        return mDao.getLiveMeasurements();
    }
    
    public List<String> getMeasuredZoneByType(String t) {
        return mDao.getMeasuredZoneByType(t);
    }
    
    public List<Double> getMeasurementsForDraw(String z, String t, int l) {
        return mDao.getMeasurementForDraw(z,t,l);
    }
    
    public Map<String, List<String>> getMeasuredZoneForEveryType() {
        return mDao.getMeasuredZoneForEveryType();
    }
    
    public LiveData<Map<String, Long>> getLastMeasurementByType(String t) {
        return mDao.getLastMeasurementByType(t);
    }
    
    public long getLastWifiMeasurement(String t) {
        return mDao.getLastWifiMeasurement(t);
    }
    
    public String[] getMeasuredNetwork() {
        return mDao.getMeasuredNetwork();
    }

    
}
