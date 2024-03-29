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
import android.icu.text.TimeZoneNames;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;

import com.example.myfirstapplication.Chat.ChatActivity;
import com.example.myfirstapplication.RoomDB.MyAppDatabase;
import com.example.myfirstapplication.RoomDB.UserLocHistDB;


import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;


import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.room.Room;

import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends LoginActivity/* ANTES TENIA ESTO ENVEZ DE LOGINACTIVITY AppCompatActivity*/   implements View.OnClickListener {

    public MapView osm;
    public MapController mc;
    public LocationManager ubicacion;
    public ArrayList<User> users = new ArrayList<User>();
    private User selectedUser;
    Button bFIni, bFFin, bHIni, bHFin, bLocsHist,bLimpiarHist, bChat;
    private int iDia, iMes, iAno, iHora, iMinutos, fDia, fMes, fAno, fHora, fMinutos;
    public double lat=10.882873605005443, longi=-75.08137609809637;
    private long time;
    Date iDate = new Date();
    Date fDate = new Date();
    private boolean sfi, shi, sff, shf; // Swtiches para revisar que se selecciono las fechas y horas iniciales y finales para la busqueda en el historial de posiciones
    public Location loc;
    public boolean online = false, gpsOn=false;;
    public TextView InternetState, GPSState,vel,dist;
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
    String id;
    boolean permisos=false;
    String name;
    int esperar;
    List<UserLocHistDB> myLocs;
    String myTime, myLati,myLongi;
    ArrayList<User> userLocHist=new ArrayList<>();
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

        users=new ArrayList<>();
        //yo = new User(name,name,true);
        if (permisos){
            esperar=3000;
        }else{
        esperar=5000;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                gpsStatus = new GPSManager(); // No estoy seguro de que pasa si lo quito, asi que mejor lo dejo
                gpsStatus.startGPSRequesting();
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
                    //agregarUsuariosStartThread();
                    InternetState.setText("Conectado");
                    InternetState.setTextColor(Color.GREEN);
                }else{
                    InternetState.setText("Desconectado");
                    InternetState.setTextColor(Color.RED);
                }
                Log.i("Usuario Creado123", "Usuario Creado123"+ String.valueOf(gpsStatus.GpsOn()));
                if (gpsStatus.GpsOn()) {
                    // ESTO DEBERIA SER ASYNCRONO
                    yo = new User(name,name,true);
                    while(loc==null){}
                    yo.setLoc(loc);
                    addMarker(yo,true,false);
                    users.add(0,yo);
                    Log.i("Usuario Creado", " Creado 4");
                    if (!online) {
                        yo.setLoc(loc);
                        db.myDao().add(new UserLocHistDB(yo.getTime(), yo.getLatitude(), yo.getLongitude()));
                     }else {
                        Log.i("Usuario Creado", " Creado 5");
                        Log.i("Usuario Creado", " Creado 3"+loc);
                        Calendar c =Calendar.getInstance();
                        c.setTimeInMillis(loc.getTime());
                        Date date = new Date(c.getTimeInMillis());
                        SimpleDateFormat sdfDate1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
                        sdfDate1.setTimeZone(TimeZone.getTimeZone("America/Bogota"));

                        String fecha = sdfDate1.format(date);
                        Log.i("Usuario Creado", " Creado 31"+loc.getLatitude());
                        //mandarPosStartThread(fecha , String.valueOf(loc.getLatitude()),String.valueOf(loc.getLongitude()));  /TODO Comente esto porq me marcaba dos veces la misma pos al iniciar

                    }//Añado a los usuarios que consigo atravez del ws
                }/*Creo el user y lo pongo en el mapa, en caso de estar offline guardo su pos en el Room DB*/else{
                    GPSState.setTextColor(Color.RED);
                    /*builder.setTitle("GPS Desactivado");
                    builder.setMessage("Por favor active el GPS para ver su posicion y la de los demas usuarios");
                    builder.show();*/
                }//Notifico que el GPS no esta activado

            }
        },esperar); //Funciona
        Log.d("ConfBotnoes OK", "");
    }

    void recibirDatos(){
        Bundle extras=getIntent().getExtras();
        id = extras.getString("id");
        name=extras.getString("user_name");
    }

    private void confBotones() {
        GPSState = (TextView) findViewById(R.id.gpsState);
        InternetState = (TextView) findViewById(R.id.internetState);
        vel = (TextView) findViewById(R.id.vel);
        dist = (TextView) findViewById(R.id.dist);
        bLimpiarHist=(Button) findViewById(R.id.cleanHist);
        bFIni=(Button) findViewById(R.id.bFechaIni);
        bFFin=(Button) findViewById(R.id.bFechaFin);
        bHIni=(Button) findViewById(R.id.bHoraIni);
        bHFin=(Button) findViewById(R.id.bHoraFin);
        bLocsHist=(Button) findViewById(R.id.bLocsHist);
        bChat=(Button) findViewById(R.id.bChat);
        bFIni.setOnClickListener(this);
        bFFin.setOnClickListener(this);
        bHIni.setOnClickListener(this);
        bHFin.setOnClickListener(this);
        bChat.setOnClickListener(this);
        bLimpiarHist.setOnClickListener(this);
        bLocsHist.setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(final View view) {
        final Calendar iCalendar =Calendar.getInstance();
        final Calendar fCalendar =Calendar.getInstance();
        if (view==bLimpiarHist) {
            limpiarHist();
        }
        if (view==bChat){
            Intent intetToBecalled=new Intent(getApplicationContext(), ChatActivity.class);
            intetToBecalled.putExtra("user_name", id);
            startActivity(intetToBecalled);

        }else{
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
                                    Log.i("sadsad","Fecha 1"+fechaI);
                                    Log.i("sadsad","Fecha 2"+fechaF);
                                    MarcarHiststartThread(); // Inicia el marcado de pos historicas
                                    Date iDate = new Date(fechaI);
                                    Date fDate = new Date(fechaF);
                                    SimpleDateFormat sdfDate1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
                                    sdfDate1.setTimeZone(TimeZone.getTimeZone("America/Bogota"));
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
        }
      //  InfoPopUp.instantiate(this,"");
    }

    public void marcarLocsHist(final User user, long fechaI, long fechaF){
        Double latitud,longitud;
        long time;
        ArrayList<User> userLocHist=new ArrayList<>();
        limpiarHist();
        Log.i("Usuarios: ", "Selected"+String.valueOf(selectedUser)+", "+yo.getName());

        if (selectedUser.getName()==yo.getName()&& !online){
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
               for (int i = 0; i < userLocHist.size(); i++) {
                     Log.d("valor de i","el valor de i = "+String.valueOf(i));
                     addMarker(userLocHist.get(i),false,true);               }
                Log.i("Tamaño de userLocHist", "el tamaño es: "+userLocHist.size());
            }else {
                dx=0;
                dy=0;
             /*   for (int i = 0; i < 10; i++) {
                    userLocHist.add((crearLocHist(selectedUser)));
                }*/ //QUITAR AL CONSUMIR EL WERB SERVICE
              //  Log.i("Fecha inicial")

            Date iDate = new Date(fechaI);
            SimpleDateFormat sdfDate1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
            sdfDate1.setTimeZone(TimeZone.getTimeZone("America/Bogota"));
            String sFechaI = sdfDate1.format(iDate);
            Date fDate = new Date(fechaF);
            String sFechaF = sdfDate1.format(fDate);
            sdfDate1.setTimeZone(TimeZone.getTimeZone("America/Bogota"));
            historialOnlineStartThread(selectedUser,sFechaI,sFechaF);

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
        Log.i("Tamaño osm.getoverlays", "Mpa config "+String.valueOf(osm.getOverlays().size()));

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

    void cheekPermisos(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1000);
        }else{
            permisos=true;
        }

    }

    void limpiarHist(){
        Log.i("Tamaño osm.getoverlays", "El tamaño de users es: "+String.valueOf(users.size()));
        Log.i("Tamaño osm.getoverlays", "El tamaño osm.overlasy es: "+String.valueOf(osm.getOverlays().size()));
        for (int j = 0; j < users.size(); j++) {
            for (int k = 0; k < userLocHist.size(); k++) {
                if (users.get(j).equals(userLocHist.get(k))){
                    users.remove(userLocHist.get(k));
                }
            }
        }
        userLocHist.clear();
        if (users.size()<osm.getOverlays().size()) {
            int inicio = osm.getOverlays().size() - 1;
            for (int i = inicio; i >= users.size(); i--) {
                osm.getOverlays().remove(i);
            }
        }
        osm.invalidate();
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

            }else{
                gpsOn=false;
            }
            return gpsOn;
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
            while(loc==null){
                loc = ubicacion.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                loc = ubicacion.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            GPSState.setTextColor(Color.GREEN);
            mc.animateTo(new GeoPoint(loc));

        }

        @Override
        public void onLocationChanged(Location location) {
            Log.i("Conf1","Cambio pos "+ location.getLatitude()+ ", "+location.getLongitude());
            Log.i("Conf1","yo:  "+gpsOn);
            GPSState.setTextColor(Color.GREEN);
            if (gpsOn) {
                Log.i("Conf1","yo:  "+yo);
                if (yo != null) {
                    yo.setLoc(location);
                    //yo.getLoc().setLatitude(location.getLatitude()+x ); // Es solo para probar, QUITAR AL FINAL
                    //yo.getLoc().setLongitude(location.getLongitude()+y); // Es solo para probar, QUITAR AL FINAL
                    Log.i("ConfLocChange","ConfLocChange Location"+location.toString());
                    Log.i("ConfLocChange","ConfLocChange osm.getoverlays.size 1= "+osm.getOverlays().size());
                    osm.getOverlays().remove(myMarker);
                    Log.i("ConfLocChange","ConfLocChange osm.getoverlays.size 2= "+osm.getOverlays().size());
                    addMarker(yo, true,false);
//                   Log.i("Coordenadas ", i + ": " + String.valueOf(loc.getLatitude()) + ", " + String.valueOf(loc.getLongitude()) + ", At: " + String.valueOf(loc.getTime()));
                    i++;
                    y = x+0.1;
                    x = y+0.2;
                    Date iDate = new Date(location.getTime());
                    SimpleDateFormat sdfDate1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
                    sdfDate1.setTimeZone(TimeZone.getTimeZone("America/Bogota"));
                    String time = sdfDate1.format(iDate);
                    String lati=String.valueOf(location.getLatitude());
                    String longi=String.valueOf(location.getLongitude());
                    if (!online) {
                        Log.d("Guardar posicion", "los datos son time= "+time+" lati= "+lati+" longi "+longi);
                        db.myDao().add(new UserLocHistDB(time, lati,longi));
                        Log.i("Guardar posicion", "pos añadida, tamaño de la db:" + String.valueOf(db.myDao().getAll().size()));
                    } else {
                        Log.i("Guardar posicion", "entro al else");


                        mandarPosStartThread(time,lati,longi);
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
            Log.i("MenuActivity", "dice que el gpsOn es "+gpsOn);


            if (networkInfo != null) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    users=new ArrayList<>();
                    users.add(yo);

                    agregarUsuariosStartThread();
                    //crearUsuariosDePrueba();// BORRAR

                    Log.i("MenuActivity", "CONNECTED");
                    toast1 = Toast.makeText(getApplicationContext(), "Conectado", Toast.LENGTH_SHORT);
                    InternetState.setTextColor(Color.GREEN);

                    InternetState.setText("Conectado");

                    revisarCambiosStartThread();
                    Log.i("Confirmación", "Wifi Activado");
                    Log.i("Confirmación", "online= "+online);

                    if (!online){
                        myLocs = db.myDao().getAll();
                        Log.d("NOTAAA", String.valueOf(db.myDao().getAll()));
                        Log.d("Lista: ", "");
                        if (myLocs!=null && myLocs.size()>0) {
                            Log.d("ConfirmacionImpor"," entro al for");
                            Log.d("ConfirmacionImpor","tam Mylocs= "+myLocs.size());

                            for (int j = 0; j < myLocs.size(); j++) {
                                Log.d("Elemento: ", j + ": " + myLocs.get(j));
                                String time,lati,longi;
                                Log.d("ConfirmacionImpor: ", "conf1");
                                Log.d("ConfirmacionImpor: ", "time= "+myLocs.get(j).getTime());

                                time = myLocs.get(j).getTime();
                                Log.d("ConfirmacionImpor: ", "conf2");

                                lati=myLocs.get(j).getLatitude();
                                longi=myLocs.get(j).getLongitude();
                                Log.i("ConfirmacionImpor: ","roomdb a dbe longi= "+longi);
                                Log.i("ConfirmacionImpor: ","roomdb a dbe lat= "+lati);
                                Log.i("ConfirmacionImpor: ","roomdb a dbe time= "+time);

                                mandarPosStartThread(time, lati,longi);
                            }
                            db.myDao().deleteTable();
                            Log.i("ConfirmacionImpor: ","tamaño de la roomdb despues de conectarse "+db.myDao().getAll().size());
                        }

                    }
                    online = true;
                    //     InternetState.setText("Conectado");

                } else {
                    InternetState.setText("Desconectado");
                    InternetState.setTextColor(Color.RED);
                    logOutStartThread();
                    online = false;
                    Log.i("MenuActivity", "DISCONNECTED");
                    toast1 = Toast.makeText(getApplicationContext(), "Desconectado", Toast.LENGTH_SHORT);
                }
            } else {
                InternetState.setText("Desconectado");
                InternetState.setTextColor(Color.RED);
                logOutStartThread();
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

    public void MarcarHiststartThread() {
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

    public void mandarPosStartThread(String time, String lati, String longi) {
        mandarPosThread e = new mandarPosThread(time,lati,longi);
        e.start();
    }
    class mandarPosThread extends Thread {
        String time, lati, longi;
        public mandarPosThread(String time, String lati, String longi){
            this.time=time;
            this.lati=lati;
            this.longi=longi;
        }

        String respuesta;
        @Override
        public void run() {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://10.0.2.2:8080/WebSer/webresources/generic/addposition?id="+id+"&dateTime="+time+"&latitud="+lati+"&longitud="+longi)
                    .get()
                    .build();
            try {
                Log.i("response: ", "entra al try");
                Response response = client.newCall(request).execute();
                respuesta = response.body().string();
                //CON ESTA RESPUESTA NO HAGO NADA
            } catch (IOException e) {
                Log.i("error: ", "error en el ws catch " + e);
                e.printStackTrace();
            }
            //users.add(respuesta);
            Log.i("los demas usuarios",respuesta);


        }
    }

    public void historialOnlineStartThread(User user,String fechaI, String fechaF) {
        historialOnlineThread e = new historialOnlineThread(user,fechaI,  fechaF);
        e.start();
        distanciaVelocidadThread e1 = new distanciaVelocidadThread(user,fechaI,  fechaF);
        e1.start();
    }
    class distanciaVelocidadThread extends Thread {
        String respuesta;
        String fechaI, fechaF;
        User user;
        public distanciaVelocidadThread(User user,String fechaI, String fechaF){
            this.user=user;
            this.fechaI=fechaI;
            this.fechaF=fechaF;
        }
        @Override
        public void run() {
            Log.i("error: ", "el id para logout es" + id);

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://10.0.2.2:8080/WebSer/webresources/generic/getSpeedAndMeters?id="+String.valueOf(user.getName())+"&dateTime1="+fechaI+"&dateTime2="+fechaF)
                    .get()
                    .build();
            try {
                Log.i("response: ", "entra al try");
                Response response = client.newCall(request).execute();
                while(response==null){}
                Looper.prepare();
                respuesta = response.body().string();
                Log.i("error: ", "la respuesta de velocidad " + respuesta);

                String[] vr =respuesta.split("#");
                vel.setText("Velocidad promedio: "+String.valueOf(vr[1])+ " m/s");
                dist.setText("Distancia recorrida: "+String.valueOf(vr[0])+ " m");

            } catch (IOException e) {
                Log.i("error: ", "error en el ws" + e);
                e.printStackTrace();
            }


        }

    }
    class historialOnlineThread extends Thread {
        String respuesta;
        String fechaI, fechaF;
        User user;
        public historialOnlineThread(User user,String fechaI, String fechaF){
            this.user=user;
            this.fechaI=fechaI;
            this.fechaF=fechaF;
        }
        @Override
        public void run() {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://10.0.2.2:8080/WebSer/webresources/generic/searchbetweendates?id="+String.valueOf(user.getName())+"&dateTime1="+fechaI+"&dateTime2="+fechaF)
                    .get()
                    .build();
            try {
                Response response = client.newCall(request).execute();
                Log.i("url "," urlsss"+                request.url().toString());
                while(response==null){}
                respuesta = response.body().string();
                if (!respuesta.equals("")) {
                    Log.i("response: ", "entra al try fechaI " + fechaI);

                    Log.i("response: ", "entra al try retorna" + respuesta);
                    String[] hist = respuesta.split("!");
                   userLocHist = new ArrayList<>();
                    ArrayList<Location> locs = new ArrayList<>();

                    for (int i = 1; i < hist.length; i++) {
                        String[] locData = hist[i].split("#");
                        Double lati = Double.parseDouble(locData[0]);
                        Double longi = Double.parseDouble(locData[1]);
                        String[] fecha=locData[2].split("-");
                        String[] diaHora=fecha[2].split(" ");
                        String[] tiempo=diaHora[1].split(":");
                        Calendar c =Calendar.getInstance();
                        c.set(Integer.parseInt(fecha[0]),Integer.parseInt(fecha[1]),Integer.parseInt(diaHora[0]),Integer.parseInt(tiempo[0]),Integer.parseInt(tiempo[1]),Integer.parseInt(tiempo[2]));
                        long time= c.getTimeInMillis();
                        Location loc = new Location("");
                        loc.setLatitude(lati);
                        loc.setLongitude(longi);
                        loc.setTime(time);
                        locs.add(loc);
                        User s = new User(selectedUser.getName(), loc, false);
                        userLocHist.add(s);
                        Log.i("los demas usuarios", respuesta);
                        addMarker(s, false, true);
                    }

                }else{
                    Toast toast1 = Toast.makeText(getApplicationContext(), "Este usuario no tiene historial", Toast.LENGTH_SHORT);
                    toast1.show();
                }

            } catch (IOException e) {
                Log.i("error: ", "error en el ws catch " + e);
                e.printStackTrace();
            }


            //for{
            // WS: Aca para cada posicion se debe crear un Location y agregarsela a un usuario por cada lat y longi que se reciba

            //}
        }
    }

    public void agregarUsuariosStartThread() {
        agregarUsuariosThread e = new agregarUsuariosThread();
        e.start();
    }
    class agregarUsuariosThread extends Thread {
        String respuesta;

        @Override
        public void run() {
            users.clear();
            /*if (gpsStatus.GpsOn()){
                users.add(yo);
            }*/
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://10.0.2.2:8080/WebSer/webresources/generic/getlastpos")
                    .get()
                    .build();
            try {
                users.clear();

                Log.i("response: ", "entra al try");
                Response response = client.newCall(request).execute();
                while  (response==null){}
                respuesta = response.body().string();
                Log.i("Respuesta","los demas usuarios "+respuesta);
                String[] datos=respuesta.split("!");
                Log.i("Respuesta","Los datos= "+datos.length);
                for (int j = 1; j < datos.length; j++) {
                    String[] usuario= datos[j].split("#");
                    Log.i("Respuesta","La respuesta 0 "+usuario);
                    Log.i("Respuesta","La respuesta 0 "+usuario[0]);
                    String name=usuario[0];
                    Log.i("Respuesta","La respuesta 1 "+usuario[1]);
                    double lati=Double.parseDouble(usuario[1]);
                    Log.i("Respuesta","La respuesta 2 "+usuario[2]);
                    double longi=Double.parseDouble(usuario[2]);
                    Log.i("Respuesta","La respuesta 3 "+usuario[4]);
                    boolean on =false;
                    if  (Integer.parseInt(usuario[4])==1){
                        on =true;
                    }
                    String[] fecha=usuario[3].split("-");
                    String[] diaHora=fecha[2].split(" ");
                    String[] tiempo=diaHora[1].split(":");
                    Calendar c =Calendar.getInstance();
                    c.set(Integer.parseInt(fecha[0]),Integer.parseInt(fecha[1]),Integer.parseInt(diaHora[0]),Integer.parseInt(tiempo[0]),Integer.parseInt(tiempo[1]),Integer.parseInt(tiempo[2]));
                    long time= c.getTimeInMillis();
                    Location userLoc=new Location("");
                    userLoc.setLatitude(lati);
                    userLoc.setLongitude(longi);
                    userLoc.setTime(time);
                    Log.i("size antes de crear 2", "sizeaaaa="+users.size());

                        User user = new User(name, userLoc, on);
                        users.add(user);
                        if(userLocHist!=null) {
                            for (int i = 0; i < userLocHist.size(); i++) {
                              //  users.add(userLocHist.get(i));
                            }
                        }
                    Log.i("size despues de crear 2", "sizeaaaa="+users.size());
                }
                respuesta=null;
                for (int j = 1; j < osm.getOverlays().size(); j++) {
                    osm.getOverlays().remove(j);
                }
                Log.i("error: ", "nombre de usuario-------------------------" );
                for (int j = 0; j < users.size(); j++) {
                    if(!users.get(j).getName().equals(id)) {
                        addMarker(users.get(j), false, false);
                        Log.i("error: ", " nombre de usuario ENTRA AL IF: " );

                    }else{
                        addMarker(users.get(j), true, false);
                        Log.i("error: ", " nombre de usuario ENTRA AL ELSE: " );

                    }
//                    Log.i("error: ", "nombre de usuario " + users.get(j).getName());
                }
                if (userLocHist.size()>0){
                    for (int j = 0; j < userLocHist.size(); j++) {
                        addMarker(userLocHist.get(j), false, true);
                    }
                }
            } catch (IOException e) {
                Log.i("error: ", "error en el ws catch " + e);
                e.printStackTrace();
            }


        }
    }

    public void revisarCambiosStartThread() {
        revisarCambiosThread e = new revisarCambiosThread();
        e.start();
    }
    class revisarCambiosThread extends Thread {
        @Override
        public void run() {
            InternetState = (TextView) findViewById(R.id.internetState);
            while (true) {
                new  revisarCambios().start();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class revisarCambios extends Thread {
        String respuesta;
        Calendar iCalendar =Calendar.getInstance();



        @Override
        public void run() {


            long fechaI=iCalendar.getTimeInMillis()-2000;
            Date iDate = new Date(fechaI);

            SimpleDateFormat sdfDate1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
            sdfDate1.setTimeZone(TimeZone.getTimeZone("America/Bogota"));
            String formDateI = sdfDate1.format(iDate);

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://10.0.2.2:8080/WebSer/webresources/generic/validatechange?dateTime="+formDateI)
                    .get()
                    .build();
            try {
                Log.i("response: ", "entra al try");
                Response response = client.newCall(request).execute();
                while(response==null){}
                respuesta = response.body().string();
                Log.i("response: ", "fecha del validateChange="+formDateI);
                Log.i("response: ", "respuesta de validateChange="+respuesta);
             //  if (respuesta.equals("true")){
                    agregarUsuariosStartThread();
               // }
                response=null;
            } catch (IOException e) {
                Log.i("error: ", "error en el ws" + e);
                e.printStackTrace();
            }
            Log.i("Respuesta de ", "revisarCambios "+respuesta);

        }
    }

    public void logOutStartThread() {
        logOutThread e = new logOutThread();
        e.start();
    }
    class logOutThread extends Thread {

            @Override
            public void run() {
                Log.i("error: ", "el id para logout es" + id);

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:8080/WebSer/webresources/generic/logout?id="+id)
                        .get()
                        .build();
                try {
                    Log.i("response: ", "entra al try");
                    Response response = client.newCall(request).execute();
                    while(response==null){}
                    Looper.prepare();
                    Toast toast1 = Toast.makeText(getApplicationContext(), "Sesión Finalizada", Toast.LENGTH_SHORT);
                    toast1.show();
                } catch (IOException e) {
                    Log.i("error: ", "error en el ws" + e);
                    e.printStackTrace();
                }


            }

        }





    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {

           // mapOpen=false;
            drawer.closeDrawer(GravityCompat.START);
        } else {

            logOutStartThread();
            super.onBackPressed();
        }
    }



















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
