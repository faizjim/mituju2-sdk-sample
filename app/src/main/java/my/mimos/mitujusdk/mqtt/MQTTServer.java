package my.mimos.mitujusdk.mqtt;

import android.content.Context;

import org.json.JSONObject;

import my.mimos.m3gnet.libraries.mqtt.AbsTopic;
import my.mimos.m3gnet.libraries.mqtt.MQTTComm;

/**
 * Created by ariffin.ahmad on 27/03/2017.
 */

public class MQTTServer implements MQTTComm.IMQTTConnection {
    private final MQTTComm mqtt;

    public IMQTTServer callback;

    public interface IMQTTServer extends MQTTComm.IMQTTConnection {
        void onMQTTTopicSiteConfig(SiteConfig site);
    }

    public MQTTServer(Context context, String organisation, String site, String url) { //, String google_id, String email) {
//        AbsTopic[] topics = new AbsTopic[2];
//        topics[0]         = new TopicAll(this);
//        topics[1]         = new TopicSiteConfig(this, organisation, site);
        AbsTopic[] topics = new AbsTopic[]{new TopicSiteConfig(this, organisation, site)};
        mqtt              = new MQTTComm(context, organisation, site, "M3Gnet", url, topics);
        mqtt.callback     = this;
    }

    public boolean isConnected() {
        return mqtt.isConnected();
    }

    public void connect(String username, String password) {
        mqtt.reconnect(username, password, true);
    }

    public void disconnect() {
        mqtt.disconnect();
    }

    public synchronized void publish(String topic, JSONObject json) {
        mqtt.publish(topic, json);
    }

    @Override
    public void onMQTTConnecting(int reconnect_counter) {
//        Log.wtf(MiTujuApplication.TAG, " > MQTT Server: connecting attempt " + reconnect_counter);
        if (callback != null)
            callback.onMQTTConnecting(reconnect_counter);
    }

    @Override
    public void onMQTTConnected() {
//        Log.wtf(MiTujuApplication.TAG, " > MQTT Server: connected");
        if (callback != null)
            callback.onMQTTConnected();
    }

    @Override
    public void onMQTTConnFailed(String s) {
//        Log.wtf(MiTujuApplication.TAG, " > MQTT Server: connection failed - " + s);
        if (callback != null)
            callback.onMQTTConnFailed(s);
    }

    @Override
    public void onMQTTConnLost(String s) {
//        Log.wtf(MiTujuApplication.TAG, " > MQTT Server: connection lost - " + s);
        if (callback != null)
            callback.onMQTTConnLost(s);
    }

    @Override
    public void onMQTTDisconnected(String s) {
//        Log.wtf(MiTujuApplication.TAG, " > MQTT Server: disconnected - " + s);
        if (callback != null)
            callback.onMQTTDisconnected(s);
    }

    @Override
    public void onMQTTMessageArrived(String topic, String message) {
//        Log.wtf(MiTujuApplication.TAG, " > MQTT Server: message arrive: topic '" + topic + "' - message '" + message + "'");
        if (callback != null)
            callback.onMQTTMessageArrived(topic, message);
    }

    @Override
    public void onMQTTMessageDispatched(String topic, String message) {
//        Log.wtf(MiTujuApplication.TAG, " > MQTT Server: message dispatched: topic '" + topic + "' - message '" + message + "'");
        if (callback != null)
            callback.onMQTTMessageDispatched(topic, message);
    }
}
