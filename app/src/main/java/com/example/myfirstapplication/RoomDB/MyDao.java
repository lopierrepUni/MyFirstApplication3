package com.example.myfirstapplication.RoomDB;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
@Dao
public interface MyDao {
    @Query("SELECT * FROM Locs")
    List<UserLocHistDB>getAll();
    //List<String[]> getUserLocHist();
    @Insert
    public void add(UserLocHistDB userLocHist);
    @Query("DELETE FROM Locs")
    void deleteTable();
}
