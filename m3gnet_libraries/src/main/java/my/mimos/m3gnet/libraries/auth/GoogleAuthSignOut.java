package my.mimos.m3gnet.libraries.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import my.mimos.m3gnet.libraries.Constant;

/**
 * Created by ariffin.ahmad on 8/25/2016.
 */
public class GoogleAuthSignOut extends AbsGoogleAuth {

    private final SharedPreferences preferences;
    public IGoogleAuthSignOut callback;

    public interface IGoogleAuthSignOut {
        void onSignOutDone();
    }

    public GoogleAuthSignOut(Context context, String web_client_id) {
        super(context, web_client_id);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void signingOut() {
        Log.wtf(Constant.TAG, "main activity>>> silent signin: connecting...");
        connect();
    }

    @Override
    public GoogleApiClient buildApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Auth.GoogleSignInApi.signOut(google_api_client).setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                Log.wtf(Constant.TAG, ">>> logout status: " + status.getStatusCode() + " - " + status.getStatusMessage() + " - " + status.getStatus());
                                disconnect();
                                if (status.getStatusCode() == 0)
                                    hasSignOut(preferences);
                                if (callback != null)
                                    callback.onSignOutDone();
                            }
                        });
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }
}
