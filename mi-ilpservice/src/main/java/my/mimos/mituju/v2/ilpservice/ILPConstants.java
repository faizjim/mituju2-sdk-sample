package my.mimos.mituju.v2.ilpservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import my.mimos.miilp.core.algo.Location;
import my.mimos.miilplib.android.LocEngine;
import my.mimos.mituju.v2.ilpservice.db.DBMiTuju;
import my.mimos.mituju.v2.ilpservice.struc.ProfilesInfo;

/**
 * Created by ariffin.ahmad on 07/04/2017.
 */

public class ILPConstants {
    public static final int ALGO_WIFI_DEFAULT         = 10011;
    public static final int ALGO_WIFI_BAYESIAN        = 10012;
    public static final int ALGO_WIFI_KNN             = 10013;
    public static final int ALGO_BLE_DEFAULT          = 10021;
    public static final int ALGO_BLE_BAYESIAN         = 10022;
    public static final int ALGO_BLE_KNN              = 10023;
    public static final int ALGO_ROUND_ROBIN          = 10031;
    public static final int ALGO_GPS_PURE             = 10041;
    public static final int ALGO_GPS_SNAP             = 10042;

    public static final String TAG                    = "ILP Service";
    public static final int NOTIFICATION_ID           = 10001;
    public static final int WIFI_SCAN_INTERVAL        =  2 * 1000;   // 5 * 1000;   // 5 seconds
    public static final int BLE_SCAN_INTERVAL         =  2 * 1000;   // 2 * 1000;   // 5 seconds
    public static final int BACKGROUND_SCAN_INTERVAL  = 30 * 1000;   // 30 seconds
    public static final double MAX_LATITUDE           = 85.05113220214844;
    public static final double MAX_LONGITUDE          = 180;
    public static final SimpleDateFormat UTC_FORMATER = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.S'Z'");

    public static String DOWNLOADPATH                 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/";
    public static String ROOTPATH                     = Environment.getExternalStorageDirectory().getPath() + "/";

//    public static String MQTT_URL                     = "wss://iot-mqtt.mimos.my";
    public static int MQTT_RECONNECT_INTERVAL          = 5 * 1000;                      // miliseconds
    public static String M3GNET_URL                    = "iot-mqtt.mimos.my";
    public static String MIILP_SERVER_PROTOCOL         = "https://";
//    public static String MIILP_SERVER_PATH_BUNDLE     = "/mi-tuju/map";
    public static String MIILP_SERVER_PATH_FINGERPRINT = "/mi-tuju/app/fingerprint";
    public static String MIILP_SERVER_PATH_MAP         = "/mi-tuju/app/map";
    public static String MIILP_SERVER_PATH_SITES       = "/mi-tuju/app/getmaps"; //"/mi-tuju/getapprovedsites";
    public static String MIILP_SERVER_PATH_STATUS      = "/mi-tuju/getstatus";
    public static String MIILP_DB_NAME                 = "miilp.db";
    public static String MIILP_LICENSE_NAME            = "license.out";
    public static String MIILP_MAP_PATH                = "map";

    public static boolean FLAG_SCREEN_LANDSCAPE   = true;
    public static boolean FLAG_ILP_LOC_VOTING     = true;
    public static boolean FLAG_MAP_ROTATION       = true;

    private final Context context;
    private AtomicBoolean flag_restart_ilp        = new AtomicBoolean(false);
    private String project_path                   = null;
//    private boolean ble_mode                      = false;
    private int algo_mode                         = ALGO_WIFI_DEFAULT;
    private ILPListener listener                  = new ILPListener();
//    private static ILPConstants instance          = null;
    protected SharedPreferences preferences       = null;
    protected SharedPreferences.Editor editor     = null;
    public DBMiTuju database;

    public final ArrayList<LocEngineWorker.ILocEngineWorker> listeners = new ArrayList<>();
    private LocEngineWorker engine_worker;

    public interface PREF_NAMES {
        String PREF_MAP_SCALE           = "mapScale";
        String PREF_MAP_DATE            = "mapDate";
        String PREF_FLAG_MAP_UPDATED    = "flagMapUpdated";
        String PREF_MAP_DEFAULT_SITEID  = "mapDefaultSiteId";
        String PREF_FLAG_ILP_LOC_VOTING = "ilpLocVoting";
        String PREF_FLAG_LANDSCAPE      = "screenLandscape";
        String PREF_FLAG_ROTATION       = "mapRotation";
    }

