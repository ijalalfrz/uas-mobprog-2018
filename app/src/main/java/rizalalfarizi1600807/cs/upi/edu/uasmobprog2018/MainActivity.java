package rizalalfarizi1600807.cs.upi.edu.uasmobprog2018;

//NAMA : RIZAL ALFARIZI
//NIM : 1600807
//KELAS : ILKOM C 2016


import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import rizalalfarizi1600807.cs.upi.edu.uasmobprog2018.adapter.RvAdapter;
import rizalalfarizi1600807.cs.upi.edu.uasmobprog2018.database.SensorDB;
import rizalalfarizi1600807.cs.upi.edu.uasmobprog2018.model.BrakeDetector;


public class MainActivity extends AppCompatActivity implements SensorEventListener,View.OnClickListener,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
    private SensorManager sm;
    private Sensor linearAccl;
    private Sensor accl;
    private Sensor magneto;
    private TextView tvHasil;
    private Button clear,save,load;

    private static final int MY_PERMISSION_REQUEST = 1;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;

    @Override
    protected void onResume() {
        super.onResume();

        sm.registerListener(this, linearAccl, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sm.unregisterListener(this);
    }

    RvAdapter adapter;
    private ArrayList<BrakeDetector> data;
    private RecyclerView recV;
    private SensorDB dbSensor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildGoogleApiClient();
        sensorDetect();
        clear = (Button) findViewById(R.id.clear);
        save = (Button) findViewById(R.id.save);
        load = (Button) findViewById(R.id.load);

        clear.setOnClickListener(this);
        save.setOnClickListener(this);
        load.setOnClickListener(this);


        tvHasil = (TextView) findViewById(R.id.tv_hasil);
        recV = (RecyclerView)findViewById(R.id.rv_posisi);
        recV.setHasFixedSize(true);

        data = new ArrayList<>();

        // membuka database dan menampilkan datanya di reyclerview
        dbSensor = new SensorDB(getApplicationContext());
        dbSensor.open();
        dbSensor.deleteAllSensor();
        dbSensor.close();




        loadData();


    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        getLocation();
    }
    protected void getLocation(){
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},MY_PERMISSION_REQUEST);
            return;
        }

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

    }

    public void sensorDetect(){
        sm = (SensorManager)    getSystemService(getApplicationContext().SENSOR_SERVICE);
        linearAccl = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        accl = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneto = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sm.registerListener(this, linearAccl, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, accl, SensorManager.SENSOR_DELAY_NORMAL);
        sm.registerListener(this, magneto,SensorManager.SENSOR_DELAY_NORMAL);

        if (linearAccl != null){
            // ada sensor accelerometer!
            Toast.makeText(this,"ada sensor", Toast.LENGTH_SHORT).show();

        }
        else {
            // gagal, tidak ada sensor accelerometer.
            Toast.makeText(this,"Tidak ada sensor", Toast.LENGTH_SHORT).show();

        }

    }

    float[] mGravity;
    float[] mGeomagnetic;
    float azimuth;
    float pitch;
    float roll;


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double ax=0,ay=0,az=0;
        // menangkap perubahan nilai sensor
        if (sensorEvent.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION) {
            ax=sensorEvent.values[0];
            ay=sensorEvent.values[1];
            az=sensorEvent.values[2];
        }

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = sensorEvent.values.clone();
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = sensorEvent.values.clone();
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            //ambil rotationmatrix
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);

                //pitch adalah rotasi kedepan & belakang
                pitch = (float) Math.toDegrees((double)orientation[1]);
                pitch = (pitch + 360) % 360;
            }
        }




        Date date = new Date();
        BrakeDetector d = new BrakeDetector();
        DateFormat dateFormat = new SimpleDateFormat("dd-mm-yy/hh:mm:ss");

        d.date = dateFormat.format(date);
        if(mLocation!=null){

            Location loc = mLocation;
            d.lattitude = loc.getLatitude();
            d.longitude = loc.getLongitude();
        }

        //pengecekkan jika terjadi hentakan
        //cek sumbu z dengan toleransi jika lebih dari 5
        //cek derajat pitch diberi toleransi 270 derajat sampai 310
        if(az >= 5 && (pitch>270 && pitch < 310)){
            d.brake_status = "Pengereman Mendadak!";
            data.add(d);
            adapter.notifyDataSetChanged();

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }

    public void loadData(){

        recV.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RvAdapter(this);
        adapter.setListSensor(data);
        recV.setAdapter(adapter);
    }

    @Override
    protected void onDestroy(){
        // menutup database
        dbSensor.close();
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.clear){
            data.clear();
            adapter.notifyDataSetChanged();
        }else if(v.getId() == R.id.save){
            dbSensor.open();
            for (BrakeDetector m :data) {
                dbSensor.insertSensor(m);
            }
            Toast.makeText(this,"Saved", Toast.LENGTH_SHORT).show();

            dbSensor.close();

        }else if(v.getId() == R.id.load){

            dbSensor.open();
            data = dbSensor.getAllSensor();
            dbSensor.close();
            loadData();
            if(data.size()>0){

                Toast.makeText(this,"Loaded", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Empty", Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this,"Gagal konek location", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==MY_PERMISSION_REQUEST){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getLocation();
        }else{
            Toast.makeText(this,"Tidak mendapat izin", Toast.LENGTH_SHORT).show();
        }
    }
}

