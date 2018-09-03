package my.mimos.mitujusdk;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;

import java.io.File;

import my.mimos.mituju.v2.ilpservice.ILPConstants;

/**
 * Created by ariffin.ahmad on 05/07/2017.
 */

public class MiTujuApplication extends Application {
    public static final String PREF_ALGO_MODE                  = "PREF_ALGO_MODE";

    public static final String TAG                             = "MiTuju SDK Sample";
    public static String ANDROID_ID                            = "wss";

    public static String ROOTPATH                              = Environment.getExternalStorageDirectory().getPath() + "/";
    public static String FINGERPRINT_DB                        = "miilp.db";
    public static String COMPRESSED_DB                         = "tractive.zip";

    public ModeSelector algo_selector;
    public final ILPConstants ilp_constant                     = new ILPConstants(this);

    private SharedPreferences pref;
    private SharedPreferences.Editor pref_editor;

    @Override
    public void onCreate() {
        super.onCreate();

        ANDROID_ID        = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        pref              = PreferenceManager.getDefaultSharedPreferences(this);
        pref_editor       = pref.edit();

        algo_selector     = new ModeSelector(pref, pref_editor);
        ROOTPATH          = getExternalFilesDir(null).getPath() + File.separator;
        ilp_constant.init("");
    }
}