    public interface MESSAGE_ILP {
        int MSG_REGISTER_CLIENT             = 10001;
        int MSG_UNREGISTER_CLIENT           = 10002;
        int MSG_STATUS                      = 10003;
        int MSG_PROFILE                     = 10004;
        int MSG_SITES                       = 10005;
        int MSG_SITES_STATUS                = 10006;

        int MSG_START_ILP                   = 10006;
        int MSG_STOP_ILP                    = 10007;
        int MSG_REQUEST_SITES               = 10008;
    }

    public interface MESSAGE_ILP_DATA {
        String DATA_ILP_RUNNING                 = "ILPConstants.MESSAGE_ILP_DATA.DATA_ILP_RUNNING";
        String DATA_ILP_MESSAGE                 = "ILPConstants.MESSAGE_ILP_DATA.DATA_ILP_MESSAGE";
        String DATA_ILP_PROFILES_LOADED         = "ILPConstants.MESSAGE_ILP_DATA.DATA_ILP_PROFILES_LOADED";

        String DATA_SITE_STATUS                 = "ILPConstants.MESSAGE_ILP_DATA.DATA_SITE_STATUS";
        String DATA_SITE_ERROR                  = "ILPConstants.MESSAGE_ILP_DATA.DATA_SITE_ERROR";
        String DATA_SITE_SIZE                   = "ILPConstants.MESSAGE_ILP_DATA.DATA_SITE_SIZE";
        String DATA_SITE_COUNT                  = "ILPConstants.MESSAGE_ILP_DATA.DATA_SITE_COUNT";
        String DATA_SITE_ID                     = "ILPConstants.MESSAGE_ILP_DATA.DATA_SITE_ID";
        String DATA_SITE_ORG                    = "ILPConstants.MESSAGE_ILP_DATA.DATA_SITE_ORG";
        String DATA_SITE_SITE                   = "ILPConstants.MESSAGE_ILP_DATA.DATA_SITE_SITE";

        String DATA_PROFILE_VOTING_POS          = "ILPConstants.MESSAGE_ILP_DATA.DATA_PROFILE_VOTING_POS";
        String DATA_PROFILE_ID                  = "ILPConstants.MESSAGE_ILP_DATA.DATA_PROFILE_ID";
        String DATA_PROFILE_POS_X               = "ILPConstants.MESSAGE_ILP_DATA.DATA_PROFILE_POS_X";
        String DATA_PROFILE_POS_Y               = "ILPConstants.MESSAGE_ILP_DATA.DATA_PROFILE_POS_Y";
    }


    public ILPConstants(Context context) {
        this.context = context;
    }

//
//    public static ILPConstants getInstance() {
//        if (instance == null)
//            instance = new ILPConstants();
//        return instance;
//    }

    public void init(String m3gnet_url) {
        this.M3GNET_URL         = m3gnet_url;

        ROOTPATH                = context.getExternalFilesDir(null).getPath() + "/";
        database                = new DBMiTuju(context);
        UTC_FORMATER.setTimeZone(TimeZone.getTimeZone("UTC"));

        preferences             = PreferenceManager.getDefaultSharedPreferences(context);
        editor                  = preferences.edit();
        FLAG_SCREEN_LANDSCAPE   = preferences.getBoolean(PREF_NAMES.PREF_FLAG_LANDSCAPE, false);
        FLAG_ILP_LOC_VOTING     = preferences.getBoolean(PREF_NAMES.PREF_FLAG_ILP_LOC_VOTING, true);
        FLAG_MAP_ROTATION       = preferences.getBoolean(PREF_NAMES.PREF_FLAG_ROTATION, true);

        Log.i(TAG, " > application: application created");
        Log.wtf(TAG, " > application: Lolipop version '" + Build.VERSION_CODES.LOLLIPOP + "'");
        Log.wtf(TAG, " > application: current version '" + Build.VERSION.SDK_INT + "'");
        Log.wtf(TAG, " > application: ROOTPATH '" + ROOTPATH + "'");
        Log.wtf(TAG, " > application: Landscape mode '" + FLAG_SCREEN_LANDSCAPE + "'");
        Log.wtf(TAG, " > application: profile voting '" + FLAG_ILP_LOC_VOTING + "'");
    }


//    public boolean startService() {
//        if (!engine_worker.isRunning())
//            return engine_worker.start();
//        return true;
//    }

