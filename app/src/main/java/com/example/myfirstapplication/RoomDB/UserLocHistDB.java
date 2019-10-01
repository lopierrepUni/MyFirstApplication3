package com.example.myfirstapplication.RoomDB;


import android.location.Location;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.RoomDatabase;

import java.util.List;

@Entity(tableName = "Locs")
public  class UserLocHistDB {
    @NonNull@PrimaryKey(autoGenerate = true) int i;
    @ColumnInfo(name = "Time") private String time;
    @ColumnInfo(name = "Longitud") private String longitude;
    @ColumnInfo(name = "Latitud") private String latitude;
    public UserLocHistDB(String time, String latitude, String longitude) {
        this.time = time;
        this.longitude = longitude;
        this.latitude = latitude;
    }
    @NonNull
    public String getTime() {
        return time;
    }

    public void setTime(@NonNull String time) {
        this.time = time;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}
