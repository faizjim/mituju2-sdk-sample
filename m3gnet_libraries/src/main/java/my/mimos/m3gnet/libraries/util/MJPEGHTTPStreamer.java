package my.mimos.m3gnet.libraries.util;

import java.io.DataInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ariffin.ahmad on 21/11/2016.
 */

public class MJPEGHTTPStreamer extends AbsWorkerThread {
    private Exception exception        = null;
    private final int timeout_interval = 10 * 1000; // 10 seconds

    public String url               = "";
    public IMJPEGStreamer callback;

    public interface IMJPEGStreamer {
        void onMJPEGFrameReceived(byte[] bytes);
        void onMJPEGFrameStarted();
        void onMJPEGFrameEnded();
        void onMJPEGFrameError(Exception e);
    }

    public boolean checkStreamValidity() throws Exception {
        boolean ret                    = false;
        URL tmp_url                    = new URL(url);
        URLConnection conn             = tmp_url.openConnection();
        conn.setConnectTimeout(timeout_interval);
        Map<String, List<String>> maps = conn.getHeaderFields();

        if (maps != null) {
            List<String> map = maps.get("Content-Type");
            if (map != null && map.size() > 0 && map.get(0).toLowerCase().contains("multipart/"))
                ret = true;
            else
                throw new Exception("Invalid MJPEG link...");
        }
        else
            throw new Exception("Invalid MJPEG link...");

        return ret;
    }

    @Override
    public boolean preProcess() {
        boolean is_valid = false;
        try {
            is_valid = checkStreamValidity();
        }
        catch (Exception e) {
            this.exception = e;
        }

        if (callback != null && is_valid)
            callback.onMJPEGFrameStarted();
        return is_valid;
    }

    @Override
    public void postProcess() {
        if (callback != null) {
            if (exception != null)
                callback.onMJPEGFrameError(exception);
            else
                callback.onMJPEGFrameEnded();
        }
        super.postProcess();
    }

    @Override
    public void process() {
        try {
            URL tmp_url                = new URL(url);
            HttpURLConnection url_conn = (HttpURLConnection) tmp_url.openConnection();
            DataInputStream dis        = new DataInputStream(url_conn.getInputStream());

            byte prev_byte             = 0x00;
            int data                   = 0;
            ArrayList<Byte> bytes      = new ArrayList<>();

            while (!stop.get() && data > -1) {
                data = dis.read();
                byte tmp = (byte) data;
                if (prev_byte == (byte)0xFF && tmp == (byte)0xD8) {
                    bytes.clear();
                    bytes.add(prev_byte);
                }
                else if (prev_byte == (byte)0xFF && tmp == (byte)0xD9) {
                    bytes.add(tmp);
                    byte[] buff_frame  = new byte[bytes.size()];
                    for (int j = 0; j < buff_frame.length; j++) {
                        buff_frame[j] = bytes.get(j);
                    }
                    if (callback != null) {
                        callback.onMJPEGFrameReceived(buff_frame);
                    }
                }
                bytes.add(tmp);
                prev_byte = tmp;
            }
        }
        catch (Exception e) {
            this.exception = e;
        }
    }
}
