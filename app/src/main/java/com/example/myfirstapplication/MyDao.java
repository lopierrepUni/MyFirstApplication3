package com.example.myfirstapplication;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;
@Dao
public interface MyDao {
        @Query("SELECT * FROM Locs")
        List<UserLocHist>getAll();
        //List<String[]> getUserLocHist();
        @Insert
        public void add(UserLocHist userLocHist);
        @Query("DELETE FROM Locs")
        void deleteTable();
}
