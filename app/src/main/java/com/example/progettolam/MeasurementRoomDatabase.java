package com.example.progettolam;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Measurement.class}, version=1, exportSchema = false)
public abstract class MeasurementRoomDatabase extends RoomDatabase {

    public abstract MeasurementDAO mDao();
    
    public static volatile MeasurementRoomDatabase INSTANCE;
    private static final int nTHREADS = 4;
    
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(nTHREADS);
    
    static MeasurementRoomDatabase getDatabase(Context context) {
        if (INSTANCE==null) {
            synchronized (MeasurementRoomDatabase.class) {
                if (INSTANCE==null)  {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    MeasurementRoomDatabase.class, "measurement_database").build();
                }
            }
        }
        return INSTANCE;
    }
}

