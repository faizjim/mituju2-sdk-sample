package my.mimos.mituju.v2.maptool;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by ariffin.ahmad on 05/09/2017.
 */

public class Kompas implements SensorEventListener {
    private final SensorManager sensor_service;
    private final Sensor sensor_rotation_vector;

    private final static float[] Y_AXIS             = { 0, 1, 0, 0 };
    private float[] R                               = new float[16];
    private float last_val[]                        = new float[3];
    private float last_deg;

    public IKompas callback;

    public interface IKompas {
        void onKompasUpdated(float degree);
    }

    public Kompas(Context context) {
        sensor_service         = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor_rotation_vector = sensor_service.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    public void register() {
        last_deg    = Float.NaN;
        last_val[0] = Float.NaN;
        last_val[1] = Float.NaN;
        last_val[2] = Float.NaN;
        sensor_service.registerListener(this, sensor_rotation_vector, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregister() {
        sensor_service.unregisterListener(this, sensor_rotation_vector);
    }

    private static float smod(float x, float m)
    {
        return x-(float)((Math.floor(x/m + 0.5))*m);
    }

    float[] orientation = new float[3];
    float[] r_mat       = new float[9];
    @Override
    public void onSensorChanged(SensorEvent sensor_event) {
//        Log.wtf(Constant.TAG, "kompass>>> sensor changed - name '" + sensor_event.sensor.getName() + "' - type '" + sensor_event.sensor.getType() + "':(" + Sensor.TYPE_ROTATION_VECTOR + ")");
        if (callback == null || sensor_event.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR)
            return;

//        if (Float.isNaN(last_val[0]))
//            System.arraycopy(sensor_event.values, 0, last_val, 0, 3);
//
//        // The rotation matrix R transform a vector from the device coordinate
//        // system to the world coordinate system where Y is flat against the
//        // ground and pointing to magnetic North, Z pointing up and X is
//        // vector product Y.Z, roughly pointing East.
//
//        // Device coordinate system is defined relative to its default
//        // orientation. X is the horizontal axis, Y is the vertical axis and
//        // Z is pointing away from the screen.
//
//        // To get the compass vector pointing to magnetic north, the world Y
//        // axis, we need to rotate the device Y axis to it.
//
//        float[] N      = new float[4];
//        SensorManager.getRotationMatrixFromVector(R, sensor_event.values);
//        Matrix.multiplyMV(N, 0, R, 0, Y_AXIS, 0);
//
//        final float mx = last_val[0];
//        final float my = last_val[1];
//        final float mz = last_val[2];
//        float deg      = (float)Math.toDegrees(Math.atan2(mx, my));
//        last_val[0]    = mx;
//        last_val[1]    = my;
//        last_val[2]    = mz;

        // sdfdsfsdf
        SensorManager.getRotationMatrixFromVector(r_mat, sensor_event.values);
        int azimuth = (int) (Math.toDegrees(SensorManager.getOrientation(r_mat, orientation)[0]) + 360) % 360;

//        Log.wtf(Constant.TAG, "kompass>>> sensor changed - degree '" + deg + "' - azimuth '" + azimuth + "'");
        callback.onKompasUpdated(azimuth);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
