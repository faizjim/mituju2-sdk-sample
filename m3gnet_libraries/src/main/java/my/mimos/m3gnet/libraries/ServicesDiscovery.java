package my.mimos.m3gnet.libraries;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ariffin.ahmad on 8/30/2016.
 */
public class ServicesDiscovery {
    private NsdManager nsd_manager            = null;
    private NSDListener mqtt_listener         = new NSDListener();
    private NSDListener sip_listener          = new NSDListener();
    LinkedList<NsdServiceInfo> service_infos  = new LinkedList<>();
    private ResolverAdaptor resolver          = new ResolverAdaptor();

    public interface IServicesDiscovery {
        void onServiceDiscovered(NsdServiceInfo service_info);
        void onServiceLost(NsdServiceInfo service_info);
    }
    public interface IServicesDiscoveryStatus {
        void onDiscoveryStarted(String service_type);
        void onDiscoveryStopped(String service_type);
        void onStartDiscoveryFailed(String service_type, int error_code);
        void onStopDiscoveryFailed(String service_type, int error_code);
    }

    public IServicesDiscovery callback;
    public IServicesDiscoveryStatus callback_status;

    public ServicesDiscovery(Context context) {
        nsd_manager  = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void restartMonitor() {
        try {
            nsd_manager.stopServiceDiscovery(mqtt_listener);
            mqtt_listener.flag_start_discover = true;
        } catch (Exception e) {
            nsd_manager.discoverServices(Constant.SERVICE_TYPE_MQTT, NsdManager.PROTOCOL_DNS_SD, mqtt_listener);
        }
        try {
            nsd_manager.stopServiceDiscovery(sip_listener);
            sip_listener.flag_start_discover = true;
        } catch (Exception e) {
            nsd_manager.discoverServices(Constant.SERVICE_TYPE_SIP, NsdManager.PROTOCOL_DNS_SD, sip_listener);
        }
    }

    public void stopMonitor() {
        try {
            nsd_manager.stopServiceDiscovery(mqtt_listener);
            mqtt_listener.flag_start_discover = false;
        } catch (Exception e) {}
        try {
            nsd_manager.stopServiceDiscovery(sip_listener);
            sip_listener.flag_start_discover = false;
        } catch (Exception e) {}
    }

    private class NSDListener implements NsdManager.DiscoveryListener {
        public boolean flag_discovering          = false;
        public boolean flag_start_discover       = false;

        @Override
        public void onDiscoveryStarted(String service_type) {
            flag_discovering = true;
            Log.wtf(Constant.TAG, "discovering - '" + service_type + "'...");
            if (callback_status != null)
                callback_status.onDiscoveryStarted(service_type);
        }

        @Override
        public void onServiceFound(NsdServiceInfo service_info) {
            String msg = "\tname: '" + service_info.getServiceName() + "'\n";
            msg += "\ttype: '" + service_info.getServiceType() + "'";
            Log.wtf(Constant.TAG, "service found: \n" + msg);
            resolver.startResolve(service_info);
            //nsd_manager.resolveService(service_info, new ResolverAdaptor());
        }

        @Override
        public void onServiceLost(NsdServiceInfo service_info) {
            Log.wtf(Constant.TAG, "service lost: " + service_info);

            if (callback != null) {
                callback.onServiceLost(service_info);
//                if (service_info.getServiceType().equals(IShieldApplication.SERVICE_TYPE_MQTT))
//                    callback.onMQTTLost(service_info);
//                else
//                    callback.onSIPLost(service_info);
            }
        }

        @Override
        public void onDiscoveryStopped(String service_type) {
            flag_discovering = false;
            Log.wtf(Constant.TAG, "discovery stopped: " + service_type);
            if (flag_start_discover) {
                nsd_manager.discoverServices(service_type, NsdManager.PROTOCOL_DNS_SD, this);
                flag_start_discover = false;
            }
            if (callback_status != null)
                callback_status.onDiscoveryStopped(service_type);
        }

        @Override
        public void onStartDiscoveryFailed(String service_type, int error_code) {
            Log.wtf(Constant.TAG, "discovery start failed: type '" + service_type + "' - error code '" + error_code + "'");
            if (callback_status != null)
                callback_status.onStartDiscoveryFailed(service_type, error_code);
        }

        @Override
        public void onStopDiscoveryFailed(String service_type, int error_code) {
            Log.wtf(Constant.TAG, "discovery stop failed: type '" + service_type + "' - error code '" + error_code + "'");
            if (callback_status != null)
                callback_status.onStopDiscoveryFailed(service_type, error_code);
        }
    }

    private class ResolverAdaptor implements NsdManager.ResolveListener {
        private AtomicBoolean resolving = new AtomicBoolean(false);

        public void startResolve(NsdServiceInfo service_info) {
            if (service_info != null)
                service_infos.push(service_info);
            if (!resolving.get()) {
                if (service_infos.peek() != null) {
                    resolving.set(true);
                    nsd_manager.resolveService(service_infos.pop(), this);
                }
                else
                    resolving.set(false);
            }
        }

        @Override
        public void onResolveFailed(NsdServiceInfo service_info, int error_code) {
            Log.wtf(Constant.TAG, "resolve failed: name '" + service_info.getServiceName() + "' - error code '" + error_code + "'");
            resolving.set(false);
            startResolve(null);
        }

        @Override
        public void onServiceResolved(NsdServiceInfo service_info) {
            int port                  = service_info.getPort();
            InetAddress host          = service_info.getHost();
//            Map<String, byte[]> attrs = service_info.getAttributes();

//            String str_attrs  = "attrs\n=====\n";
//            for (Map.Entry<String, byte[]> entry : attrs.entrySet()) {
//                str_attrs  += "\t" + entry.getKey() + ": '" + new String(entry.getValue(), Charset.forName("UTF-8")) + "'\n";
//            }
            String msg       = "\tname: '" + service_info.getServiceName() + "'\n";
            msg             += "\ttype: '" + service_info.getServiceType() + "'\n";
            msg             += "\thost name: '" + host.getHostName() + "'\n";
            msg             += "\thost addr: '" + host.getHostAddress() + "'\n";
            msg             += "\tport: '" + port + "'";
            Log.wtf(Constant.TAG, "service resolved: \n" + msg);
            Log.i(Constant.TAG, "service resolved: \n" + msg);
//            Log.wtf(Constant.TAG, "service attrs: " + str_attrs);
//            Log.i(Constant.TAG, "service attrs: " + str_attrs);

            if (callback != null) {
                callback.onServiceDiscovered(service_info);
//                if (service_info.getServiceType().contains(IShieldApplication.SERVICE_TYPE_MQTT))
//                    callback.onMQTTDiscovered(service_info);
//                else
//                    callback.onSIPDiscovered(service_info);
            }
            resolving.set(false);
            startResolve(null);
        }
    }
}
