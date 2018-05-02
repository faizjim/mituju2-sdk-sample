package my.mimos.mitujusdk.mqtt;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ariffin.ahmad on 23/01/2017.
 */

public class SiteConfig {
    public String name = "";
    public Attr attr   = new Attr();
    public String org  = "";
    public String site = "";
    public String id   = "";

    public class Attr {
        public GeoFence geofence     = new GeoFence();
        public IndoorData indoorData = new IndoorData();
    }

    public class GeoFence {
        public String type        = "";
        public String origin      = "";
        public int radius         = 0;
    }

    public class IndoorData {
        public String origin      = "";
        public int rotate         = 0;
        public int scale          = 0;
    }

    @Override
    public String toString() {
        String ret = "";

        ret  = "name: " + name + "\norg: " + org + "\nsite: " + site + "\nid: " + id + "\nattr: \n";
        ret += "\tgeofence: \n\t\ttype: " + attr.geofence.type + "\n\t\torigin: " + attr.geofence.origin + "\n\t\tradius: " + attr.geofence.radius + "\n";
        ret += "\tindoorData: \n\t\torigin: " + attr.indoorData.origin + "\n\t\trotate: " + attr.indoorData.rotate + "\n\t\tscale: " + attr.indoorData.scale + "\n";

        return ret;
    }

    public static SiteConfig parse(JSONObject json) {
//        Log.wtf(MiTujuApplication.TAG, " > PARSER: site config: " + json.toString());
        SiteConfig ret = new SiteConfig();

        try { ret.name = json.getString("name"); } catch (JSONException e) {}
        try { ret.org  = json.getString("org"); } catch (JSONException e) {}
        try { ret.site = json.getString("site"); } catch (JSONException e) {}
        try { ret.id   = json.getString("id"); } catch (JSONException e) {}

        try {
            JSONObject json_attr = json.getJSONObject("attr");

            try {
                JSONObject json_fence          = json_attr.getJSONObject("geofence");
                try { ret.attr.geofence.type   = json_fence.getString("type"); } catch (JSONException e) {}
                try { ret.attr.geofence.origin = json_fence.getString("origin"); } catch (JSONException e) {}
                try { ret.attr.geofence.radius = json_fence.getInt("radius"); } catch (JSONException e) {}
            } catch (JSONException e) {}

            try {
                JSONObject json_indoor = json_attr.getJSONObject("indoorData");
                try { ret.attr.indoorData.origin = json_indoor.getString("origin"); } catch (JSONException e) {}
                try { ret.attr.indoorData.rotate = json_indoor.getInt("rotate"); } catch (JSONException e) {}
                try { ret.attr.indoorData.scale = json_indoor.getInt("scale"); } catch (JSONException e) {}
            } catch (JSONException e) {}
        }
        catch (JSONException e) {}

        return ret;
    }
}
