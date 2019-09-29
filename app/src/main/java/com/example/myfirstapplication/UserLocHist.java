package com.example.myfirstapplication;

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
public  class UserLocHist {
    @NonNull@PrimaryKey(autoGenerate = true) int i;
    @ColumnInfo(name = "Time") private String time;
    @ColumnInfo(name = "Longitud") private String longitude;
    @ColumnInfo(name = "Latitud") private String latitude;



    public UserLocHist(String time, String latitude, String longitude) {


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
}/*
@Dao
interface UserLocHistDao {
    @Query("SELECT * FROM Locs")
    List<Location> getAll();
    @Insert
    void insert(String time, String logitud,String latitud);
    @Query("DELETE FROM Locs")
    void deleteTable();
}
@Database(entities = {UserLocHistDao.class}, version = 1)
abstract class AppDatabase extends RoomDatabase {
    public abstract UserLocHistDao userLocHistDao();
}*/
