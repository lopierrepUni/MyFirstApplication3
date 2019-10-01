package com.example.myfirstapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.example.myfirstapplication.RoomDB.MyAppDatabase;
import com.example.myfirstapplication.RoomDB.UserLocHistDB;


import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;


import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MainActivity extends LoginActivity/* ANTES TENIA ESTO ENVEZ DE LOGINACTIVITY AppCompatActivity*/   implements View.OnClickListener {

    public MapView osm;
    public MapController mc;
    public LocationManager ubicacion;
    public ArrayList<User> users = new ArrayList<User>();
    private User selectedUser;
    Button bFIni, bFFin, bHIni, bHFin, bLocsHist,bLimpiarHist;
    private int iDia, iMes, iAno, iHora, iMinutos, fDia, fMes, fAno, fHora, fMinutos;
    public double lat=10.882873605005443, longi=-75.08137609809637;
    private long time;
    Date iDate = new Date();
    Date fDate = new Date();
    private boolean sfi, shi, sff, shf; // Swtiches para revisar que se selecciono las fechas y horas iniciales y finales para la busqueda en el historial de posiciones
    public Location loc;
    public boolean online = false, gpsOn=false;;
    public TextView InternetState, GPSState;
    private MyLocationNewOverlay mLocationOverlay;
    ArrayList<String> listOfMessages = new ArrayList<>();
    private static final String TAG = "MainActivity";
    private static ConnectivityManager manager;
    private String cambio;
    public Marker myMarker;
    public User yo;
    private ArrayList<ArrayList> locHistArray;
    public GPSManager gpsStatus;
    public MyAppDatabase db;
    long fechaI,fechaF;
    public boolean mapOpen;
    public int i=0;
    public double x=0.2,y=0.1;
    double dx,dy;
    int id;
    String name;


    void recibirDatos(){
    Bundle extras=getIntent().getExtras();
    id = extras.getInt("id");
    name=extras.getString("user_name");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recibirDatos();
        mapOpen=true;
        cheekPermisos();
        MapConfig();
        confBotones();
        Log.i("Confirmación", "Botones configurados");
        Configuration.getInstance().setUserAgentValue(getPackageName());
        db= Room.databaseBuilder(getApplicationContext(),MyAppDatabase.class, "Historial de Posciones").allowMainThreadQueries().build();
        Log.i("Confirmacion", "Se creo el RoomDatabase");
        gpsStatus = new GPSManager(); // No estoy seguro de que pasa si lo quito, asi que mejor lo dejo
        users=new ArrayList<>();
        yo = new User(name,true);
        gpsStatus.startGPSRequesting();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder =new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(true);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                Log.i("estoy", "online1230="+online);
                if (online){
                    InternetState.setText("Conectado");
                    InternetState.setTextColor(Color.GREEN);
                    Log.i("size antes de crear 1", "size="+users.size());
                   // crearUsuariosDePrueba();// BORRAR
                    Log.i("size despues de crear 1", "size="+users.size());
                    MapConfig();
                    for (int i = 0; i < users.size() ; i++) {
                        Log.d("Confirmación","USUARIO "+i);
                        addMarker(users.get(i),false,false);
                    }
                }/*Notifico que no hay internet*/else{
                    Log.i("Confirmación", "Revision inicial de conexion="+online);
                    InternetState.setText("Desconectado");
                    InternetState.setTextColor(Color.RED);
                }
                if (gpsStatus.GpsOn()) {
                    // ESTO DEBERIA SER ASYNCRONO
                    addMarker(yo,true,false);
                    users.add(0,yo);

                    Log.i("Usuario Creado", "4");
                    if (!online){
                        yo.setLoc(loc);
                        db.myDao().add(new UserLocHistDB(yo.getTime(), yo.getLatitude(), yo.getLongitude()));
                        addMarker(users.get(0),true,false);
                    }/*Guardo su pos en el Room DB*/else {
                        //CONSUMO EL WS
                        // Crear usuario con nombre, y estado
                        //Crear location con time, lat y longi y añadir al usuario creado
                        //Añadir al usuario a la lista de usuarios
                       // Log.i("Confirmacion:","entra al gps como si estuviera on");
                    }//Añado a los usuarios que consigo atravez del ws
                }/*Creo el user y lo pongo en el mapa, en caso de estar offline guardo su pos en el Room DB*/else{
                    GPSState.setTextColor(Color.RED);
                    /*builder.setTitle("GPS Desactivado");
                    builder.setMessage("Por favor active el GPS para ver su posicion y la de los demas usuarios");
                    builder.show();*/
                }//Notifico que el GPS no esta activado

            }
        },2000); //Funciona
        Log.d("ConfBotnoes OK", "");
    }

    private void confBotones() {

        GPSState = (TextView) findViewById(R.id.gpsState);
        InternetState = (TextView) findViewById(R.id.internetState);
        bLimpiarHist=(Button) findViewById(R.id.cleanHist);
        bFIni=(Button) findViewById(R.id.bFechaIni);
        bFFin=(Button) findViewById(R.id.bFechaFin);
        bHIni=(Button) findViewById(R.id.bHoraIni);
        bHFin=(Button) findViewById(R.id.bHoraFin);
        bLocsHist=(Button) findViewById(R.id.bLocsHist);
        bFIni.setOnClickListener(this);
        bFFin.setOnClickListener(this);
        bHIni.setOnClickListener(this);
        bHFin.setOnClickListener(this);
        bLimpiarHist.setOnClickListener(this);
        bLocsHist.setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(final View view) {
        final Calendar iCalendar =Calendar.getInstance();
        final Calendar fCalendar =Calendar.getInstance();
        if (view==bLimpiarHist){
            limpiarHist();
        }
        if(view==bFIni || view==bFFin){
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    Log.d("Confirmación","Entro al calendario");
                    if (view==bFIni) {
                        iAno=i;
                        iMes=i1;
                        iDia=i2;
                        bFIni.setText("Fecha inicial: "+i + "/" + (i1 + 1) + "/" + i2);
                        Log.d("Confirmación","Seleccionada la fecha inicial");
                        sfi=true;
                    }else{
                        if (view==bFFin){
                            fAno=i;
                            fMes=i1;
                            fDia=i2;
                            bFFin.setText("Fecha final: "+i + "/" + (i1 + 1) + "/" + i2);
                            Log.d("Confirmación","Seleccionada la fecha final");
                            sff=true;
                        }
                    }
                }
            }
            ,iDia,iMes,iAno);
            datePickerDialog.show();
        }
        if(view==bHIni||view==bHFin){
            TimePickerDialog timePickerDialog=new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                    if (view == bHIni) {
                        iHora=i;
                        iMinutos=i1;
                        bHIni.setText("Hora inicial: " + i + ":" + i1);
                        Log.d("Confirmación", "Seleccionada la hora inicial");
                        shi=true;
                    } else {
                        if (view == bHFin) {
                            fHora=i;
                            fMinutos=i1;
                            bHFin.setText("Hora Final: " + i + ":" + i1);
                            Log.d("Confirmación", "Seleccionada la hora final");
                            shf=true;
                        }
                    }
                }
                },iHora,iMinutos,true);
            timePickerDialog.show();


        }
        if(view==bLocsHist){
            AlertDialog.Builder builder =new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(true);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            Log.d("Online== ", String.valueOf(online));
            if (selectedUser == null) {
                builder.setTitle("Faltan datos");
                builder.setMessage("Seleccione un usuario");
                builder.show();
            }else{
                Log.i("Nombre Usuarios","mi nombre: "+yo.getName()+" nombre del usuario seleccionado: "+selectedUser.getName());
                if (!online && !selectedUser.getName().equals(yo.getName())){
                  builder.setTitle("Estas desconectado");
                  builder.setMessage("Por favor conectese a una red con acceso a internet para ver el historial de otros usuarios");
                  builder.show();
                }else {
                    if (!sfi) {
                        builder.setTitle("Faltan datos");
                        builder.setMessage("Seleccione la fecha inicial");
                        builder.show();
                    } else {
                        if (!sff) {
                            builder.setMessage("Seleccione la fecha final");
                            builder.show();
                        } else {
                            if (!shi) {
                                builder.setMessage("Seleccione la hora inicial");
                                builder.show();
                            } else {
                                if (!shf) {
                                    builder.setMessage("Seleccione la hora final");
                                    builder.show();
                                } else {
                                    iCalendar.set(iAno, iMes, iDia, iHora, iMinutos);
                                    fCalendar.set(fAno, fMes, fDia, fHora, fMinutos);
                                    fechaI=iCalendar.getTimeInMillis();
                                    fechaF=fCalendar.getTimeInMillis();
                                    startThreadMarcarHist(); // Inicia el marcado de pos historicas
                                    Date iDate = new Date(fechaI);
                                    Date fDate = new Date(fechaF);
                                    SimpleDateFormat sdfDate1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
                                    String formDateI = sdfDate1.format(iDate);
                                    String formDateF = sdfDate1.format(fDate);
                                    Log.i("Formato fecha", "Fecha inicial: "+formDateI);
                                    Log.i("Formato fecha", "Fecha final: "+formDateF);

                                }
                            }
                        }
                    }
                }
            }
        }
      //  InfoPopUp.instantiate(this,"");
    }

    public void marcarLocsHist(final User user, long fechaI, long fechaF){
        Double latitud,longitud;
        long time;
        ArrayList<User> userLocHist=new ArrayList<>();
        limpiarHist();
        Log.i("Usuarios: ", "Selected"+String.valueOf(selectedUser)+", "+yo.getName());

        if (selectedUser.getId()==yo.getId()&& !online){
                Log.i("online: ", "Tamaño del dao"+String.valueOf(db.myDao().getAll().size()));
                for (int i = 0; i < db.myDao().getAll().size(); i++) {
                   time=Long.parseLong((db.myDao().getAll().get(i).getTime()));
                    Log.i("Tiempos", "tiempos:: "+String.valueOf(time)+", "+String.valueOf(fechaI)+", "+String.valueOf(fechaF));
                    if (time>=fechaI&&time<=fechaF) {
                        latitud = Double.parseDouble(db.myDao().getAll().get(i).getLatitude());
                        longitud = Double.parseDouble((db.myDao().getAll().get(i).getLongitude()));
                        Location loc = new Location("");
                        loc.setTime(time);
                        loc.setLongitude(longitud);
                        loc.setLatitude(latitud);
                        User s = new User(selectedUser.getName(), loc, false);
                        userLocHist.add(s);
                    }
                }
                Log.i("Tamaño de userLocHist", "el tamaño es: "+userLocHist.size());
            }else {
                dx=0;
                dy=0;
                for (int i = 0; i < 10; i++) {
                    userLocHist.add((crearLocHist(selectedUser)));
                } //QUITAR AL CONSUMIR EL WERB SERVICE
              //  Log.i("Fecha inicial")
                //CONSUMIR EL WEBSERVICES MANDNADO LAS VARIABLES DE TIEMPO Y RECIBIR  EL JSON CON EL ARRAY DE ARRAYS DE TIME, LAT, LONG
                // CREAR EL USUARIO CON EL NOMBRE DEL SELECTED USER Y LOS DATOS QUE RETORNA EL JSON
            }
            for (int i = 0; i < userLocHist.size(); i++) {
                Log.d("valor de i","el valor de i = "+String.valueOf(i));
                addMarker(userLocHist.get(i),false,true);
            }

       }

    public void addMarker(final User user, boolean soyYo, boolean hist){
        try {
            Log.i("Confirmacion: oms= ", "osm=" + osm.toString());
            Marker marker = new Marker(osm);
            marker.setPosition(new GeoPoint(user.getLoc()));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            osm.invalidate(); //Esto es para que se actualice mi marker en el mapa
            if (hist){
                marker.setIcon(getResources().getDrawable(R.drawable.pos_azul));
            }else {
                if (soyYo) {
                    marker.setIcon(getResources().getDrawable(R.drawable.pos_amarilla));
                    myMarker = marker;
                } else {
                    if (user.conectado) {
                        marker.setIcon(getResources().getDrawable(R.drawable.pos_verde));
                    } else {
                        marker.setIcon(getResources().getDrawable(R.drawable.pos_roja));
                    }
                }
                marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker, MapView mapView) {
                        marker.showInfoWindow();
                        Log.i("Confirmación: ", "InfoWindow abierta");
                        selectedUser = new User(user.getName(),user.getLoc(), false);
                        Log.i("Confirmación: ", "Usuario Seleccionado");
                        Log.i("Confirmación: ", "nombre del usuairo = "+selectedUser.getName());
                        return false;
                    }
                });
            }
            Log.d("Confirmación: ", "Añadido OnMarkerCickListener");
            Log.d("Confirmación: ", "Conectividad revisada");
            marker.setTitle(user.getName());
            Date date = new Date(user.getLoc().getTime());
            marker.setSnippet(user.getLoc().getLatitude() + ", " + user.getLoc().getLongitude() + "\n" + "At: " + date.toString());
            //"At: "+date.toString()

            Log.d("Confirmación: ", "Añadida info de usario al InfoWindow");
            if (soyYo) {
                osm.getOverlays().add(0, marker);
            } else {
                osm.getOverlays().add(marker);

            }
        }catch (Exception e){
            Log.e("Error en addMarker",e.toString());
        }
    }

    public  void MapConfig(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }

        osm = (MapView) findViewById(R.id.mapView);
        osm.setTileSource(TileSourceFactory.MAPNIK);
        osm.setBuiltInZoomControls(true);
        osm.setMultiTouchControls(true);
        osm.invalidate();

        mc = (MapController) osm.getController();

        mc.setZoom(12);

    }

    public Location myPos(){
        boolean permisoGPS=true;

            while(permisoGPS) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
                    }, 1000);
                } else {
                    permisoGPS=false;
                    while (loc == null) {
                        Log.i("Confirmación", "13");
                        ubicacion = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        ubicacion.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 50, gpsStatus, Looper.getMainLooper());
                        ubicacion.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 50, gpsStatus, Looper.getMainLooper());
                        loc = ubicacion.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        loc = ubicacion.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        Looper.getMainLooper();

                        //ENVIAR MI LOC A LA BASE DE DATOS
                        for (int j = 0; j <100 ; j++) {}
                    }

                    longi = loc.getLongitude();
                    lat = loc.getLatitude();
                    time = loc.getTime();

                    return loc;
                }
            }
            return loc;
    }

    public void revisarCambioskstartThread() {
        revisarCambiosThread e = new revisarCambiosThread();
        e.start();
    }
    class revisarCambiosThread extends Thread {
        @Override
        public void run() {
            InternetState = (TextView) findViewById(R.id.internetState);
            while (true) {
                // REVISAR SI HUBO CAMIOS EN LA BD, YA SEA UNA POSICION O ALGUN USARIO NUEVO
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startThreadMarcarHist() {
        MarcarHiststartThread e = new MarcarHiststartThread();
        e.start();
    }
    class MarcarHiststartThread extends Thread {
        @Override
        public void run() {

            marcarLocsHist(selectedUser,  fechaI,fechaF);
            Thread.interrupted();

        }
    } //USAR ESTE HILO PARA MARCAR LAS POS HISTORICAS EN EL MAPA SIN QUE SE CONGELE EL APP

    void cheekPermisos(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1000);
        }

    }

    void limpiarHist(){
        Log.i("Tamaño osm.getoverlays", "El tamaño de users es: "+String.valueOf(users.size()));
        Log.i("Tamaño osm.getoverlays", "El tamaño osm.overlasy es: "+String.valueOf(osm.getOverlays().size()));
        if (users.size()<osm.getOverlays().size()) {
            int inicio = osm.getOverlays().size() - 1;
            for (int i = inicio; i >= users.size(); i--) {
                osm.getOverlays().remove(i);
            }
        }
    }

    public class GPSManager implements LocationListener{

        private boolean GpsOn() {
            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            System.out.println("Provider contains=> " + provider);
            if (provider.contains("gps") || provider.contains("network")){
                GPSState.setTextColor(Color.GREEN);
                if (!gpsOn) {

                    //regLoc();
                }
                Log.i("Confirmacion","GPS activado");
                gpsOn=true;
                return true;
            }
            return false;
        }

        public void startGPSRequesting() {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            ubicacion = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            ubicacion.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 50, gpsStatus, Looper.getMainLooper());
            ubicacion.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 50, gpsStatus, Looper.getMainLooper());

            loc = ubicacion.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            loc = ubicacion.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            mc.animateTo(new GeoPoint(loc));

        }

        @Override
        public void onLocationChanged(Location location) {
            Log.i("Conf1","Cambio pos");
            Log.i("Conf1","yo:  "+gpsOn);

            if (gpsOn) {
                Log.i("Conf1","yo:  "+yo);
                if (yo != null) {
                    yo.setLoc(location);
                    //yo.getLoc().setLatitude(location.getLatitude()+x ); // Es solo para probar, QUITAR AL FINAL
                    //yo.getLoc().setLongitude(location.getLongitude()+y); // Es solo para probar, QUITAR AL FINAL
                    osm.getOverlays().remove(myMarker);
                    addMarker(users.get(0), true,false);
//                    Log.i("Coordenadas ", i + ": " + String.valueOf(loc.getLatitude()) + ", " + String.valueOf(loc.getLongitude()) + ", At: " + String.valueOf(loc.getTime()));
                    i++;
                    y = x+0.1;
                    x = y+0.2;
                    if (!online) {
                        Log.d("Guardar posicion", "antes");
                        db.myDao().add(new UserLocHistDB(yo.getTime(), yo.getLatitude(), yo.getLongitude()));
                        Log.i("Guardar posicion", "pos añadida, tamaño de la db:" + String.valueOf(db.myDao().getAll().size()));
                    } else {
                        Log.i("Guardar posicion", "entro al else");
                        //CONSUMIR WEB SERVICE ENVIAR ID, NAME, ALTI, LONG, TIME
                    }
                    Log.d("Onlineeee", String.valueOf(online));
                }
            }
        }
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}
        @Override
        public void onProviderEnabled(String s) {

            /*TODOESTO DEBERIA SER ASINCRONO*/
             new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i("Confirmacion","GPS activado");

                    Log.i("GPS Activado","GPS Activado00000");
                    GPSState.setTextColor(Color.GREEN);

//                    localizaciones();
                    osm.getOverlays().remove(myMarker);
                    yo.setLoc(myPos());
                    addMarker(yo, true, false);
Log.i("num marcas", String.valueOf(osm.getOverlays().size()));
                    mc.animateTo(new GeoPoint(yo.getLoc()));
                    mc.setZoom(12);
                    Log.i("GPS Activado","Latitud "+lat+" longitud "+longi);

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
                    mc.animateTo(new GeoPoint(yo.getLoc()));
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
            //CONSUMIR WS DE LOGOUT
            if (mapOpen) {
                Log.i("Confirmacion", "GPS desactivado");

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
                    revisarCambioskstartThread();
                    users=new ArrayList<>();
                    users.add(yo);
                    Log.i("size antes de crear 2", "size="+users.size());
                    crearUsuariosDePrueba();// BORRAR
                    Log.i("size despues de crear 2", "size="+users.size());
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
        }catch (Exception e){
            Log.i("Error de internet", e.toString());
        }
    }

    public User crearLocHist(User user){
        User s=new User(user.getName(),user.getLoc(), false);
        double lat=Double.parseDouble(user.getLatitude()),longi=Double.parseDouble(user.getLongitude());
        long time=loc.getTime();
        Log.d("Locs Hist","");
        Location loc2=new Location("");
        loc2.setLongitude(longi-dx);
        loc2.setLatitude(lat-dy);
        loc2.setTime(time);
        s.setLoc(loc2);
        dx=dx+0.1;
        dy=dy+0.2;
        return s;
    } // BORRAR

    public void crearUsuariosDePrueba(){

        Log.i("CReados","Usiarios de prueba creados");
        Location loc2=new Location("");
        loc2.setLongitude(longi+0.3);
        loc2.setLatitude(lat+0.3);
        loc2.setTime(time);
        Location loc3=new Location("");

        loc3.setLongitude(longi-0.2);
        loc3.setLatitude(lat+0.2);
        loc3.setTime(time);

        User us0 = new User("2",loc2,false);
        User us1 = new User("3",loc3,true);
        users.add(us0);
        users.add(us1);

    } //BORRAR
























    //COSAS QUE NO USO PERO TENIA EL PROFESOR
