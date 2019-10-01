package com.example.myfirstapplication.RoomDB2;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.myfirstapplication.RoomDB.MyDao;

@Database(entities = {usuarioConocidoDB.class},version=1)
public abstract class usuarioConocido2 extends RoomDatabase {
    public abstract usuarioConocidoDao2 usuarioConocidoDao2();
}