package com.example.lint;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class LinternaActivity extends AppCompatActivity {

    private ImageView boton;
    private ImageView imgInternet;
    private ImageView imgModo;
    private boolean isAuto=false;

    private TextView txtLuminosidad,txtProximidad;

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private float maxValue;
    Tarea_Luminosidad AsyncTask_Luminosidad;

    Camera camara;
    Camera.Parameters parametros;
    boolean isOn=false,isFlash=false;

    private SensorManager sm_Proximidad;
    private Sensor s_Proximidad;
    boolean enBolsillo=false;
    Tarea_Proximidad AsyncTask_Proximidad;

    private Handler handlerInternet;
    private Timer timerInternet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linterna);

        boton=(ImageView) findViewById(R.id.imgLinterna);
        imgInternet=(ImageView) findViewById(R.id.imgInternet);
        imgModo=(ImageView) findViewById(R.id.imgModo);

        imgModo.setOnClickListener(HandlerCmdModo);

        //mantener pantalla encendida
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        txtLuminosidad=(TextView) findViewById(R.id.txtLuminosidad);
        txtProximidad=(TextView) findViewById(R.id.txtProximidad);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor == null) {
            Toast.makeText(this, "El dispositivo no tiene sensor de luminosidad!", Toast.LENGTH_SHORT).show();
            finish();
        }

        // max value for light sensor
        maxValue = lightSensor.getMaximumRange();

        AsyncTask_Luminosidad= new Tarea_Luminosidad();

        //Sensor de proximidad
        sm_Proximidad = (SensorManager) getSystemService(SENSOR_SERVICE);
        s_Proximidad = sm_Proximidad.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        if (s_Proximidad == null) {
            Toast.makeText(this, "El dispositivo no tiene sensor de proximidad !", Toast.LENGTH_SHORT).show();
            finish();
        }

        AsyncTask_Proximidad = new Tarea_Proximidad();

        if(getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
        {
            camara=Camera.open();
            parametros=camara.getParameters();
            isFlash=true;
        }

        handlerInternet = new Handler();
        timerInternet = new Timer();

    }

    View.OnClickListener HandlerCmdModo=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isAuto)
            {
                imgModo.setImageResource(R.drawable.manual);
                cancelarAuto();
                isAuto=false;
            }
            else
            {
                imgModo.setImageResource(R.drawable.auto);
                ejecutarAuto();
                isAuto=true;
            }

        }
    };

    public void setBolsillo (Boolean b) {
        enBolsillo=b;
    }

    public Boolean getBolsillo () {
        return enBolsillo;
    }

    public TimerTask timerTaskInternet = new TimerTask() {
        @Override
        public void run() {
            handlerInternet.post(new Runnable() {
                public void run() {
                    try {
                        Tarea_Internet AsyncTask_Internet=new Tarea_Internet();
                        // PerformBackgroundTask this class is the class that extends AsynchTask
                        AsyncTask_Internet.execute();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                    }
                }
            });
        }
    };

    public void muestraMensaje(String mensaje)
    {

        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }


    public void enciendeFlash()
    {
        if(isFlash) {
            boton.setImageResource(R.drawable.linterna_on);
            parametros.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camara.setParameters(parametros);
            camara.startPreview();
            isOn = true;
        }
    }

    public void apagaFlash()
    {
        if(isFlash) {
            boton.setImageResource(R.drawable.linterna_off);
            parametros.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camara.setParameters(parametros);
            camara.stopPreview();
            isOn = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isAuto) {
            ejecutarAuto();
        }
        timerInternet.schedule(timerTaskInternet, 0, 5000);

    }

    protected void ejecutarAuto()
    {
        AsyncTask_Proximidad=new Tarea_Proximidad();
        AsyncTask_Luminosidad=new Tarea_Luminosidad();
        AsyncTask_Luminosidad.execute(sensorManager, lightSensor);
        AsyncTask_Proximidad.execute(sm_Proximidad, s_Proximidad);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isAuto) {
            cancelarAuto();
            apagaFlash();
        }

        timerInternet.cancel();
    }

    protected  void cancelarAuto()
    {
        AsyncTask_Luminosidad.cancel(true);
        AsyncTask_Proximidad.cancel(true);

        //apagaFlash();
    }


    class Tarea_Luminosidad extends AsyncTask {

        SensorManager sm;
        Sensor s;

        @Override
        protected Object doInBackground(Object[] objects) {
            sm=(SensorManager) objects[0];
            s=(Sensor) objects[1];
            sm.registerListener(lightEventListener, s, SensorManager.SENSOR_DELAY_FASTEST);
            return null;
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);

            float valor=(float)values[0];

            //getSupportActionBar().setTitle("Luminosity : " + valor + " lx");
            txtLuminosidad.setText("Luminosidad: "+valor+" Bolsillo: "+getBolsillo());

            //Si la luminosidad es nula, el flash esta apagado y no esta en el bolsillo->enciende
            if(valor==0.0 && isOn==false && getBolsillo()==false)
            {
                enciendeFlash();
            }

            //Si la luminosidad no es nula y el flash esta encendido->apaga
            if(valor!=0.0 && isOn==true)
            {
                apagaFlash();
            }
        }


        @Override
        protected void onCancelled() {
            super.onCancelled();
            sm.unregisterListener(lightEventListener);
        }

        public SensorEventListener lightEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float value = sensorEvent.values[0];

                publishProgress(value);

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }

    class Tarea_Proximidad extends AsyncTask {

        SensorManager sm;
        Sensor s;

        @Override
        protected Object doInBackground(Object[] objects) {
            sm=(SensorManager) objects[0];
            s=(Sensor) objects[1];
            sm.registerListener(proximidadEventListener, s, SensorManager.SENSOR_DELAY_FASTEST);
            return null;
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);

            float valor=(float)values[0];

            txtProximidad.setText("Proximidad: "+valor+" Bolsillo: "+getBolsillo()+" "+txtLuminosidad.getText().subSequence(13,16));

            /*if(valor ==0.0) {
                // Detected something nearby
                //enBolsillo=false;
                setBolsillo(false);
            } else {
                //enBolsillo=true;
                setBolsillo(true);
            }*/

            if(valor < s_Proximidad.getMaximumRange()) {
                // Detected something nearby
                setBolsillo(true);
            } else {
                // Nothing is nearby
                setBolsillo(false);
            }

            //el flash esta encendido y esta en el bolsillo->apaga
            if(isOn==true && getBolsillo()==true)
            {
                apagaFlash();
            }

            if(getBolsillo()==false && txtLuminosidad.getText().subSequence(13,16).equals("0.0"))
            {
                enciendeFlash();
            }

        }


        @Override
        protected void onCancelled() {
            super.onCancelled();
            sm.unregisterListener(proximidadEventListener);
        }

        public SensorEventListener proximidadEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float value = sensorEvent.values[0];

                publishProgress(value);

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }

    class Tarea_Internet extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {

            String mensaje;

            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                mensaje="SI";
            } else {
                mensaje="NO";
            }

            return mensaje;
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            if(o.equals("SI"))
                imgInternet.setImageResource(R.drawable.internet_on);
            else
                imgInternet.setImageResource(R.drawable.internet_off);
        }
    }

}


