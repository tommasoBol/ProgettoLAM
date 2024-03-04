package com.example.progettolam;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

@Entity(tableName = "measurement_table")
public class Measurement {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "type")
    private String tipo;
    
    @ColumnInfo(name = "zone")
    private String zona;
    
    @ColumnInfo(name = "result")
    private double risultato;
    
    @ColumnInfo(name = "timestamp")
    private long timestamp;
    
    
    
        public Measurement() {
        
        }
        public String getTipo() {
            return this.tipo;
        }
        
        public String getZona() {
            return this.zona;
        }
        
        public double getRisultato() {
            return this.risultato;
        }
        
        public long getTimestamp() { return this.timestamp ;}
        
        public int getId() {
            return this.id;
        }
        
        public void setId(int i) {
            this.id = i;
        }
        
        public void setTipo(String s) {
            this.tipo = s;
        }
        
        public void setZona(String s) {
            this.zona = s;
        }
        
        public void setRisultato(double r) {
            this.risultato = r;
        }
        
        public void setTimestamp(long l) { this.timestamp = l; }
    
        public String toString() {
            Date d = new Date(this.timestamp);
            String risultato;
            if (this.tipo=="LTE") risultato = this.risultato + "/4";
            else risultato = String.valueOf(this.risultato);
            return "Type: " + this.tipo + "\n" +"Result: " + risultato + "\n" + "Zone: " + this.zona + "\n" + "Date: " + d.toString();
        }
    
}
