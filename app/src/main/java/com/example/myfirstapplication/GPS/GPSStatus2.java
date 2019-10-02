package com.example.myfirstapplication.GPS;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.myfirstapplication.MainActivity;
import com.example.myfirstapplication.RoomDB.UserLocHistDB;
import com.example.myfirstapplication.User;

import org.osmdroid.util.GeoPoint;

public class GPSStatus2 extends MainActivity implements LocationListener {

    Activity activity;
    public GPSStatus2(Activity activity) {
        this.activity = activity;

    }

    public void startGPSRequesting() {
        try {
            Log.i("Confirmacion: ", "aaaa " + Manifest.permission.ACCESS_FINE_LOCATION + " ," + Manifest.permission.ACCESS_COARSE_LOCATION);
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            ubicacion.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 3, gpsStatus, Looper.getMainLooper());
            ubicacion.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 3, gpsStatus, Looper.getMainLooper());
            ubicacion = (LocationManager) getSystemService(Context.LOCATION_SERVICE);



        }catch (Exception e){
            Log.i("fallo"," el error: "+e.toString());
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        Log.i("Conf2","Cambio pos");
        Log.i("Conf2","yo:  "+gpsOn);
      /*  Toast toast1 =Toast.makeText(getApplicationContext(),
                "Cambio la pos", Toast.LENGTH_SHORT);

        toast1.show();
        */if (gpsOn) {
            Log.i("Conf2","yo:  "+yo);
            if (yo!= null) {
                yo.setLoc(location);
                yo.getLoc().setLatitude(location.getLatitude()+x ); // Es solo para probar, QUITAR AL FINAL
                yo.getLoc().setLongitude(location.getLongitude()+y); // Es solo para probar, QUITAR AL FINAL
                osm.getOverlays().remove(myMarker);
                addMarker(users.get(0), true,false);
                Log.i("Coordenadas ", i + ": " + String.valueOf(loc.getLatitude()) + ", " + String.valueOf(loc.getLongitude()) + ", At: " + String.valueOf(loc.getTime()));
                i++;
                y = x+0.1;
                x = y+0.2;
                if (!online) {
                    Log.d("Guardar posicion", "antes");
                    db.myDao().add(new UserLocHistDB(yo.getTime(), yo.getLatitude(), yo.getLongitude()));
                    Log.i("Guardar posicion", "pos añadida, tamaño de la db:" + String.valueOf(db.myDao().getAll().size()));
                } else {
                    Log.i("Guardar posicion", "entro al else");
                    //CONSUMIR WEB SERVICE
                }
                Log.d("Onlineeee", String.valueOf(online));
            }
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {


    }

    @Override
    public void onProviderEnabled(String s) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("Confirmacion2","GPS activado");

                Log.i("GPS Activado","GPS Activado00000");
                GPSState.setTextColor(Color.GREEN);

//                    localizaciones();
                osm.getOverlays().remove(myMarker);

                if(yo==null) {
                    yo = new User("Yo", myPos(), true);
                 //   yo.setId(10);//  ESTE ID SE RECIBE AL MOMENTO DE REGISTRARSE O LOGEARSE
                    addMarker(yo, true, false);
                    users.add(0, yo);
                }
                mc.animateTo(new GeoPoint(lat,longi));
                Log.i("GPS Activado","Latitud "+lat+" longitud "+longi);

                mc.setZoom(12);
                yo.setLoc(myPos());
                if (online) {
                    for (int i = 1; i < users.size(); i++) {
                        Log.d("Confirmación", "USUARIO " + i);
                        addMarker(users.get(i), false, false);
                    }
                }

                if (!online){
                    db.myDao().add(new UserLocHistDB(yo.getTime(), yo.getLatitude(), yo.getLongitude()));

                }/*Guardo su pos en el Room DB*/else {
                    //CONSUMO EL WS
                    // Crear usuario con nombre, y estado
                    //Crear location con time, lat y longi y añadir al usuario creado
                    //Añadir al usuario a la lista de usuarios
                    // Log.i("Confirmacion:","entra al gps como si estuviera on");
                    for (int i = 1; i < users.size() ; i++) {
                        Log.d("Confirmación","USUARIO "+i);
                        addMarker(users.get(i),false,false);
                    }
                }//Añado a los usuarios que consigo atravez del ws
            }

        },3000); //Funciona

            /*users.clear();
            yo = new User("Yo",myPos(),true);
            addMarker(yo,true,false);
            users.add(yo);
            crearUsuariosDePrueba();// BORRAR
            mc.animateTo(new GeoPoint(lat,longi));
            Log.i("Usuario Creado", "4");
*/

    }

    @Override
    public void onProviderDisabled(String s) {
        if (mapOpen) {
            Log.i("Confirmacion", "GPS desactivado");

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            GPSState.setTextColor(Color.RED);
            builder.setTitle("GPS Desactivado");
            builder.setMessage("Por favor active el gps");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            //    builder.show();
            Toast toast1 =
                    Toast.makeText(getApplicationContext(),
                            "GPS desactivado", Toast.LENGTH_SHORT);

            toast1.show();
        }
    }
}

