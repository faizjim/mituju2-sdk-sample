package my.mimos.m3gnet.libraries.auth;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by ariffin.ahmad on 8/25/2016.
 */
public class GoogleAuth extends AbsGoogleAuth {

    public interface IGoogleAuth {
        void onAuthConnected();
    }

    public IGoogleAuth callback;

    public GoogleAuth(Context context, String web_client_id) {
        super(context, web_client_id);
    }

    @Override
    public GoogleApiClient buildApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        if (callback != null)
                            callback.onAuthConnected();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void connect() {
        super.connect();
    }

    @Override
    public void disconnect() {
        super.disconnect();
    }
}
