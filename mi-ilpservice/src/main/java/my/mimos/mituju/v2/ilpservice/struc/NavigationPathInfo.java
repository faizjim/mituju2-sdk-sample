package my.mimos.mituju.v2.ilpservice.struc;

import java.util.ArrayList;

import my.mimos.miilp.core.algo.Location;
import my.mimos.miilp.plugin.android.model.ProfileWithMap;

/**
 * Created by ariffin.ahmad on 22/06/2017.
 */

public class NavigationPathInfo {

    public final ArrayList<NavigationProfile> navigations = new ArrayList<>();

    public enum NavigationPinType {
        NORMAL, START, TARGET, GOING_UP, GOING_DOWN
    }

    public NavigationPathInfo(Iterable<Location> paths, ProfilesInfo profiles, String last_pin_description) {
        NavigationProfile curr_profile    = null;
        NavigationProfile prev_profile    = null;

        NavigationProfile.NavigationPin last_pin = null;
        for (Location path : paths) {
            NavigationPinType type = NavigationPinType.NORMAL;

            if (curr_profile == null || !curr_profile.profile_id.equals(path.profileId)) {
                type         = NavigationPinType.START;
                prev_profile = curr_profile;
                curr_profile = getProfile(path.profileId);
                if (curr_profile == null) {
                    ProfileWithMap tmp_profile = profiles.get(path.profileId);
                    curr_profile         = addProfile(tmp_profile);
                }

                if (prev_profile != null) {
                    if (prev_profile.number < curr_profile.number)
                        prev_profile.setLastPinType(NavigationPinType.GOING_UP);
                    else
                        prev_profile.setLastPinType(NavigationPinType.GOING_DOWN);
                }
            }
            last_pin = curr_profile.addPin(type, path.x, path.y, path.description);
        }
        if (last_pin != null) {
            last_pin.type        = NavigationPinType.TARGET;
            last_pin.description = last_pin_description;
        }
    }

    public static String getTypeName(NavigationPinType type) {
        switch (type) {
            case START:
                return "start";
            case TARGET:
                return "target";
            case GOING_DOWN:
                return "going down";
            case GOING_UP:
                return "going up";
            default:
                return "normal";
        }
    }

    private NavigationProfile addProfile(ProfileWithMap profile) {
        NavigationProfile tmp = new NavigationProfile(profile.getId(), profile.getName(), profile.getFloorNumber(), profile.getMapScale());
        navigations.add(tmp);
        return tmp;
    }

    public NavigationProfile getProfile(String profile_id) {
        NavigationProfile ret = null;
        for (NavigationProfile profile : navigations) {
            if (profile.profile_id.equals(profile_id)) {
                ret = profile;
                break;
            }
        }
        return ret;
    }

    public NavigationProfile getNextProfile(String profile_id) {
        NavigationProfile ret = null;
        for (int i = 0; i < navigations.size() - 1; i++) {
            NavigationProfile profile = navigations.get(i);
            if (profile.profile_id.equals(profile_id) && i < navigations.size() - 1) {
                ret = navigations.get(i + 1);
                break;
            }
        }
        return ret;
    }

    public class NavigationProfile {
        public final String profile_id;
        public final String profile_name;
        public final int number;
        public final float scale;
        public final ArrayList<NavigationPin> pins = new ArrayList<>();

        public NavigationProfile(String profile_id, String profile_name, int number, float scale) {
            this.profile_id   = profile_id;
            this.profile_name = profile_name;
            this.number       = number;
            this.scale        = scale;
        }

        public NavigationPin addPin(NavigationPinType type, float x, float y, String description) {
            NavigationPin ret = new NavigationPin(x, y, scale, type, description);
            pins.add(ret);
            return ret;
        }

        public void setLastPinType(NavigationPinType type) {
            if (pins.size() > 0) {
                NavigationPin tmp = pins.get(pins.size() - 1);
                tmp.type          = type;
            }
        }

        public class NavigationPin {
            public final float original_x;
            public final float original_y;
            public final float scaled_x;
            public final float scaled_y;
            public NavigationPinType type;
            public String description;

            public NavigationPin(float x, float y, float scale, NavigationPinType type, String description) {
                this.original_x  = x;
                this.original_y  = y;
                this.scaled_x    = x * scale;
                this.scaled_y    = y * scale;
                this.type        = type;
                this.description = description;
            }
        }
    }
}
