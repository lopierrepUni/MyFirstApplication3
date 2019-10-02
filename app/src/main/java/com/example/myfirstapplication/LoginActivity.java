package com.example.myfirstapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.room.Room;

import com.example.myfirstapplication.RoomDB.MyAppDatabase;
import com.example.myfirstapplication.RoomDB.UserLocHistDB;
import com.example.myfirstapplication.RoomDB2.usuarioConocido2;
import com.example.myfirstapplication.RoomDB2.usuarioConocidoDB;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class LoginActivity extends Activity {
Button loginB,registerB;
boolean online;
public int id;

    public usuarioConocido2 db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        BroadcastReceiver networkStateReceiver;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_layout);
        loginB=(Button) findViewById(R.id.login_button);
        registerB=(Button) findViewById(R.id.register_button);
        db= Room.databaseBuilder(getApplicationContext(), usuarioConocido2.class, "Usuarios registrados").allowMainThreadQueries().build();

        ((Button)findViewById(R.id.login_button)). setOnClickListener(new View.OnClickListener() {
            String respuesta;
            @Override
            public void onClick(View view) {
                id = 10;

                Intent intetToBecalled = new Intent(getApplicationContext(), MainActivity.class);
                final String usuario=((EditText)findViewById(R.id.login_user_name)).getText().toString();
                final String contraseña=((EditText)findViewById(R.id.login_password)).getText().toString();
                intetToBecalled.putExtra("user_name", usuario);
                intetToBecalled.putExtra("user_password", contraseña);
                intetToBecalled.putExtra("id", id);
                Log.i("Login", "LOGEADO00");
                if (online) {
                    new Thread(new Runnable() {
                        public void run() {
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .url("http://10.0.2.2:8080/WebSer/webresources/generic/login?id=" + usuario + "&Pass=" + contraseña)
                                    .get()
                                    .build();
                            try {
                                Log.i("response: ", "entra al try");
                                Response response = client.newCall(request).execute();
                                respuesta = response.body().string();

                            } catch (IOException e) {
                                Log.i("error: ", "error en el ws catch " + e);

                                e.printStackTrace();
                            }
                        }
                    }).start();

                    while(respuesta==null){
                        Log.i("error: ", "la respuesta es " + respuesta);
                    }
                    if (respuesta.equals("Inicio de sesion correcto") /*AQUI VA EL WS PARA SABER SI EL USUARIO ES VALIDO EN CASO DE TENER ACCESO A INTERNET */) {
                        Toast toast1 =Toast.makeText(getApplicationContext(),"Bienvenido camarada", Toast.LENGTH_SHORT);
                        toast1.show();
                        startActivity(intetToBecalled);
                        respuesta=null;
                    } else {
                        if (respuesta.equals("Identificación o contraseña incorrecta")) {
                            Toast toast1 = Toast.makeText(getApplicationContext(), "Usuario y/o contraseña incorrectos", Toast.LENGTH_SHORT);
                            toast1.show();
                            respuesta=null;

                        }else{
                            Toast toast1 = Toast.makeText(getApplicationContext(), respuesta, Toast.LENGTH_SHORT);
                            toast1.show();
                            respuesta=null;


                        }
                    }

                } else {
                    ArrayList<usuarioConocidoDB> usuarioConocidoDBArrayList= (ArrayList<usuarioConocidoDB>) db.usuarioConocidoDao2().getAll();
                    boolean encontrado=false;
                    for (int i = 0; i < usuarioConocidoDBArrayList.size(); i++) {
                        if (usuarioConocidoDBArrayList.get(i).getUser().equals(usuario)&& usuarioConocidoDBArrayList.get(i).getPass().equals(contraseña)){
                            startActivity(intetToBecalled);
                            encontrado=true;
                            break;
                        }
                    }
                    if (encontrado==false){
                        Toast toast1 = Toast.makeText(getApplicationContext(), "Usuario y/o contraseña incorrectos", Toast.LENGTH_SHORT);

                        toast1.show();
                    }
                }
            }
        });
        ((Button)findViewById(R.id.register_button)). setOnClickListener(new View.OnClickListener() {
            String respuesta;
            @Override
            public void onClick(View view) {

                if (online) {
                    Intent intetToBecalled = new Intent(getApplicationContext(), MainActivity.class);
                    final String usuario = ((EditText) findViewById(R.id.login_user_name)).getText().toString();
                    final String contraseña = ((EditText) findViewById(R.id.login_password)).getText().toString();
                    intetToBecalled.putExtra("user_name", usuario);
                    intetToBecalled.putExtra("user_password", contraseña);
                    intetToBecalled.putExtra("id", id);
                    Log.i("Login", "LOGEADO00");
                    new Thread(new Runnable() {
                        public void run() {
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .url("http://10.0.2.2:8080/WebSer/webresources/generic/createuser?nombre=" + usuario + "&id=" + usuario + "&Pass=" + contraseña)
                                    .get()
                                    .build();
                            try {
                                Log.i("response: ", "entra al try");
                                Response response = client.newCall(request).execute();
                                respuesta = response.body().string();
                            } catch (IOException e) {
                                Log.i("error: ", "error en el ws catch " + e);
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    while (respuesta == null) {}
                    if (respuesta.equals("Usuario creado exitosamente") /*AQUI VA EL WS PARA SABER REGISTRAR EL USUARIO Y SABER SI EL REGISTRO FUE VALIDO*/) {
                        db.usuarioConocidoDao2().add(new usuarioConocidoDB(id, usuario, contraseña));
                        int tam = db.usuarioConocidoDao2().getAll().size();
                        String us = db.usuarioConocidoDao2().getAll().get(0).getUser();
                        String pass = db.usuarioConocidoDao2().getAll().get(0).getPass();
                        Log.i("info importante", "size " + String.valueOf(tam) + " , valor 1 " + us + "valor 2" + pass);
                        startActivity(intetToBecalled);
                        Toast toast1 = Toast.makeText(getApplicationContext(), "Registro exitoso", Toast.LENGTH_SHORT);
                        toast1.show();
                        respuesta = null;
                        } else {
                            Toast toast1 = Toast.makeText(getApplicationContext(), "Nombre de usuario no disponible", Toast.LENGTH_SHORT);
                            toast1.show();
                        }
                    }
                else {
                    Toast toast1 = Toast.makeText(getApplicationContext(), "No hay coneccion a internet", Toast.LENGTH_SHORT);
                    toast1.show();
                }
            }
        });
    }
    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
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

                    Log.i("MenuActivity", "CONNECTED");

                    // preguntar si hay algun usuario nuevo

                    Log.i("Confirmación", "Wifi Activado");


                    online = true;
                    //     InternetState.setText("Conectado");

                } else {

                    online = false;

                }
            } else {

                online = false;
                Log.i("MenuActivity", "DISCONNECTED");
            }

        }catch (Exception e){
            Log.i("Error de internet", e.toString());
        }
    }


}
