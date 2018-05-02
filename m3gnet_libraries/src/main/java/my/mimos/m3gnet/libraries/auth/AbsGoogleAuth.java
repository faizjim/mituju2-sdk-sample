package my.mimos.m3gnet.libraries.auth;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

/**
 * Created by ariffin.ahmad on 8/25/2016.
 */
public abstract class AbsGoogleAuth {
    public static final String PREF_SIGNED_IN          = "preferences.signed.in";
    public static final String PREF_SIGNED_NAME        = "preferences.signed.name";
    public static final String PREF_SIGNED_NAME_GIVEN  = "preferences.signed.name.given";
    public static final String PREF_SIGNED_NAME_FAMILY = "preferences.signed.name.family";
    public static final String PREF_SIGNED_EMAIL       = "preferences.signed.email";
    public static final String PREF_SIGNED_GOOGLEID    = "preferences.signed.googleid";
    public static final String PREF_SIGNED_URL         = "preferences.signed.url";

    protected GoogleSignInOptions gso;

    public GoogleApiClient google_api_client;

    public AbsGoogleAuth(Context context, String web_client_id) {
        gso           = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(new Scope(Scopes.PROFILE))
                        .requestIdToken(web_client_id)
                        .requestProfile()
                        .requestEmail()
                        .build();

        google_api_client = buildApiClient(context);
    }


    public static boolean hasSignedIn(SharedPreferences preferences) {
        return preferences.getBoolean(PREF_SIGNED_IN, false);
    }

    public static void hasSignOut(SharedPreferences preferences) {
        preferences.edit().putBoolean(PREF_SIGNED_IN, false).commit();
    }

    public static SignedInDataStructure signingIn(SharedPreferences preferences, String name, String name_given, String name_family, String email, String google_id, String url) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_SIGNED_IN, true);
        editor.putString(PREF_SIGNED_NAME, name);
        editor.putString(PREF_SIGNED_NAME_GIVEN, name_given);
        editor.putString(PREF_SIGNED_NAME_FAMILY, name_family);
        editor.putString(PREF_SIGNED_EMAIL, email);
        editor.putString(PREF_SIGNED_GOOGLEID, google_id);
        editor.putString(PREF_SIGNED_URL, url);
        editor.commit();

        return loadSignIn(preferences);
    }

    public static SignedInDataStructure loadSignIn(SharedPreferences preferences) {
        SignedInDataStructure ret = null;
        String url                = preferences.getString(PREF_SIGNED_URL, null);
        String name               = preferences.getString(PREF_SIGNED_NAME, null);
        String name_given         = preferences.getString(PREF_SIGNED_NAME_GIVEN, null);
        String name_family        = preferences.getString(PREF_SIGNED_NAME_FAMILY, null);
        String email              = preferences.getString(PREF_SIGNED_EMAIL, null);
        String google_id          = preferences.getString(PREF_SIGNED_GOOGLEID, null);

        if (google_id != null)
            ret = new SignedInDataStructure(name, name_given, name_family, email, google_id, url);

        return ret;
    }

    protected void connect() {
        google_api_client.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
    }

    protected void disconnect() {
        if (google_api_client != null)
            google_api_client.disconnect();
    }

    public Scope[] getScopeArray() {
        return gso.getScopeArray();
    }

    public abstract GoogleApiClient buildApiClient(Context context);
}
