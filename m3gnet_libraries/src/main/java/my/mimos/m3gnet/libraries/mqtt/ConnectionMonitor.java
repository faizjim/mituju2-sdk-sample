package my.mimos.m3gnet.libraries.mqtt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.NetworkInterface;
import java.util.Enumeration;

import my.mimos.m3gnet.libraries.Constant;

/**
 * Created by ariffin.ahmad on 10/8/2015.
 */
public class ConnectionMonitor {
    private ConnectivityManager conn;
    private WifiManager wifi;
    private Context context;

    private ConnectionListener listener = new ConnectionListener();

    public interface IConnectionMonitor {
        void onNewConnection(WifiInfo wifi_info);
    }

    public IConnectionMonitor callback;

    public ConnectionMonitor(Context context) {
        this.context =  context;
        this.conn    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.wifi    = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public NetworkInfo getActiveNetwork() {
        return conn.getActiveNetworkInfo();
    }

    public void startMonitor() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        this.context.registerReceiver(listener, filter);
    }

    public void stopMonitor() {
        this.context.unregisterReceiver(listener);
    }

    private class ConnectionListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String extra_info          = intent.getStringExtra(ConnectivityManager.EXTRA_EXTRA_INFO);
            String reason              = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
            boolean no_connectivity    = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            NetworkInfo active_network = conn.getActiveNetworkInfo();
            WifiInfo wifi_info         = active_network != null && active_network.getType() == ConnectivityManager.TYPE_WIFI ? wifi.getConnectionInfo() : null;

            try {
                Constant.NETWORK_INTERFACE_NAME   = "";
                if (wifi_info != null) {
                    Enumeration<NetworkInterface> aaa = NetworkInterface.getNetworkInterfaces();
                    while (aaa.hasMoreElements()) {
                        NetworkInterface itf = aaa.nextElement();
                        byte[] addr          = itf.getHardwareAddress();
                        String tmp_mac       = "";
                        if (addr != null)
                            for (int i = 0; i < addr.length; i++) {
                                tmp_mac += String.format("%02X%s", addr[i], (i < addr.length - 1) ? ":" : "");
                            }
                        tmp_mac = tmp_mac.toLowerCase();
                        if (tmp_mac.length() > 0 && tmp_mac.equals(wifi_info.getMacAddress())) {
                            Constant.NETWORK_INTERFACE_NAME = itf.getName();
                            break;
                        }

                    }
                }
            } catch (Exception e) {}

            String msg = " -> " + (active_network != null ? " interface name '" + Constant.NETWORK_INTERFACE_NAME + "' - type '" + active_network.getTypeName() + "' " : "") + (wifi_info != null ? "ssid '" + wifi_info.getSSID() + "'" : "") + "extra info '" + extra_info + "' - reason '" + reason + "' - no connectivity '" + no_connectivity + "'";
            Log.wtf(Constant.TAG, msg);

            if (callback != null)
                callback.onNewConnection(wifi_info);
        }
    }
}
