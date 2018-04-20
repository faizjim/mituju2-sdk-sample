package my.mimos.mitujusdk.search;

/**
 * Created by ariffin.ahmad on 04/10/2017.
 */

import android.text.Editable;
import android.text.TextWatcher;

public abstract class TextWatcherAdapter implements TextWatcher {

    private static final String TAG = "TextWatcherAdapter";

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}