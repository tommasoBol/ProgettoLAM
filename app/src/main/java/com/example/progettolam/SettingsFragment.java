package com.example.progettolam;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsFragment extends PreferenceFragmentCompat {
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        EditTextPreference period = findPreference("period");
        EditTextPreference mForAverage = findPreference("mForAverage");
        EditTextPreference time = findPreference("time");
        EditTextPreference backTime = findPreference("back_time");
        SwitchPreferenceCompat notificationMeasured = findPreference("background_measurements");
        if (period!=null && mForAverage!=null && time!=null) {
            period.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
            mForAverage.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
            time.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
        }
        
        period.setOnPreferenceChangeListener((preference, newValue) -> checkPreference(newValue));
        mForAverage.setOnPreferenceChangeListener((preference, newValue) -> checkPreference(newValue));
        time.setOnPreferenceChangeListener((preference, newValue) -> checkPreference(newValue));
        //backTime.setOnPreferenceChangeListener((preference, newValue) -> checkPreference(newValue));
        
        
        notificationMeasured.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                boolean isChecked = (boolean) newValue;
                if (isChecked) {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Intent i = new Intent(getActivity(), BackgroundLocationService.class);
                        getActivity().startForegroundService(i);
                        return true;
                    } else {
                        Toast.makeText(SettingsFragment.this.getContext(), "You must enable always on location", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } else {
                    getActivity().stopService(new Intent(getActivity(), BackgroundLocationService.class));
                    return true;
                }
            }
        });
    }

    
    private boolean checkPreference(Object newValue) {
        char[] nuovoValore = ((String) newValue).toCharArray();
        if (nuovoValore.length>1 && nuovoValore[0]=='0') {
            Toast.makeText(SettingsFragment.this.getContext(), "Value must be a positive integer", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }
}