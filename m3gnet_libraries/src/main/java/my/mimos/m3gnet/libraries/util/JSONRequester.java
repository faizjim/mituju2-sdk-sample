package my.mimos.m3gnet.libraries.util;

import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import my.mimos.m3gnet.libraries.Constant;

/**
 * Created by ariffin.ahmad on 27/03/2017.
 */

public class JSONRequester extends AbsWorkerThread {
    public interface IJSONRequester {
        void onJSONRequestStart();
        void onJSONRequestDone(JSONArray json_array);
        void onJSONRequestDone(JSONObject json);
        void onJSONRequestError(Exception e);
    }

    private String s_url            = "";
    private int wait_secs           = 0;
    public IJSONRequester callback;

    public void start(String path) {
        start(path, 0);
    }

    public void start(String url, int wait_secs) {
        this.s_url      = url;
        this.wait_secs  = wait_secs;
        super.start();
    }

    @Override
    public void process() {
//        String tmp_url = "http://" + MiTujuApplication.MIILP_SERVER + ":" + MiTujuApplication.MIILP_SERVER_PORT + extra_path; // MiTujuApplication.MIILP_SERVER_PATH_STATUS + "?token=";
//        String tmp_url = Constant.MIILP_SERVER_PROTOCOL + MiTujuApplication.MIILP_SERVER + extra_path; // MiTujuApplication.MIILP_SERVER_PATH_STATUS + "?token=";
        Log.i(Constant.TAG, ">>>>> requesting status from: '" + s_url + "'");
        if (callback != null)
            callback.onJSONRequestStart();

        if (wait_secs > 0) {
            try { thread.join(wait_secs * 1000); } catch (InterruptedException e) {}
            if (stop.get())
                return;
        }

        try {
            JSONArray json_array   = null;
            JSONObject json        = null;
            URL url                = new URL(s_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn instanceof HttpsURLConnection && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                ((HttpsURLConnection)conn).setSSLSocketFactory(new TLSSocketFactory());
            conn.setReadTimeout(10 * 1000);
            conn.setConnectTimeout(15 * 1000);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();
            int response           = conn.getResponseCode();
            int content_length     = conn.getContentLength();

            InputStream is         = new BufferedInputStream(conn.getInputStream());//url.openStream());
            BufferedReader reader  = new BufferedReader(new InputStreamReader(is));

            StringBuilder incoming = new StringBuilder();
            String line            = "";
            while (!stop.get() && (line = reader.readLine()) != null) {
                incoming.append(line);
            }
            try { json_array = new JSONArray(incoming.toString()); }
            catch (JSONException e) { try { json = new JSONObject(incoming.toString()); } catch (JSONException e1) {} }
            Log.i(Constant.TAG, ">>>>> status response: '" + response + "' - content length '" + content_length + "' - body '" + incoming.toString() + "'");

            reader.close();
            is.close();

            if (callback != null) {
                if (response != 200)
                    callback.onJSONRequestError(new Exception("response error: " + response));
                else if (json_array != null)
                    callback.onJSONRequestDone(json_array);
                else
                    callback.onJSONRequestDone(json);
            }
        }
        catch (MalformedURLException e) {
            if (callback != null)
                callback.onJSONRequestError(e);
        }
        catch (IOException e) {
            if (callback != null)
                callback.onJSONRequestError(e);
        } catch (Exception e) {
            if (callback != null)
                callback.onJSONRequestError(e);
        }
    }
}