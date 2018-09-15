package com.example.alper_arik.sensorexample;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private long lastUpdate = 0;
    private float last_x = 0, last_y = 0, last_z = 0;   //previous sensor values
    private static final int SHAKE_THRESHOLD = 50;      //threshold value
    private static final int EPSILON = 10;              //epsilon value

    private TextView passiveTimeTextView;   //views passive time
    private TextView activeTimeTextView;    //views active time

    private long counterActive = 0;     //active time variable
    private long counterPassive = 0;    //passive time varaible
    private long startTime = 0;         //stores system time when sensor is fired
    private long stopTime = 0;          //stores system time when sensor is stopped

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //rotation purpose
        if(savedInstanceState != null){
            counterPassive = savedInstanceState.getLong("PASSIVE");
            counterActive = savedInstanceState.getLong("ACTIVE");
        }

        //initialize sensorManager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //chooses sensor type
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //registers sensor
        mSensorManager.registerListener(this,mSensor,mSensorManager.SENSOR_DELAY_NORMAL);

        //textView configurations via xml file
        passiveTimeTextView = (TextView)findViewById(R.id.passiveTimeTextView);
        activeTimeTextView = (TextView) findViewById(R.id.activeTimeTextView);

        //initialize text views
        initTextViews();
    }

    private void initTextViews(){
        //calculates minutes and seconds
        long minutes = counterPassive / 60;
        long seconds = counterPassive % 60;
        //prints passive time
        passiveTimeTextView.setText(String.format("%d:%02d", minutes, seconds));

        minutes = counterActive / 60;
        seconds = counterActive % 60;
        activeTimeTextView.setText(String.format("%d:%02d", minutes, seconds));
    }

    @Override
    protected void onPause() {
        //unregisters sensor on pause
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        //registers sensor on resume
        super.onResume();
        mSensorManager.registerListener(this, mSensor, mSensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        //Saves time values with Bundle
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putLong("ACTIVE", counterActive);
        savedInstanceState.putLong("PASSIVE", counterPassive);
        Log.d("TAG", "save");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if(mySensor.getType() == Sensor.TYPE_ACCELEROMETER){
            //reads values
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            //gets current time
            long curTime = System.currentTimeMillis();

            //restrict samplingRate
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                //calc speed
                float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;

                //if speed is more than threshold, increases active time
                if (speed > SHAKE_THRESHOLD) {

                    stopTime = System.currentTimeMillis();

                    //if passed one second
                    if(stopTime - startTime >1000){
                        counterActive++;
                        long minutes = counterActive / 60;
                        long seconds = counterActive % 60;
                        activeTimeTextView.setText(String.format("%d:%02d", minutes, seconds));
                        startTime = System.currentTimeMillis();
                    }
                }

                //if speed is less than epsilon, increases passive time
                if (speed < EPSILON ){

                    stopTime = System.currentTimeMillis();

                    //if passed one second
                    if(stopTime - startTime >1000){
                        counterPassive++;
                        long minutes = counterPassive / 60;
                        long seconds = counterPassive % 60;
                        passiveTimeTextView.setText(String.format("%d:%02d", minutes, seconds));
                        startTime = System.currentTimeMillis();
                    }

                }

                //stores last variable of sensor
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
