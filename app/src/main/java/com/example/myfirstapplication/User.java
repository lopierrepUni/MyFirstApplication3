package com.example.myfirstapplication;

import android.location.Location;

import java.util.ArrayList;

public class User {

    String name;
    Location loc;
    boolean conectado;
    String id;
    ArrayList<Location> locHist= new ArrayList<>();

    public User( String name,Location loc, boolean conectado) {
        this.name=name;

        this.loc = (loc);
        this.conectado=conectado;
        this.locHist=locHist;
    }
    public User(String id,String name, boolean conectado) {
        this.name=name;
        this.id=id;
        this.conectado=conectado;


    }
    public String getTime(){
        return String.valueOf(this.loc.getTime());
    }
    public String getLatitude(){
        return String.valueOf(this.loc.getLatitude());
    }
    public String getLongitude(){
        return String.valueOf(this.loc.getLongitude());
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLoc() {
        return loc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public boolean isConectado() {
        return conectado;
    }

    public void setConectado(boolean conectado) {
        this.conectado = conectado;
    }

    public ArrayList<Location> getLocHist() {
        return locHist;
    }

    public void setLocHist(ArrayList<Location> locHist) {
        this.locHist = locHist;
    }

}