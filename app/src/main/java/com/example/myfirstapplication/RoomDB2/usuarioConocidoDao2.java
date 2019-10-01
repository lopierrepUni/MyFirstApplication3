package com.example.myfirstapplication.RoomDB2;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface usuarioConocidoDao2 {
    @Query("SELECT * FROM UsCon")
    List<usuarioConocidoDB> getAll();
    //List<String[]> getUserLocHist();
    @Insert
    public void add(usuarioConocidoDB usuarioConocido);
    @Query("DELETE FROM UsCon")
    void deleteTable();
}
