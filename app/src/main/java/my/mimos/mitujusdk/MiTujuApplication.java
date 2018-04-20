package my.mimos.mitujusdk;

import android.app.Application;
import android.os.Environment;

import java.io.File;
import java.util.Calendar;
import java.util.TimeZone;

import my.mimos.mituju.v2.ilpservice.ILPConstants;

/**
 * Created by ariffin.ahmad on 05/07/2017.
 */

public class MiTujuApplication extends Application {
    private static final String PREF_ALGO_MODE                 = "PREF_ALGO_MODE";

    public static final String TAG                             = "MiTuju SDK Sample";

    public static String ROOTPATH                              = Environment.getExternalStorageDirectory().getPath() + "/";
    public static String FINGERPRINT_DB                        = "miilp.db";
    public static String COMPRESSED_DB                         = "tractive.zip"; //"bitxlab.zip";

    public final ILPConstants ilp_constant                     = new ILPConstants(this);

    @Override
    public void onCreate() {
        super.onCreate();

        ROOTPATH                      = getExternalFilesDir(null).getPath() + File.separator;
        ilp_constant.init("");
    }

    public static String getUTCNow() {
        String ret = "";

        TimeZone time_zone = TimeZone.getTimeZone("UTC");
        Calendar cal       = Calendar.getInstance(time_zone);
        ret                = ILPConstants.UTC_FORMATER.format(cal.getTime());

        return ret;
    }


    public int getAlgoMode() {
        return ILPConstants.ALGO_ROUND_ROBIN;
    }
}
