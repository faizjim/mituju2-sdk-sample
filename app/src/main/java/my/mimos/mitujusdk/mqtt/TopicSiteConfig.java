package my.mimos.mitujusdk.mqtt;

import org.json.JSONObject;

import my.mimos.m3gnet.libraries.mqtt.AbsTopic;

/**
 * Created by ariffin.ahmad on 5/13/2016.
 */
public class TopicSiteConfig extends AbsTopic {
    private MQTTServer mqtt_server;
    private final String org;
    private final String site;

    public TopicSiteConfig(MQTTServer mqtt_server, String org, String site) {
        super("Org Site info");
        this.mqtt_server = mqtt_server;
        this.org       = org;
        this.site      = site;
    }

    @Override
    public String[] getTopics() {
        return new String[] { "m3g/cfg/org/" + org + "/" + site };
    }

    @Override
    public void onTopicOccur(String topic, String message) {

    }

    @Override
    public void onTopicOccur(String topic, JSONObject json) {
        if (mqtt_server.callback != null)
            mqtt_server.callback.onMQTTTopicSiteConfig(SiteConfig.parse(json));
    }
}
