package com.example.myfirstapplication.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.example.myfirstapplication.MainActivity;
import com.example.myfirstapplication.RoomDB.UserLocHistDB;

import java.util.ArrayList;
import java.util.List;

public class BroadcastManager extends MainActivity    {

    public BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = manager.getActiveNetworkInfo();
            onNetworkChange(ni);
        }


    };

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onPause() {
        unregisterReceiver(networkStateReceiver);
        super.onPause();
    }

    private void onNetworkChange(NetworkInfo networkInfo) {
        Toast toast1;
        try {
            if (networkInfo != null) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    revisarCambioskstartThread();
                    users = new ArrayList<>();
                    users.add(yo);
                    Log.i("size antes de crear 2", "size=" + users.size());
                    crearUsuariosDePrueba();// BORRAR
                    Log.i("size despues de crear 2", "size=" + users.size());
                    Log.i("MenuActivity", "CONNECTED");
                    toast1 = Toast.makeText(getApplicationContext(), "Conectado", Toast.LENGTH_SHORT);
                    InternetState.setTextColor(Color.GREEN);
                    InternetState.setText("Conectado");

                    revisarCambioskstartThread();
                    if (true/*SI CAMBIO ALGUNA POS, CONSUMIR WS QUE RETORNE TODOS LOS USUARIOS*/) {
                        osm.getOverlays().clear();
                        for (int j = 1; j < users.size(); j++) {
                            boolean soyYo = false;
                            if (j > 0) {
                                soyYo = false;
                            }
                            addMarker(users.get(j), soyYo, false);
                        }
                    }
                    // preguntar si hay algun usuario nuevo
                    if (!online) {
                        List<UserLocHistDB> locs = db.myDao().getAll();
                        Log.d("NOTAAA", String.valueOf(db.myDao().getAll()));
                        Log.d("Lista: ", "");
                        if (locs != null) {
                            for (int j = 0; j < locs.size(); j++) {
                                Log.d("Elemento: ", j + ": " + locs.get(j));
                                // ENVIAR CON WEB SERVICE EL ID DE YO, LOC.TIM, LOC.LONGI Y LOC.LATI
                            }
                        }

                        db.myDao().deleteTable();
                        for (int j = 0; j < users.size(); j++) {
                            boolean soyYo = true;
                            if (j > 0) {
                                soyYo = false;
                            }
                            addMarker(users.get(j), soyYo, false);
                        }
                    }
                    Log.i("Confirmación", "Wifi Activado");

                    online = true;
                    //     InternetState.setText("Conectado");

                } else {
                    InternetState.setText("Desconectado");
                    InternetState.setTextColor(Color.RED);
                    online = false;
                    Log.i("MenuActivity", "DISCONNECTED");
                    toast1 = Toast.makeText(getApplicationContext(), "Desconectado", Toast.LENGTH_SHORT);
                }
            } else {
                InternetState.setText("Desconectado");
                InternetState.setTextColor(Color.RED);
                toast1 = Toast.makeText(getApplicationContext(), "Desconectado", Toast.LENGTH_SHORT);
                online = false;
                Log.i("MenuActivity", "DISCONNECTED");
            }
            toast1.show();
        } catch (Exception e) {
            Log.i("Error de internet", e.toString());
        }
    }
}