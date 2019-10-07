package com.example.myfirstapplication.RoomDB2;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "UsCon")
public class usuarioConocidoDB {
    @NonNull
    @PrimaryKey(autoGenerate = true) int i;
    @ColumnInfo(name="Id") String id;
    @ColumnInfo(name = "Usuario") private String user;
    @ColumnInfo(name = "Contraseña") private String pass;

    public usuarioConocidoDB(String id, String user, String pass) {
        this.id = id;
        this.user = user;
        this.pass = pass;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}
