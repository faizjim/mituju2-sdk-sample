package my.mimos.mitujusdk;

import android.content.SharedPreferences;
import android.util.Log;

import my.mimos.mituju.v2.ilpservice.ILPConstants;

/**
 * Created by ariffin.ahmad on 24/04/2018.
 */

public class ModeSelector {
    private final SharedPreferences pref;
    private final SharedPreferences.Editor pref_editor;
    private int current_pos                             = 0;
    private final int[] modes                           = { ILPConstants.ALGO_ROUND_ROBIN, ILPConstants.ALGO_WIFI_DEFAULT, ILPConstants.ALGO_BLE_DEFAULT };

    public ModeSelector(SharedPreferences pref, SharedPreferences.Editor pref_editor) {
        this.pref        = pref;
        this.pref_editor = pref_editor;

        getStoredAlgoMode();
    }

    public int getNextAlgo() {
        current_pos += 1;
        if (current_pos >= modes.length)
            current_pos = 0;

        return modes[current_pos];
    }

    public String getAlgoName(int algo) {
        switch (algo) {
            case ILPConstants.ALGO_ROUND_ROBIN:
                return "Debug";
            case ILPConstants.ALGO_WIFI_DEFAULT:
                return "WiFi";
            case ILPConstants.ALGO_BLE_DEFAULT:
                return "BLE";
            default:
                return "Invalid";
        }
    }


    public void storeAlgoMode(int algo_mode) {
        pref_editor.putInt(MiTujuApplication.PREF_ALGO_MODE, algo_mode);
        pref_editor.commit();

        for (int i = 0; i < modes.length; i++) {
            if (algo_mode == modes[i]) {
                current_pos = i;
                break;
            }
        }
    }

    public int getStoredAlgoMode() {
        int cur = pref.getInt(MiTujuApplication.PREF_ALGO_MODE, ILPConstants.ALGO_ROUND_ROBIN);
        for (int i = 0; i < modes.length; i++) {
            if (cur == modes[i]) {
                current_pos = i;
                break;
            }
        }

        return cur;
    }
}