/*    public Location initializeOSM(){
        try{
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    !=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.
                                        WRITE_EXTERNAL_STORAGE},1002);

            }
            Context ctx = getApplicationContext();
            Configuration.getInstance().load(ctx,
                    PreferenceManager.
                            getDefaultSharedPreferences(ctx));
            osm = (MapView) findViewById(R.id.mapView);
            osm.setTileSource(TileSourceFactory.MAPNIK);
            this.mLocationOverlay =
                    new MyLocationNewOverlay(
                            new GpsMyLocationProvider(
                                    this),osm);
            this.mLocationOverlay.enableMyLocation();
            osm.getOverlays().add(this.mLocationOverlay);

            Location loc=new Location(String.valueOf(mLocationOverlay.getMyLocation()));
            Log.d("Valor:mLocationOverlay:",String.valueOf(loc.getLongitude()));
          //  loc.setLatitude(this.mLocationOverlay.getMyLocation().getLatitude());
            //loc.setLongitude(this.mLocationOverlay.getMyLocation().getLongitude());

            return loc;
        }catch (Exception error){
            Toast.makeText(this,error.getMessage(),Toast.LENGTH_SHORT).show();
return null;
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            mapOpen=false;
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
        } else if (id == R.id.nav_slideshow) {
        } else if (id == R.id.nav_tools) {
        } else if (id == R.id.nav_share) {
        } else if (id == R.id.nav_send) {
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    }

    @Override
    public void needPermissions() {
        this.requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                1001);
    }


    @Override
    public void locationHasBeenReceived(final Location location) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {



            }
        });
    }

    @Override
    public void gpsErrorHasBeenThrown(Exception error) {

    }

*/
}
