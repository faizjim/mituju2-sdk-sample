package my.mimos.m3gnet.libraries.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ariffin.ahmad on 14/09/2016.
 */
public abstract class AbsTopic implements IMqttMessageListener {
    public final String desc;

    public AbsTopic(String desc) {
        this.desc   = desc;
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqtt_message) throws Exception {
        String message = mqtt_message.toString();

        try {
            onTopicOccur(topic, new JSONObject(message));
        }
        catch (JSONException e) {
            onTopicOccur(topic, message);
        }
    }

    public abstract String[] getTopics();
    public abstract void onTopicOccur(String topic, String message);
    public abstract void onTopicOccur(String topic, JSONObject json);
}
