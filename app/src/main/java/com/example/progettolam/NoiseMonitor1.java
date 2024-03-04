package com.example.progettolam;

import android.app.Application;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.telephony.CellInfoLte;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class NoiseMonitor1 extends BasicMonitor{
    
    private MediaRecorder recorder;
    
    public NoiseMonitor1(Application a) {
        super(a, "Noise", 202);
        recorder = new MediaRecorder();
    }
    
    @Override
    public void setInstruments() throws IOException {
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "RECORDING8"));
        recorder.prepare();
        recorder.start();
    }
    
    @Override
    public void measure(String posizione) {
        int powerDB = (int) (20 * Math.log10(recorder.getMaxAmplitude()/2));
        if (powerDB>0) measurements.computeIfAbsent(posizione, k -> new ArrayList<>()).add(powerDB);
    }
    
    @Override
    public void releaseInstruments() {
        recorder.stop();
        recorder.release();
    }
}
