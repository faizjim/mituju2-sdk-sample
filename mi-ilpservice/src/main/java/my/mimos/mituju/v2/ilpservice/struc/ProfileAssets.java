package my.mimos.mituju.v2.ilpservice.struc;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import my.mimos.m3gnet.libraries.Constant;

/**
 * Created by ariffin.ahmad on 01/06/2017.
 */

public class ProfileAssets {
    public final int array_position;
    public final String profile_id;
    public final String folder_path;
    public final List<ProfileZoomLevel> zoom_levels = new ArrayList<>();
    public int ilp_rotation_deg                     = 0;
    public float ilp_bound_width                    = 1024;
    public float ilp_bound_height                   = 1024;

    public ProfileAssets(int array_position, String profile_id, String folder_path) {
        this.array_position = array_position;
        this.profile_id     = profile_id;
        this.folder_path    = folder_path;
    }

    public boolean discoverAssetFiles(String path) {
        String[] list;
        list = (new File(path)).list();
        if (list != null) {
            Log.wtf(Constant.TAG, "listAssetFiles - path: " + path + " - length: " + list.length);

            if (list != null && list.length > 0) {
                // This is a folder
                boolean pass          = true;
                String regex          = "\\d+";
                List<String> list_str = new LinkedList<>();
                for (String directory : list) {
                    Log.wtf(Constant.TAG, "listAssetFiles - directory: " + directory);
                    pass = directory.matches(regex);
                    if (pass)
                        list_str.add(directory);
                }

                if (list_str.size() > 0)
                    setZoomLevel(list_str);
            } else {
                // This is a file
                // TODO: add file name to an array list
            }
        }

        return true;
    }

    private void setZoomLevel(List<String> folders){
        int scale = folders.size();
        scale--;
        zoom_levels.clear();
        for(String folder : folders){
            ProfileZoomLevel tmp = new ProfileZoomLevel(scale, folder);
            Log.wtf(Constant.TAG, "listAssetFiles - folder scan - folder: " + folder + " - scale: " + scale);
            zoom_levels.add(new ProfileZoomLevel(scale, folder));
            scale--;
        }
        Collections.sort(zoom_levels, new SortByZoomLevel());
        for (ProfileZoomLevel z : zoom_levels) {
            Log.wtf(Constant.TAG, "listAssetFiles - zoom level: " + z.zoom_level + " >>>>> location: " + z.location + " - detail scale: " + z.detail_scale + " - scale: " + z.scale);
        }
    }

    public int count() {
        return zoom_levels.size();
    }

    public int getMinZoomLevel(){
        return zoom_levels.get(0).zoom_level;
    }

    public int getMaxZoomLevel(){
        return zoom_levels.get(zoom_levels.size() - 1).zoom_level;
    }
//
//    //Mainly to know how large is the width of the map
//    public double getMinTotalYFolder(){
//        double value = Double.parseDouble(profiles.get(0).location);
//        return Math.pow(2,value);
//    }

    public class ProfileZoomLevel {
        public final double scale;
        public final double detail_scale;
        public String location             = "";
        public int zoom_level              = 1;

        public ProfileZoomLevel(double scale, String location) {
            this.scale        = scale;
            this.detail_scale = Math.pow(2.0, -this.scale);
            this.location     = location;
            try {
                if (location != null && location.length() > 0)
                    this.zoom_level   = Integer.parseInt(location);
            } catch (Exception e) {}
        }
    }

    private class SortByZoomLevel implements Comparator<ProfileZoomLevel> {
        @Override
        public int compare(ProfileZoomLevel z1, ProfileZoomLevel z2) {
            return z1.zoom_level - z2.zoom_level;
        }
    }
}
