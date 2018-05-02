package my.mimos.m3gnet.libraries.auth;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;

import my.mimos.m3gnet.libraries.Constant;


/**
 * Created by ariffin.ahmad on 8/25/2016.
 */
public class GoogleAuthSilent extends AbsGoogleAuth {

    public interface IGoogleAuthSilent {
        void onSignInResult(GoogleSignInResult result);
    }

    public IGoogleAuthSilent callback;

    public GoogleAuthSilent(Context context, String web_client_id) {
        super(context, web_client_id);
    }

    @Override
    public GoogleApiClient buildApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(new SilentSignedInHandler())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    public void silentSignIn() {
        Log.wtf(Constant.TAG, "main activity>>> silent signin: connecting...");
        connect();
    }

    private class SilentSignedInHandler implements GoogleApiClient.ConnectionCallbacks {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Log.i(Constant.TAG, "Google Signin Connected...");
            GoogleSignInResult result                     = null;
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(google_api_client);
            Log.i(Constant.TAG, "Google Signin: onStart - opr done: " + opr.isDone());
            if (opr.isDone()) {
                Log.i(Constant.TAG, "got cached signed in....");
                result = opr.get();
//                if (callback != null)
//                    callback.onSignInResult(opr.get());
            } else {
                Log.i(Constant.TAG, "no cached signed in....");
//                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
//                    @Override
//                    public void onResult(GoogleSignInResult result) {
//                        if (callback != null)
//                            callback.onSignInResult(result);
//                    }
//                });
            }
            disconnect();
            if (callback != null)
                callback.onSignInResult(result);
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.i(Constant.TAG, "Google Signin Suspended...");
        }
    }
}
