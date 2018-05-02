package my.mimos.mitujusdk;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.util.Calendar;
import java.util.TimeZone;

import my.mimos.mituju.v2.ilpservice.ILPConstants;
import my.mimos.mitujusdk.mqtt.MQTTServer;

/**
 * Created by ariffin.ahmad on 05/07/2017.
 */

public class MiTujuApplication extends Application {
    public static final String PREF_ALGO_MODE                  = "PREF_ALGO_MODE";
    public static final String PREF_NAME                       = "PREF_NAME";

    public static final String TAG                             = "MiTuju SDK Sample";
    public static final String ORG                             = "PSDC";
    public static final String SITE                            = "PENANG";
    public static String ANDROID_ID                            = "wss";
    public static String MQTT_PROTOCOL                         = "wss";
    public static  String MQTT_NAME                            = "xxxxxxxx";
    public static final String MQTT_SERVER                     = "iot-mqtt.mimos.my";
    public static final String MQTT_USERNAME                   = "system";
    public static final String MQTT_PASSWORD                   = "Mari-Pergi";

    public static String ROOTPATH                              = Environment.getExternalStorageDirectory().getPath() + "/";
    public static String FINGERPRINT_DB                        = "miilp.db";
    public static String COMPRESSED_DB                         = "psdc.zip";

    public ModeSelector algo_selector;
    public final ILPConstants ilp_constant                     = new ILPConstants(this);
    public MQTTServer mqtt_server;

    private SharedPreferences pref;
    private SharedPreferences.Editor pref_editor;

    @Override
    public void onCreate() {
        super.onCreate();

        ANDROID_ID        = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        pref              = PreferenceManager.getDefaultSharedPreferences(this);
        pref_editor       = pref.edit();
        MQTT_NAME         = pref.getString(PREF_NAME, ANDROID_ID);
        Log.wtf(MiTujuApplication.TAG, "app>>> android id: " + MQTT_NAME);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            MQTT_PROTOCOL = "ws";

        algo_selector     = new ModeSelector(pref, pref_editor);
        ROOTPATH          = getExternalFilesDir(null).getPath() + File.separator;
        mqtt_server       = new MQTTServer(MiTujuApplication.this, ORG, SITE, MQTT_PROTOCOL + "://" + MQTT_SERVER);
        ilp_constant.init("");
    }

    public void storeName(String name) {
        MQTT_NAME = name;
        pref_editor.putString(PREF_NAME, name);
        pref_editor.commit();
    }

    public static String getUTCNow() {
        String ret = "";

        TimeZone time_zone = TimeZone.getTimeZone("UTC");
        Calendar cal       = Calendar.getInstance(time_zone);
        ret                = ILPConstants.UTC_FORMATER.format(cal.getTime());

        return ret;
    }
}
