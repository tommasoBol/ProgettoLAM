package com.example.progettolam;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.MapInfo;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
import java.util.Map;

@Dao
public interface MeasurementDAO {
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertMeasurement(Measurement m);
    
    @Query("DELETE FROM measurement_table")
    void deleteAll();
    
    @Query("SELECT * FROM measurement_table ORDER BY id DESC")
    LiveData<List<Measurement>> getLiveMeasurements();
    
    @Query("SELECT DISTINCT zone FROM measurement_table WHERE type LIKE :t")
    List<String> getMeasuredZoneByType(String t);
    
    @Query("SELECT result FROM measurement_table WHERE zone LIKE :z AND type LIKE :t ORDER BY id DESC LIMIT :l")
    List<Double> getMeasurementForDraw(String z, String t, int l);
    
    @MapInfo(keyColumn = "type", valueColumn = "zone")
    @Query("SELECT DISTINCT type, zone from measurement_table")
    Map<String, List<String>> getMeasuredZoneForEveryType();
    
    @MapInfo(keyColumn = "zone", valueColumn = "timestamp")
    @Query ("SELECT zone, timestamp FROM measurement_table WHERE type LIKE :t ORDER BY id DESC LIMIT 1")
    LiveData<Map<String, Long>> getLastMeasurementByType(String t);
    
    @Query("SELECT timestamp FROM measurement_table WHERE type LIKE :t ORDER BY id DESC LIMIT 1")
    long getLastWifiMeasurement(String t);
    
    @Query("SELECT DISTINCT type FROM measurement_table WHERE type NOT LIKE 'LTE' AND type NOT LIKE 'Noise'")
    String[] getMeasuredNetwork();
}
