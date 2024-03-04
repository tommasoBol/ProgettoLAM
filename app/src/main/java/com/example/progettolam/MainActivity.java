package com.example.progettolam;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnItemSelectedListener {
    
    private BottomNavigationView bottomNavigationView;
    private FragmentContainerView fragmentContainerView;
    private SharedPreferences sharedPreferences;
    private MonitorFragment mf;
    private MapsFragment mapF;
    private SettingsFragment sf;
    private ItemFragment lf;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mf = new MonitorFragment();
        mapF = new MapsFragment();
        sf = new SettingsFragment();
        lf = new ItemFragment();
        
        fragmentContainerView = findViewById(R.id.fragment);
        
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        
        bottomNavigationView.setSelectedItemId(R.id.btnMonitor);
        bottomNavigationView.setOnItemSelectedListener(this);
        
    
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        if (sharedPreferences.getBoolean("background_measurements", false)
        && (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            startForegroundService(new Intent(this, BackgroundLocationService.class));
        }
    }
    
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnMonitor:
                if (bottomNavigationView.getSelectedItemId()==R.id.btnMonitor) return true;
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, mf)
                        .commit();
                break;
            case R.id.btnMap:
                if (bottomNavigationView.getSelectedItemId()==R.id.btnMap) return true;
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, mapF)
                        .commit();
                break;
            case R.id.btnSettings:
                if (bottomNavigationView.getSelectedItemId()==R.id.btnSettings) return true;
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, sf)
                        .commit();
                break;
            case R.id.btnList:
                if (bottomNavigationView.getSelectedItemId()==R.id.btnList) return true;
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, lf)
                        .commit();
                break;
        }
        return true;
    }
    
    private ActivityResultLauncher<String[]> requestPermissions =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
                if (!isGranted.containsValue(false)) {
                    Intent i = new Intent(this, BackgroundLocationService.class);
                    startService(i);
                } else {
                    Toast.makeText(this, "Background location access: DENIED", Toast.LENGTH_LONG).show();
                }
            });

}