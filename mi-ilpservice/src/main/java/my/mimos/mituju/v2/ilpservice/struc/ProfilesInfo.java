package my.mimos.mituju.v2.ilpservice.struc;

import android.graphics.BitmapFactory;
import android.util.Log;

import java.util.ArrayList;
import java.util.Hashtable;

import my.mimos.miilp.core.algo.Location;
import my.mimos.miilp.plugin.android.model.ProfileWithMap;
import my.mimos.miilplib.android.LocEngine;
import my.mimos.mituju.v2.ilpservice.ILPConstants;

/**
 * Created by ariffin.ahmad on 07/04/2017.
 */

public class ProfilesInfo {
//    private final String MAIN_IMAGE                       = "main.png";
    private final ArrayList<ProfileWithMap> profiles      = new ArrayList<>();
    private final Hashtable<String, ProfileAssets> assets = new Hashtable<>();
    private int floor_lowest                              = 0;
    private int floor_highest                             = 0;

    public int current_voting_pos                         = -1;
    public ProfileWithMap current_profile                 = null;
    public float current_x                                = 0f;
    public float current_y                                = 0f;

    public void loadProfiles(LocEngine loc_engine, String data_path) {
        Log.wtf(ILPConstants.TAG, "Mi-ILP - profile>>> loading profile list");
        profiles.clear();
        Iterable<ProfileWithMap> itr_profiles = loc_engine.getAllProfile();
        int count                       = 0;
        for (ProfileWithMap profile : itr_profiles) {
            int tmp_floor_no = profile.getFloorNumber();
            if (tmp_floor_no < floor_lowest)
                floor_lowest = tmp_floor_no;
            else if (tmp_floor_no > floor_highest)
                floor_highest = tmp_floor_no;

            loadAsset(count, data_path, profile);
            profiles.add(profile);
            if (current_profile == null)
                current_profile = profile;
            count += 1;
        }
        Log.wtf(ILPConstants.TAG, "Mi-ILP - profile>>> floor - lowest '" + floor_lowest + "' - highest '" + floor_highest + "'");
    }

    private void loadAsset(int array_position, String data_path, ProfileWithMap profile) {
        String full_path   = data_path + profile.getMapPath();
        Log.wtf(ILPConstants.TAG, "Mi-ILP - profile>>> loading profile " + profile.getName() + " asset's from '" + full_path + "'");
        int idx_last       = full_path.lastIndexOf('/');
        if (idx_last < 0)
            idx_last       = full_path.lastIndexOf('\\');

//        String filename    = null;
//        try { filename     = full_path.substring(idx_last + 1); } catch (Exception e) { filename = MAIN_IMAGE; }
        String folder_path = idx_last > 0 ? full_path.substring(0, idx_last + 1) : full_path;

        ProfileAssets profile_asset       = new ProfileAssets(array_position, profile.getId(), folder_path);
//        BitmapFactory.Options bmp_options = new BitmapFactory.Options();
//        bmp_options.inJustDecodeBounds    = true;
        profile_asset.discoverAssetFiles(folder_path);
        profile_asset.ilp_bound_height = profile.getMapHeight();
        profile_asset.ilp_bound_width  = profile.getMapWidth();
        profile_asset.ilp_rotation_deg = -Math.round(Math.round(profile.getMapAngle()));
//        BitmapFactory.decodeFile(folder_path + "/" + filename, bmp_options);
//        if (bmp_options.outWidth > 0 && bmp_options.outHeight > 0) {
//            profile_asset.ilp_bound_width  = profile.get bmp_options.outWidth;
//            profile_asset.ilp_bound_height = bmp_options.outHeight;
//        }
        assets.put(profile_asset.profile_id, profile_asset);
    }

    public void setCurrent(int voting_pos, Location user_loc, LocEngine loc_engine) {
        this.current_voting_pos = voting_pos;
        String profile_id       = user_loc.profileId;
        if (current_profile == null || (profile_id != null && !current_profile.getId().contentEquals(profile_id)))
            current_profile = loc_engine.getProfile(profile_id);

        float map_scale  = current_profile != null ? current_profile.getMapScale() : 1f;
        current_x = user_loc.x * map_scale;
        current_y = user_loc.y * map_scale;
    }

    public ProfileAssets getAsset(String profile_id) {
        return assets.get(profile_id);
    }

    public int getFloorLowest() {
        return floor_lowest;
    }

    public int getFloorHighest() {
        return floor_highest;
    }

    public int size() {
        return profiles.size();
    }

    public ProfileWithMap get(int index) {
        return profiles.get(index);
    }

    public ProfileWithMap get(String profile_id) {
        ProfileWithMap ret = null;
        for (ProfileWithMap profile : profiles) {
            if (profile.getId().equals(profile_id)) {
                ret = profile;
                break;
            }
        }
        return ret;
    }

    public void clear() {
        floor_lowest    = 0;
        floor_highest   = 0;
        current_profile = null;
        current_x       = 0f;
        current_y       = 0f;
        profiles.clear();
    }
}
