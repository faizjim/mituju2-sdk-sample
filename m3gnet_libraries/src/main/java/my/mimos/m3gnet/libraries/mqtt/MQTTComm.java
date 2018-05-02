package my.mimos.m3gnet.libraries.mqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import my.mimos.m3gnet.libraries.Constant;
import my.mimos.m3gnet.libraries.util.AbsWorkerThread;

/**
 * Created by ariffin.ahmad on 5/12/2016.
 */
public class MQTTComm {
    private static final int MQTT_QOS         = 1;
    public static int reconnect_interval_msec = 5 * 1000; // 5 mili seconds

    private MqttAsyncClient client;
    private MqttDefaultFilePersistence persistence;

    public MqttConnectOptions opts             = new MqttConnectOptions();
    private Listener listener                  = new Listener();
    private ConnListener conn_listener         = new ConnListener();
    private DisconnListener disconn_listener   = new DisconnListener();
    private SubListener sub_listener           = new SubListener();
    private final AtomicBoolean flag_reconnect = new AtomicBoolean(false);
    private final ReconnectStarter reconnector = new ReconnectStarter();
    private int reconnect_counter              = 0;

    public final ArrayList<AbsTopic> topics  = new ArrayList<>();

    public EnumMQTTComm current_status       = EnumMQTTComm.NOTHING;
    public IMQTTConnection callback;

    public String org;
    public String site;
    public String service_name;
    public String url;
    public final String ip;
    public final boolean ipv6;
    public final int port;

    public interface IMQTTConnection {
        void onMQTTConnecting(int reconnect_counter);
        void onMQTTConnected();
        void onMQTTConnFailed(String message);
        void onMQTTConnLost(String message);
        void onMQTTDisconnected(String message);
        void onMQTTMessageArrived(String topic, String message);
        void onMQTTMessageDispatched(String topic, String message);
    }

    public enum EnumMQTTComm {
        NOTHING, CONNECTING, CONNECTED, CONNECTION_FAILED, CONNECTION_LOST, DISCONNECTED
    }

    public MQTTComm(Context context, String org, String site, String service_name, String ip, int port, AbsTopic[] new_topics) {
        this(context, org, site, service_name, ip, false, port, new_topics);
    }

    public MQTTComm(Context context, String org, String site, String service_name, String ip, boolean ipv6, int port, AbsTopic[] new_topics) {
        this.ip            = ip;
        this.port          = port;
        this.ipv6          = ipv6;
        this.url           = null;

        init(context, org, site, service_name, new_topics);
//        initTopics(ishield_app);
    }

    public MQTTComm(Context context, String org, String site, String service_name, String url, AbsTopic[] new_topics) {
        this.ip            = null;
        this.port          = 0;
        this.ipv6          = false;
        this.url           = url;

        init(context, org, site, service_name, new_topics);
//        initTopics(ishield_app);
    }

    private void init(Context context, String org, String site, String service_name, AbsTopic[] new_topics) {
        this.org           = org;
        this.site          = site;
        this.service_name  = service_name.replace("\\032", " ");

        // set persistence storage
        File my_dir = context.getDir(Constant.TAG + "mqtt_wss", Context.MODE_PRIVATE);
        persistence = new MqttDefaultFilePersistence(my_dir.getAbsolutePath());

        opts.setCleanSession(true);

        for (AbsTopic tmp_topic : new_topics) {
            topics.add(tmp_topic);
        }
    }
//
//    private void initTopics(IShieldApplication ishield_app) {
//        topics.add(new TopicAll(this));
//        topics.add(new TopicEvent(this));
//        topics.add(new TopicSnapshot(this));
//        topics.add(new TopicUser(this));
//        topics.add(new TopicLatLon(this));
//        topics.add(new TopicCamera(this));
//        topics.add(new TopicNearTo(ishield_app, this));
//        topics.add(new TopicOrgSite(this));
//        topics.add(new TopicMessaging(ishield_app, this));
//        topics.add(new TopicGroupMembership(ishield_app, this));
//        topics.add(new TopicCannedMessages(this));
//    }
//
//    public void connect(String password) {
//        opts.setPassword(password.toCharArray());
//        connect();
//    }

    private void connect() {
        if (client == null || !client.isConnected()) {
            reconnect_counter += 1;
            current_status     = EnumMQTTComm.CONNECTING;
            try {
                Constant.MQTT_CLIENT_ID = MqttAsyncClient.generateClientId();
                if (url == null || url.length() <= 0) {
                    String tmp_ip = ip;
                    if (ipv6)
                        tmp_ip    = "[" + ip + "]";
                    url       = "tcp://" + tmp_ip + ":" + port;
                }

                client            = new MqttAsyncClient(url, Constant.MQTT_CLIENT_ID, persistence);
                client.setCallback(listener);

                Log.wtf(Constant.TAG, ">> mqtt - attempting connecting to: " + url);
                // make connection
                client.connect(opts, "connection sample", conn_listener);
                if (callback != null)
                    callback.onMQTTConnecting(reconnect_counter);
            } catch (MqttException e) {}
        }
    }

