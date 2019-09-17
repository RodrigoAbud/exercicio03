package br.com.abud.calculapercursoetempo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    private ToggleButton permissaoGPS;
    private ToggleButton statusGPS;
    private ToggleButton statusPercurso;
    private Button resetButton;
    private TextView distPerc;
    private ImageView searchButton;
    private EditText inputText;

    private Chronometer chronometer;
    private long pause;
    private boolean running;

    private double latitude;
    private double longitude;

    private double DistanciaTotal = 0;

    private Location currentLocation;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int REQUEST_PERMISSION_COD_GPS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Busca no mapa
        inputText = findViewById(R.id.inputTextId);

        searchButton = findViewById(R.id.searchButtonId);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = inputText.getText().toString();
                Uri uri = Uri.parse(getString(R.string.uri_mapa, latitude, longitude)+location);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                if(!statusPercurso.isChecked())
                    return;

                if(currentLocation == null)
                    currentLocation = location;

                DistanciaTotal += currentLocation.distanceTo(location);

                currentLocation = location;
                distPerc.setText(String.format("%.2f m", DistanciaTotal));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //Permissão para utilizar a localização

        permissaoGPS = findViewById(R.id.permissaoGPSId);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            permissaoGPS.setChecked(true);
        }else{
            permissaoGPS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this, new String[]
                                    {Manifest.permission.ACCESS_FINE_LOCATION},1001);

                }
            });
        }


        //Permissão para uso do GPS

        statusGPS = findViewById(R.id.statusGPSId);
        statusGPS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED){
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                1000,
                                10,
                                locationListener);
                        Toast.makeText(getApplicationContext(), getString(R.string.gps_enable), Toast.LENGTH_LONG ).show();
                        //else nao ta funcionando
                    }else {
                        Toast.makeText(MainActivity.this,getString(R.string.get_permission),Toast.LENGTH_LONG).show();
                        statusGPS.setChecked(false);
                    }
                }else {
                    locationManager.removeUpdates(locationListener);
                    Toast.makeText(getApplicationContext(), getString(R.string.gps_disable), Toast.LENGTH_LONG ).show();
                }

            }
        });

        //Iniciar Percurso

        //Tempo e distancia do percurso
        distPerc = findViewById(R.id.distPercValueId);
        chronometer = findViewById(R.id.tempPassValueId);
        statusPercurso = findViewById(R.id.statusPercursoId);
        statusPercurso.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && statusGPS.isChecked() && !running){
                    Toast.makeText(getApplicationContext(), getString(R.string.started_route), Toast.LENGTH_LONG ).show();

                    chronometer.setBase(SystemClock.elapsedRealtime() - pause);
                    chronometer.start();
                    running = true;


                    DistanciaTotal=0;

                }else if (isChecked && !statusGPS.isChecked()){
                    Toast.makeText(getApplicationContext(),getString(R.string.gps_must_be_enabled), Toast.LENGTH_LONG ).show();
                    statusPercurso.setChecked(false);
                }
                else {
                    Toast.makeText(getApplicationContext(),getString(R.string.finished_route), Toast.LENGTH_LONG ).show();
                    chronometer.stop();
                    pause = SystemClock.elapsedRealtime() - chronometer.getBase();
                    running = false;
                }

            }
        });

        //Resetar infos

        resetButton = findViewById(R.id.resetButtonId);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!running){
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    pause = 0;
                    distPerc.setText("");
                }else{
                    Toast.makeText(MainActivity.this,getString(R.string.course_in_progress),Toast.LENGTH_LONG).show();
                }
            }
        });

    }

}
