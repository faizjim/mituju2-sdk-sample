package my.mimos.mitujusdk.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import my.mimos.m3gnet.libraries.mqtt.AbsTopic;

/**
 * Created by ariffin.ahmad on 5/13/2016.
 */
public class TopicAll extends AbsTopic {
    private MQTTServer mqtt_server;

    public TopicAll(MQTTServer mqtt_server) {
        super("all readings");
        this.mqtt_server = mqtt_server;
    }

    @Override
    public String[] getTopics() {
        return new String[]{"#"};
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqtt_message) throws Exception {
        if (mqtt_server.callback != null)
            mqtt_server.callback.onMQTTMessageArrived(topic, mqtt_message.toString());
        super.messageArrived(topic, mqtt_message);
    }

    @Override
    public void onTopicOccur(String topic, String message) {

    }

    @Override
    public void onTopicOccur(String topic, JSONObject json) {
    }
}