    public void disconnect() {
        flag_reconnect.set(false);
        if (client != null && client.isConnected()) {
            try {
                Log.wtf(Constant.TAG, ">> mqtt - disconnecting....");
                client.disconnect("disconnecting...", disconn_listener);
                persistence.close();
            } catch (MqttException e) {}
        }
        else if (callback != null) {
            current_status = EnumMQTTComm.DISCONNECTED;
            callback.onMQTTDisconnected(null);
        }
    }

    public void reconnect() {
        reconnect(null, null, false);
    }

    public void reconnect(String username, String password) {
        reconnect(username, password, false);
    }

    public void reconnect(String username, String password, boolean reconnect) {
        if (username != null)
            opts.setUserName(username);
        if (password != null)
            opts.setPassword(password.toCharArray());
        reconnect(reconnect);
    }

    private void reconnect(boolean reconnect) {
        reconnect_counter = 0;
        flag_reconnect.set(reconnect);
        if (client != null && client.isConnected())
            disconnect();
        else
            connect();
    }

    public String getCurrentStatus() {
        switch(current_status) {
            case CONNECTING:
                return "connecting";
            case CONNECTED:
                return "connected";
            case CONNECTION_FAILED:
                return "connection failed";
            case CONNECTION_LOST:
                return "connection lost";
            case DISCONNECTED:
                return "disconnected";
            default:
                return "---";
        }
    }

    public void publish(String topic, JSONObject json) {
        publish(topic, json, true);
    }

    public void publish(String topic, JSONObject json, boolean retain) {
        if (client == null || !client.isConnected()) {
            Log.wtf(Constant.TAG, ">> mqtt - error publishing - client unavailable");
            return;
        }

        try {
            client.publish(topic, json.toString().getBytes("UTF-8"), 1, retain);
            if (callback != null)
                callback.onMQTTMessageDispatched(topic, json.toString());
        }
        catch (MqttException e) {
            Log.wtf(Constant.TAG, ">> mqtt - publish exception: " + e.getMessage());
        }
        catch (UnsupportedEncodingException e) {
            Log.wtf(Constant.TAG, ">> mqtt - publish encoding exception: " + e.getMessage());
        }
    }

    public void clear() {
        callback    = null;
        disconnect();
    }

    public boolean isConnected() {
        return client != null ? client.isConnected() : false;
    }

    private class ConnListener implements IMqttActionListener {

        @Override
        public void onSuccess(IMqttToken iMqttToken) {
            try {
                for (AbsTopic topic : topics) {
                    String[] arr_topics = topic.getTopics();
                    for (String tmp_topic : arr_topics) {
                        client.subscribe(tmp_topic, MQTT_QOS, topic.desc, sub_listener, topic);
                        Log.i(Constant.TAG, " > MQTT broker '" + url + "': subscribing topic - " + tmp_topic);
                    }
                }
            } catch (MqttException e) {}

            reconnect_counter = 0;
            current_status    = EnumMQTTComm.CONNECTED;
            if (callback != null)
                callback.onMQTTConnected();
            Log.i(Constant.TAG, " > MQTT broker '" + url + "': connected");
        }

        @Override
        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            current_status = EnumMQTTComm.CONNECTION_FAILED;
            throwable.printStackTrace();
            if (callback != null)
                callback.onMQTTConnFailed(throwable.getMessage());
            Log.i(Constant.TAG, " > MQTT broker '" + url + "': fail connecting - " + throwable.getMessage());
            throwable.printStackTrace();

            if (flag_reconnect.get())
                reconnector.start();
        }
    }

    private class DisconnListener implements IMqttActionListener {

        @Override
        public void onSuccess(IMqttToken iMqttToken) {
            current_status = EnumMQTTComm.DISCONNECTED;
            if (callback != null)
                callback.onMQTTDisconnected(null);
            Log.i(Constant.TAG, " > MQTT broker '" + url + "': disconnected");

            if (flag_reconnect.get())
                reconnector.start();
        }

        @Override
        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            if (flag_reconnect.get())
                reconnector.start();
        }
    }

    private class SubListener implements IMqttActionListener {

        @Override
        public void onSuccess(IMqttToken iMqttToken) {
        }

        @Override
        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
        }
    }

    private class Listener implements MqttCallback {

        @Override
        public void connectionLost(Throwable throwable) {
            current_status = EnumMQTTComm.CONNECTION_LOST;
            throwable.printStackTrace();
            if (callback != null)
                callback.onMQTTConnLost(throwable.getMessage());
            Log.i(Constant.TAG, " > MQTT broker '" + url + "' lost connection - " + throwable.getMessage());

            if (flag_reconnect.get())
                reconnector.start();
        }

        @Override
        public void messageArrived(String topic_name, MqttMessage mqtt_message) throws Exception {
            Log.i(Constant.TAG, " mqtt comm>>>> MQTT broker '" + url + "': topic '" + topic_name + "' - message " + mqtt_message.toString());
            if (callback != null)
                callback.onMQTTMessageArrived(topic_name, mqtt_message.toString());
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        }
    }

    private class ReconnectStarter extends AbsWorkerThread {

        @Override
        public void process() {
            try {
                Thread.sleep(reconnect_interval_msec);
                if (flag_reconnect.get())
                    connect();
            }
            catch (InterruptedException e) {}
        }
    }
}