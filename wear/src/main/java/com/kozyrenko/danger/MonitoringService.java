package com.kozyrenko.danger;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.os.Process;

/**
 * Created by dev on 10/25/14.
 */
public class MonitoringService extends Service implements SensorEventListener {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private Sensor mAccelerometerSensor;
    private float mHeartRate;
    private String mMovement = "";
    private float mMovementSum;
    private boolean triggered;

    private static final String TAG = "MonitoringService";

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            if (mSensorManager == null) {
                mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
                mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
                mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

                mSensorManager.registerListener(MonitoringService.this, MonitoringService.this.mHeartRateSensor, 3);
                mSensorManager.registerListener(MonitoringService.this, MonitoringService.this.mAccelerometerSensor, 3);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        StringBuilder sb = new StringBuilder();


        if (sensorEvent.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            mHeartRate = sensorEvent.values[0];
            // Log.i(TAG, "Heart Rate" + " = " + mHeartRate);
        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // In this example, alpha is calculated as t / (t + dT),
            // where t is the low-pass filter's time-constant and
            // dT is the event delivery rate.

            final float alpha = 0.8f;
            float [] gravity = new float[3];
            float [] linear_acceleration = new float[3];

            // Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2];

            // Remove the gravity contribution with the high-pass filter.
            linear_acceleration[0] = sensorEvent.values[0] - gravity[0];
            linear_acceleration[1] = sensorEvent.values[1] - gravity[1];
            linear_acceleration[2] = sensorEvent.values[2] - gravity[2];

            // Log.i(TAG, "Movement" + " = " + linear_acceleration[0] + ":" + linear_acceleration[1] + ":" + linear_acceleration[2]);
            mMovement =  linear_acceleration[0] + "\n" + linear_acceleration[1] + "\n" + linear_acceleration[2];

            mMovementSum = Math.abs(linear_acceleration[0]) + Math.abs(linear_acceleration[1]);
        }
        if (mMovementSum > 15) {
            sendAlert();
        }
        sendToUI(mHeartRate + "\n" + mMovement);
    }

    private void sendAlert() {
        if (!triggered) {
            Intent dialogIntent = new Intent(getBaseContext(), AlertActivity.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplication().startActivity(dialogIntent);
            triggered = true;

            killUI();
        }

    }

    private void killUI() {
        Intent intent = new Intent("/kill");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendToUI(String text) {
        Intent intent = new Intent("/sensorInfo");
        intent.putExtra("sensorInfo", text);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "Accuracy changed for " + sensor + " = " + accuracy);
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        triggered = false;

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSensorManager.unregisterListener(this);
    }

    // childsafezone.herokuapp.com/alert
}
