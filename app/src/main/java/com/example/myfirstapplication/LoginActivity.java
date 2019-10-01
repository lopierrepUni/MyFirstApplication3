package com.example.myfirstapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.room.Room;

import com.example.myfirstapplication.RoomDB.MyAppDatabase;
import com.example.myfirstapplication.RoomDB.UserLocHistDB;
import com.example.myfirstapplication.RoomDB2.usuarioConocido2;
import com.example.myfirstapplication.RoomDB2.usuarioConocidoDB;

import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends Activity {
Button loginB,registerB;
boolean online;
public int id;
    public usuarioConocido2 db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        loginB=(Button) findViewById(R.id.login_button);
        registerB=(Button) findViewById(R.id.register_button);
        db= Room.databaseBuilder(getApplicationContext(), usuarioConocido2.class, "Usuarios registrados").allowMainThreadQueries().build();

        setContentView(R.layout.login_activity_layout);
        ((Button)findViewById(R.id.login_button)). setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                id = 10;

                Intent intetToBecalled = new Intent(getApplicationContext(), MainActivity.class);
                String usuario=((EditText)findViewById(R.id.login_user_name)).getText().toString();
                String contraseña=((EditText)findViewById(R.id.login_password)).getText().toString();
                intetToBecalled.putExtra("user_name", usuario);
                intetToBecalled.putExtra("user_password", contraseña);
                intetToBecalled.putExtra("id", id);
                Log.i("Login", "LOGEADO00");
                if (online) {
                    if (true /*AQUI VA EL WS PARA SABER SI EL USUARIO ES VALIDO EN CASO DE TENER ACCESO A INTERNET */) {
                        Toast toast1 =
                                Toast.makeText(getApplicationContext(),
                                        "Bienvenido camarada", Toast.LENGTH_SHORT);

                        toast1.show();
                        startActivity(intetToBecalled);

                    } else {
                        Toast toast1 =
                                Toast.makeText(getApplicationContext(),
                                        "Usuario y/o contraseña incorrectos", Toast.LENGTH_SHORT);

                        toast1.show();
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

                        Toast toast1 =
                                Toast.makeText(getApplicationContext(),
                                        "Usuario y/o contraseña incorrectos", Toast.LENGTH_SHORT);

                        toast1.show();
                    }

                }
            }
        });
        ((Button)findViewById(R.id.register_button)). setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                id=10;

                Intent intetToBecalled=new Intent(getApplicationContext(),MainActivity.class);
                String usuario=((EditText)findViewById(R.id.login_user_name)).getText().toString();
                String contraseña=((EditText)findViewById(R.id.login_password)).getText().toString();
                intetToBecalled.putExtra("user_name",usuario);
                intetToBecalled.putExtra("user_password",contraseña);
                intetToBecalled.putExtra("id",id);
                Log.i("Login","LOGEADO00");
                if (true /*AQUI VA EL WS PARA SABER REGISTRAR EL USUARIO Y SABER SI EL REGISTRO FUE VALIDO*/){
                    db.usuarioConocidoDao2().add(new usuarioConocidoDB(id,usuario,contraseña));
                    int tam=db.usuarioConocidoDao2().getAll().size();
                    String us=db.usuarioConocidoDao2().getAll().get(0).getUser();
                    String pass =db.usuarioConocidoDao2().getAll().get(0).getPass();
                    Log.i( "info importante", "size "+String.valueOf(tam)+" , valor 1 "+us+ "valor 2"+pass);
                    startActivity(intetToBecalled);
                    Toast toast1 =
                            Toast.makeText(getApplicationContext(),
                                    "Registro exitoso", Toast.LENGTH_SHORT);

                    toast1.show();
                }else{

                    Toast toast1 =
                            Toast.makeText(getApplicationContext(),
                                    "Registro fallido", Toast.LENGTH_SHORT);

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