    public static String getAlgoName(int algo_mode) {
        String ret = "Round Robin";
        switch (algo_mode) {
            case ILPConstants.ALGO_WIFI_DEFAULT:
                ret = "WiFi_- Default";
                break;
            case ILPConstants.ALGO_WIFI_BAYESIAN:
                ret = "WiFi - Bayesian";
                break;
            case ILPConstants.ALGO_WIFI_KNN:
                ret = "WiFi - KNN";
                break;
            case ILPConstants.ALGO_BLE_DEFAULT:
                ret = "BLE - Default";
                break;
            case ILPConstants.ALGO_BLE_BAYESIAN:
                ret = "BLE - Bayesian";
                break;
            case ILPConstants.ALGO_BLE_KNN:
                ret = "BLE - KNN";
                break;
        }
        return ret;
    }

    private void startInternal() {
        engine_worker           = new LocEngineWorker(context, database, algo_mode);
        engine_worker.callback  = listener;
        engine_worker.start(project_path);

        this.project_path       = null;
//        this.ble_mode           = false;
        flag_restart_ilp.set(false);
    }

    public void startService(String path, int algo_mode) {
        this.project_path = path;
        this.algo_mode    = algo_mode;
        if (engine_worker != null && engine_worker.isRunning()) {
            flag_restart_ilp.set(true);
            stopService();
        }
        else {
            engine_worker = null;
            startInternal();
        }
    }

    public void stopService() {
        if (engine_worker != null && engine_worker.isRunning())
            engine_worker.stop();
    }

    public void setBackgroundMode(boolean background_mode) {
        if (engine_worker != null)
            engine_worker.setBackgroundMode(background_mode);
    }

    public void storeVotingConfig(boolean use_vote) {
        editor.putBoolean(PREF_NAMES.PREF_FLAG_ILP_LOC_VOTING, use_vote);
        editor.commit();
        FLAG_ILP_LOC_VOTING = preferences.getBoolean(PREF_NAMES.PREF_FLAG_ILP_LOC_VOTING, true);
    }

    public String getServiceFailedMessage() {
        return engine_worker != null ? engine_worker.failed_message : "";
    }

    public boolean isServiceRunning() {
        return engine_worker != null ? engine_worker.isRunning() : false;
    }

    public boolean hasProfileLoaded() {
        return engine_worker != null ? engine_worker.flag_profile_loaded : false;
    }

    public ProfilesInfo getProfiles() {
        return engine_worker != null ? engine_worker.profiles : null;
    }

    public Iterable<Location> getNavigationPath(Location target) {
        return engine_worker != null ? engine_worker.getNavigationPath(target) : new ArrayList<Location>();
    }

    public int getAlgoMode () {
        return engine_worker != null ? engine_worker.algo_mode : ALGO_WIFI_DEFAULT;
    }

    private class ILPListener implements LocEngineWorker.ILocEngineWorker {

        @Override
        public void onILPStarted() {
            Log.wtf(ILPConstants.TAG, "ilp service>>> location engine: started");
            for (LocEngineWorker.ILocEngineWorker listener : listeners) {
                listener.onILPStarted();
            }
        }

        @Override
        public void onILPStopped() {
            Log.wtf(ILPConstants.TAG, "ilp service>>> location engine: stopped");
            for (LocEngineWorker.ILocEngineWorker listener : listeners) {
                listener.onILPStopped();
            }

            if (flag_restart_ilp.get()) {
                Log.wtf(ILPConstants.TAG, "ilp service>>> location engine: restarting...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startInternal();
                    }
                }).start();
            }
        }

        @Override
        public void onILPFailed(String message) {
            Log.wtf(ILPConstants.TAG, "ilp service>>> location engine: failed '" + message + "'");
            for (LocEngineWorker.ILocEngineWorker listener : listeners) {
                listener.onILPFailed(message);
            }
        }

        @Override
        public void onILPProfileLoaded() {
            Log.wtf(ILPConstants.TAG, "ilp service>>> location engine: done loading profiles");
            for (LocEngineWorker.ILocEngineWorker listener : listeners) {
                listener.onILPProfileLoaded();
            }
        }

        @Override
        public void onILPProfilePointUpdated(int voting_position, Location user_loc) {
            Log.wtf(ILPConstants.TAG, "ilp service>>> location engine: profile updated...");
            for (LocEngineWorker.ILocEngineWorker listener : listeners) {
                listener.onILPProfilePointUpdated(voting_position, user_loc);
            }
        }

        @Override
        public void onILPPinsLoaded() {
            Log.wtf(ILPConstants.TAG, "ilp service>>> location engine: pins updated...");
            for (LocEngineWorker.ILocEngineWorker listener : listeners) {
                listener.onILPPinsLoaded();
            }
        }
    }
}
