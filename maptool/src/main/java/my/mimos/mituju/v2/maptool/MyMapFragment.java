package my.mimos.mituju.v2.maptool;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import my.mimos.miilp.plugin.android.model.ProfileWithMap;
import my.mimos.mituju.v2.ilpservice.struc.NavigationPathInfo;
import my.mimos.mituju.v2.ilpservice.struc.ProfileAssets;
import my.mimos.mituju.v2.ilpservice.struc.ProfilesInfo;
//import my.mimos.mituju.v2.maptool.overlay.MyMapOverlay;
import my.mimos.mituju.v2.maptool.overlay.CustomMapOverlay;
import my.mimos.mituju.v2.maptool.overlay.ZoomLevelOverwriteOverlay;

/**
 * Created by ariffin.ahmad on 01/06/2017.
 */

public class MyMapFragment extends SupportMapFragment
        implements OnMapReadyCallback, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraMoveStartedListener {

//    public static int MAP_ROTATION_FIX           = 180;                  // in degree

    private static final int ROTATE_MARGIN       = 2;
    private static final float ROUTE_ALPHA       = 0.5f;
    private static final int PADDING_SIZE        = 50;                  // in pixel density
    private static final int MARKER_SIZE         = 24;                  // in pixel density
    private static final int MARKER_CAP_SIZE     = 12;                  // in pixel density
    private static final int MAP_NAVIGATION_TILT = 45;                  // in pixel density

//    private LatLngBounds map_bounds              = null;
    private LatLng current_pos                   = null;
    private float current_bearing                = 0;
    private int prev_degree                      = 0;

//    private final MyMapOverlay map_overlay      = new MyMapOverlay();
    public CustomMapOverlay map_overlay; // = new ZoomLevelOverwriteOverlay();
    private AnimationSet animations;
    private BitmapDescriptor bm_active;
    private BitmapDescriptor bm_null;
    private BitmapDescriptor bm_endcap;
    private TileOverlay tile_overlay;
    private Marker marker_position;

    private Kompas kompas                        = null;
    private boolean flag_user_gesture            = false;
    private boolean flag_navigate_mode           = false;
    public GoogleMap google_map;
    public IMyMapFragment callback;

    private ProfilesInfo profiles               = null;
    private ProfileAssets current_profile_asset = new ProfileAssets(0, "", "");
    private String target_profile_id            = "";
    private Marker marker_target                = null;
    private Marker marker_endcap                = null;
    private Polyline polyline_route             = null;
    private Polyline polyline_border            = null;
    private NavigationPathInfo navigation_paths = null;

    private Handler handler;
    private GoogleMap.InfoWindowAdapter target_info_adapter;

    public interface IMyMapFragment {
        void onMapReady();
        void onUserGesture(int reason);
        void onDisplayProfileChanges(ProfileWithMap profile, boolean has_previous, boolean has_next);
    }

    public MyMapFragment() {
        getMapAsync(this);
        animations   = new AnimationSet(false);
        handler      = new Handler();
    }

    public void setProfiles(ProfilesInfo profiles) {
        this.profiles = profiles;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup view_group, Bundle savedInstanceState) {
        Resources res = getResources();
        map_overlay   = new ZoomLevelOverwriteOverlay(res.getColor(R.color.grey300), res.getDisplayMetrics().density);
        initMarkerIcons();
        return super.onCreateView(inflater, view_group, savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap google_map) {
        target_info_adapter = getInfoWindow(); //new MarkerInfoAdapter(getActivity().getLayoutInflater());
        int padding         = Math.round((float)PADDING_SIZE * getResources().getDisplayMetrics().density);
        this.google_map     = google_map;
        this.google_map.setMapType(GoogleMap.MAP_TYPE_NONE);
        this.google_map.setOnCameraMoveStartedListener(this);
        this.google_map.setOnCameraMoveListener(this);
        this.google_map.setPadding(padding, 0, padding, 0);
        this.google_map.setInfoWindowAdapter(target_info_adapter);
        this.tile_overlay   = this.google_map.addTileOverlay(new TileOverlayOptions().tileProvider(map_overlay));

        UiSettings ui_settings = this.google_map.getUiSettings();
        ui_settings.setMapToolbarEnabled(false);                    // disable google map toolbar
        ui_settings.setCompassEnabled(false);                       // disable map compass when rotate

        this.tile_overlay.clearTileCache();

        Log.wtf(Constant.TAG, "map fragment: map ready - zoom level - min '" + google_map.getMinZoomLevel() + "' - max '" + google_map.getMaxZoomLevel() + "'");
        this.google_map.setLatLngBoundsForCameraTarget(map_overlay.getCameraBound());

        if (callback != null)
            callback.onMapReady();
    }

    public GoogleMap.InfoWindowAdapter getInfoWindow() {
        return new MarkerInfoAdapter(getActivity().getLayoutInflater());
    }

    public BitmapDescriptor initActiveBitmap() {
        return null;
    }

    public BitmapDescriptor initNullBitmap() {
        return null;
    }

    public BitmapDescriptor initEndCapBitmap() {
        return null;
    }

    private void initMarkerIcons() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view_marker_active = inflater.inflate(R.layout.marker_view, null);
        View view_marker_null   = inflater.inflate(R.layout.marker_view, null);

        ((ImageView)view_marker_active.findViewById(R.id.marker_icon)).setColorFilter(getResources().getColor(R.color.blue400));
        ((ImageView)view_marker_null.findViewById(R.id.marker_icon)).setColorFilter(getResources().getColor(R.color.grey500));

        bm_active               = initActiveBitmap();
        bm_null                 = initNullBitmap();
        bm_endcap               = initEndCapBitmap();

        if (bm_active == null)
            bm_active           = getMarkerIcon(view_marker_active);
        if (bm_null == null)
            bm_null             = getMarkerIcon(view_marker_null);
        if (bm_endcap == null)
            bm_endcap           = getMarkerIcon(R.drawable.ic_circle_cap);
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) { // user gesture on map
//            Log.wtf(Constant.TAG, "main activity>>> camera moving - user gesture.....");
            flag_user_gesture = true;
            if (callback != null)
                callback.onUserGesture(reason);
        }
        else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION) { // user tapped on map
//            Log.wtf(Constant.TAG, "main activity>>> camera moving - user tapped something...");
        }
        else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION) { // app moved the camera
//            Log.wtf(Constant.TAG, "main activity>>> camera moving - app move the camera....");
        }
    }

    @Override
    public void onCameraMove() {
        CameraPosition cam_pos = this.google_map.getCameraPosition();

        if (cam_pos.bearing == current_bearing)
            return;

        current_bearing = cam_pos.bearing;
        if (marker_position != null && !flag_navigate_mode)
            marker_position.setRotation(-current_bearing);
    }

    public String getProfileIdOnDisplay() {
        return current_profile_asset != null ? current_profile_asset.profile_id : null;
    }

    public boolean onBackPressed() {
        if (flag_user_gesture || marker_target != null) {                     // remove marker will also set user gesture flag to false
            if (marker_target != null)
                marker_target.remove();
            if (marker_endcap != null)
                marker_endcap.remove();
            if (polyline_route != null)
                polyline_route.remove();
            if (polyline_border != null)
                polyline_border.remove();
            flag_navigate_mode = false;
            flag_user_gesture  = false;

            navigation_paths    = null;
            polyline_route      = null;
            polyline_border     = null;
            marker_endcap       = null;
            marker_target       = null;
            target_profile_id   = null;

            if (profiles.current_profile != null && !current_profile_asset.profile_id.equals(profiles.current_profile.getId()))
                displayCurrentProfile();
            setCurrentPosition(true);

            return true;
        }
        else
            return false;
    }

    public void startMarkerCompass(Context context) {
        if (kompas == null) {
            kompas          = new Kompas(context);
            kompas.callback = new KompassListener();
//            sensor_service      = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
//            sensor_rotation_vector = sensor_service.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
////            sensor_magnetometer = sensor_service.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }
        kompas.register();
//        sensor_service.registerListener(sensor_listener, sensor_rotation_vector, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopMarkerCompass() {
        if (kompas != null)
            kompas.unregister();
//        if (sensor_service != null)
//            sensor_service.unregisterListener(sensor_listener, sensor_rotation_vector);
    }

    public LatLng getLatLon(float x, float y) {
//        world's lat lon
//        double lon   =  x / current_profile_asset.ilp_bound_width * 360f - 180f;
//        double n_lat = Math.PI - (2f * Math.PI * y) / current_profile_asset.ilp_bound_height;
        double lon   = map_overlay.getWidthRatio(x, current_profile_asset.ilp_bound_width) * 360f - 180f;
        double n_lat = Math.PI - (2f * Math.PI * map_overlay.getHeightRatio(y, current_profile_asset.ilp_bound_height));
        double lat   = Math.toDegrees(Math.atan(Math.sinh(n_lat)));

        return new LatLng(lat, lon);
    }


    public static BitmapDescriptor getMarkerIcon(View view) {
        view.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private BitmapDescriptor getMarkerIcon(int resource_id) {
        int size                 = Math.round((float)MARKER_CAP_SIZE * getResources().getDisplayMetrics().density);
        Drawable vector_drawable = getResources().getDrawable(resource_id); // getContext().getDrawable(resource_id);
        vector_drawable.setBounds(0, 0, size, size);

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vector_drawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void cancelUserGesture() {
        flag_user_gesture = false;
        displayCurrentProfile();
        setCurrentPosition();
    }

    public void startNavigation() {
        flag_user_gesture  = false;
        flag_navigate_mode = true;
        displayCurrentProfile();
        marker_position.setRotation(0f);
        setCurrentPosition(true);
    }

    public void showTargetPoint() {
        if (marker_target != null) {
            displayProfile(target_profile_id);
            flag_user_gesture = true;

            marker_target.showInfoWindow();
            google_map.animateCamera(CameraUpdateFactory.newLatLng(marker_target.getPosition()));
        }
    }

    public void showTargetPoint(float x, float y, String name, ProfileWithMap profile) {
        LatLng tmp        = getLatLon(x, y);
        target_profile_id = profile.getId();
        marker_target     = google_map.addMarker(new MarkerOptions().position(tmp).title(name).snippet(profile.getName()));
        showTargetPoint();
    }

    public void destroyTileOverlay() {
        tile_overlay.remove();
    }

    protected LatLng snapToRoute(float x, float y) {
        LatLng shortest_latlon  = current_pos;

        NavigationPathInfo.NavigationProfile nav_profile = navigation_paths.getProfile(current_profile_asset.profile_id);
        if (nav_profile != null) {
            float shortest_distance = Constant.MAXIMUM_SNAP_DISTANCE;
            for (NavigationPathInfo.NavigationProfile.NavigationPin pin : nav_profile.pins) {
                LatLng lat_lon = getLatLon(pin.scaled_x, pin.scaled_y);
                float tmp_distance = (Math.abs(x - pin.scaled_x) + Math.abs(y - pin.scaled_y)) / profiles.current_profile.getMapScale();
//                        Log.wtf(Constant.TAG, ">>>>>>>> display current position(" + x + "," + y + ") - distance '" + tmp_distance + "': pin " + count + " - desc '" + pin.description + "' - (" + pin.original_x + ", " + pin.original_y + ") - (" + pin.scaled_x + ", " + pin.scaled_y + ")");
                if (tmp_distance <= shortest_distance) {
                    shortest_distance = tmp_distance;
                    shortest_latlon   = lat_lon;
                    Log.wtf(Constant.TAG, ">>>>>>>> display current position(" + x + "," + y + ") - snap to - desc '" + pin.description + "' - (" + pin.scaled_x + ", " + pin.scaled_y + ") - distance '" + tmp_distance + "'");
                }
            }
        }

        return shortest_latlon;
    }

    public LatLng setCurrentPosition() {
//        float x = profiles.current_point.x;
//        float y = profiles.current_point.y;

//        Log.wtf(Constant.TAG, ">>>>>>>> display current position(" + profiles.current_x + "," + profiles.current_y + "): " + (flag_user_gesture ? "gesture mode" : "") + " - " + (flag_navigate_mode ? "navigation mode" : ""));
        if (profiles.current_x > 0 && profiles.current_y > 0) {
            current_pos = getLatLon(profiles.current_x, profiles.current_y);

            if (flag_navigate_mode && navigation_paths != null)
                current_pos = snapToRoute(profiles.current_x, profiles.current_y);
        }

        if (marker_position != null) {
            if (profiles.current_x < 0 || profiles.current_y < 0)
                marker_position.setIcon(bm_null);
            else
                marker_position.setIcon(bm_active);
            marker_position.setPosition(current_pos);
        }
        else if (current_pos != null)
            marker_position = google_map.addMarker(new MarkerOptions()
                    .position(current_pos)
                    .anchor(0.5f, 0.5f)
                    .icon(bm_active));

        if (!flag_user_gesture)
            setCurrentPosition(false);

        return current_pos;
    }

    public Marker addMarker(float x, float y, BitmapDescriptor icon) {
        LatLng loc = getLatLon(x, y);
        if (icon == null)
            icon = bm_null;
        return google_map.addMarker(new MarkerOptions()
                .position(loc)
                .anchor(0.5f, 0.5f)
                .icon(icon));
    }

    public void setCurrentPosition(boolean original_bearing) {

        if (current_pos != null) {
            CameraPosition tmp_pos             = google_map.getCameraPosition();
            CameraPosition.Builder tmp_builder = new CameraPosition.Builder(tmp_pos).target(current_pos);
            if (original_bearing && current_bearing > 0f) {
                this.current_bearing = 0f;
                tmp_builder.bearing(0f);
            }
            if (flag_navigate_mode) {
                tmp_builder.tilt(MAP_NAVIGATION_TILT);
            }
            else {
                tmp_builder.tilt(0);
                tmp_builder.bearing(0f);
            }
            google_map.animateCamera(CameraUpdateFactory.newCameraPosition(tmp_builder.build()));
        }
    }

    private boolean hasNextProfile() {
        return current_profile_asset != null && current_profile_asset.array_position < profiles.size() - 1;
    }

    private boolean hasPreviousProfile() {
        return current_profile_asset != null && current_profile_asset.array_position > 0;
    }

    public ProfileWithMap displayNextProfile() {
        ProfileWithMap ret = null;
        if (hasNextProfile()) {
            ret = profiles.get(current_profile_asset.array_position + 1);
            displayProfile(ret.getId());

            if (marker_target == null && current_profile_asset.profile_id.equals(profiles.current_profile.getId()))
                flag_user_gesture = false;
            else
                flag_user_gesture = true;
        }
        return ret;
    }

    public ProfileWithMap displayPreviousProfile() {
        ProfileWithMap ret = null;
        if (hasPreviousProfile()) {
            ret = profiles.get(current_profile_asset.array_position - 1);
            displayProfile(ret.getId());

            if (marker_target == null && current_profile_asset.profile_id.equals(profiles.current_profile.getId()))
                flag_user_gesture = false;
            else
                flag_user_gesture = true;
        }
        return ret;
    }

    public ProfileWithMap displayCurrentProfile() {
        if (google_map == null)
            return null;

        ProfileWithMap ret = null;
        if (!flag_user_gesture) {
            ret      = profiles != null ? profiles.current_profile : null;
            if (ret != null && (current_profile_asset == null || !current_profile_asset.profile_id.equals(ret.getId())))
                displayProfile(ret.getId());
        }
        return ret;
    }

    public ProfileWithMap displayProfile(String profile_id) {
        if (current_profile_asset != null && current_profile_asset.profile_id.equals(profile_id))
            return null;
        current_profile_asset = profiles.getAsset(profile_id);

        if (marker_endcap != null) {
            marker_endcap.remove();
            marker_endcap = null;
        }
        if (polyline_route != null) {
            polyline_route.remove();
            polyline_route = null;
        }
        if (polyline_border != null) {
            polyline_border.remove();
            polyline_border = null;
        }
        if (current_profile_asset == null)
            return null;

        Log.wtf(Constant.TAG, "map fragment: display profile '" + profile_id + "': asset's zoom level - min '" + current_profile_asset.getMinZoomLevel() + "' - max '" + current_profile_asset.getMaxZoomLevel() + "'");
        Log.wtf(Constant.TAG, "map fragment: display profile '" + profile_id + "': zoom level - min '" + google_map.getMinZoomLevel() + "' - max '" + google_map.getMaxZoomLevel() + "'");
        map_overlay.path   = current_profile_asset.folder_path;
        if (google_map != null && current_profile_asset.count() > 0) {
            google_map.resetMinMaxZoomPreference();
            google_map.setMinZoomPreference(current_profile_asset.getMinZoomLevel() + map_overlay.zoom_level_base);
            google_map.setMaxZoomPreference(current_profile_asset.getMaxZoomLevel() + map_overlay.zoom_level_base);
        }

        if (tile_overlay != null) {
            tile_overlay.clearTileCache();
            Log.wtf(Constant.TAG, "map fragment: local tile: refreshed - " + map_overlay.path);
        }
        else
            Log.wtf(Constant.TAG, "map fragment: local tile: not refreshed - tile overlay unavailable...");

        drawRoute();

        if (marker_position != null) {
            if (profile_id.equals(profiles.current_profile.getId()))
                marker_position.setAlpha(1f);
            else
                marker_position.setAlpha(ROUTE_ALPHA);
        }
        if (marker_target != null) {
            if (target_profile_id.equals(current_profile_asset.profile_id))
                marker_target.setVisible(true);
            else
                marker_target.setVisible(false);
        }

        if (callback != null)
            callback.onDisplayProfileChanges(profiles.get(current_profile_asset.profile_id), hasPreviousProfile(), hasNextProfile());

        return profiles.get(profile_id);
    }

    public void setNavigationPath(NavigationPathInfo navigation_paths) {
        this.navigation_paths = navigation_paths;
        drawRoute();
    }

    private void drawRoute() {
        if (navigation_paths == null)
            return;

        NavigationPathInfo.NavigationProfile nav_profile = navigation_paths.getProfile(current_profile_asset.profile_id);
        if (nav_profile == null)
            return;

        ArrayList<LatLng> path = new ArrayList<>();
        int count              = 0;
        for (NavigationPathInfo.NavigationProfile.NavigationPin pin : nav_profile.pins) {
            LatLng lat_lon = getLatLon(pin.scaled_x, pin.scaled_y);
            path.add(lat_lon);
            Log.wtf(Constant.TAG, "main activity>>> showing navigation path: pin '" + count + "' - type '" + NavigationPathInfo.getTypeName(pin.type) + "' - x '" + pin.original_x + "' - y '" + pin.original_y + "' - desc '" + pin.description + "' - lat '" + lat_lon.latitude + "' - lon '" + lat_lon.longitude + "'");

            int icon_id          = -1;
            String str_direction = "";
            if (pin.type == NavigationPathInfo.NavigationPinType.GOING_UP) {
                icon_id       = R.drawable.ic_arrow_up;
                str_direction = "going up to ";
            }
            else if (pin.type == NavigationPathInfo.NavigationPinType.GOING_DOWN) {
                icon_id       = R.drawable.ic_arrow_down;
                str_direction = "going down to ";
            }

            if (icon_id > -1) {
                NavigationPathInfo.NavigationProfile next_nav_profile = navigation_paths.getNextProfile(current_profile_asset.profile_id);
                marker_endcap = google_map.addMarker(new MarkerOptions()
                        .position(lat_lon)
                        .anchor(0.5f, 0.5f)
                        .icon(bm_endcap));
                marker_endcap.setTag(icon_id);
                if (next_nav_profile != null) {
                    marker_endcap.setTitle(pin.description);
                    marker_endcap.setSnippet(str_direction + next_nav_profile.profile_name);
                    marker_endcap.showInfoWindow();
                }
            }

            count += 1;
        }

        PolylineOptions polyline_options    = new PolylineOptions();
        PolylineOptions polyline_opt_border = new PolylineOptions();
        polyline_options.width(20);
        polyline_opt_border.width(24);
        polyline_options.addAll(path);
        polyline_opt_border.addAll(path);


        float[] hsv_blue = new float[3];
        float[] hsv_red  = new float[3];
        int alpha_value  = Math.round(255f * ROUTE_ALPHA);
        Color.colorToHSV(getResources().getColor(R.color.blue400), hsv_blue);
        Color.colorToHSV(getResources().getColor(R.color.red), hsv_red);

        polyline_route                   = google_map.addPolyline(polyline_options);
        polyline_route.setColor(Color.HSVToColor(alpha_value, hsv_blue));
        polyline_route.setZIndex(1000);        // draw on top tile
        polyline_route.setJointType(JointType.ROUND);
        polyline_route.setStartCap(new RoundCap());
        polyline_route.setEndCap(new RoundCap());
        polyline_border                   = google_map.addPolyline(polyline_opt_border);
        polyline_border.setColor(Color.HSVToColor(alpha_value, hsv_red));
        polyline_border.setZIndex(990);        // draw on top tile
        polyline_border.setJointType(JointType.ROUND);
        polyline_border.setStartCap(new RoundCap());
        polyline_border.setEndCap(new RoundCap());

        customPath(polyline_route, polyline_border);
    }

    public void customPath(Polyline main_line, Polyline border_line) {

    }

    private class KompassListener implements Kompas.IKompas {
        private AtomicBoolean flag_animation_running = new AtomicBoolean(false);
        private CameraPosition tobe_pos              = null;

        @Override
        public void onKompasUpdated(float degree) {
            int rotate    = -Math.round(current_bearing);
            int i_degree  = (int)degree;
//            Log.wtf(Constant.TAG, "map fragment>>> kompass updated - degree '" + degree + "' - rotate '" + rotate + "' - map rotation fix '" + MAP_ROTATION_FIX + "'");

            if (marker_position == null)
                return;

            // only rotate if different more than margin
            if (Math.abs(i_degree - prev_degree) < ROTATE_MARGIN)
                return;

            if (!flag_navigate_mode) {
                int tmp_to_rotate = current_profile_asset.ilp_rotation_deg + i_degree + rotate; // MAP_ROTATION_FIX + i_degree + rotate;
                marker_position.setRotation(tmp_to_rotate);
            }
            else if (!flag_user_gesture) {
                int tmp_to_rotate = current_profile_asset.ilp_rotation_deg + i_degree; //MAP_ROTATION_FIX + i_degree;

//                Log.wtf(Constant.TAG, "map fragment>>> kompass updated - camera - width: " + getView().getMeasuredWidth() + " - height: " + getView().getMeasuredHeight() + " - i degree: " + i_degree + " - current bearing: " + rotate + " - to rotate: " + tmp_to_rotate + "...");
                CameraPosition tmp_pos                 = google_map.getCameraPosition();
                CameraPosition.Builder cam_pos_builder = new CameraPosition.Builder(tmp_pos);
                cam_pos_builder.bearing(tmp_to_rotate);
                cam_pos_builder.target(current_pos);

                tobe_pos = cam_pos_builder.build();
                if (!flag_animation_running.get())
                    animate(tobe_pos);
            }
            prev_degree = i_degree;
        }

        private void animate(CameraPosition cam_pos) {
            tobe_pos = null;
            flag_animation_running.set(true);
            google_map.animateCamera(CameraUpdateFactory.newCameraPosition(cam_pos), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    restart();
                }

                @Override
                public void onCancel() {
                    restart();
                }

                private void restart() {
                    if (tobe_pos != null) {
                        new Thread(new Runnable() { @Override public void run() {
                            handler.post(new Runnable() { @Override public void run() { animate(tobe_pos); } });
                        } }).start();
                    }
                    else
                        flag_animation_running.set(false);
                }
            });
        }
    }

//    private class SensorListener implements SensorEventListener {
//        private AtomicBoolean flag_animation_running = new AtomicBoolean(false);
//        private CameraPosition tobe_pos              = null;
//
//        @Override
//        public void onSensorChanged(SensorEvent event) {
//            int rotate      = -Math.round(current_bearing);
//
//            if (marker_position == null)
//                return;
//
//            final float mx  = event.values[0];
//            final float my  = event.values[1], my2 = my * my;
//            final float mz  = event.values[2], mz2 = mz * mz;
//
////            if (app.site_config != null && app.site_config.attr != null && app.site_config.attr.indoorData != null)
////                rotate      = app.site_config.attr.indoorData.rotate;
////            else
////                return;
//
//            // rotate around x axis into xy plane. After rotation:
//            //   x' = x
//            //   y' = sgn(y) * sqrt(z*z + y*y)
//            //   z' = 0
//            // magnitude of the vector stays the same (left as an exercise to the
//            // reader). The sign of the y value of the rotated vector follows the
//            // sign of the original y value.
//            double yp  = Math.copySign(1.0f, my) * Math.sqrt(mz2 + my2);
//
//            // apply filter to compass value
//            double deg = 180.0 * Math.atan2(mx, yp) / Math.PI;
//            int i_deg  = (int)deg;
//            //deg        = filter(rotate_filter_alpha, deg, prev_degree);
////            String out = String.format(Locale.US, "rotate: %d - [%6.2f %6.2f %6.2f] deg %6.1f (%d) - modified deg %d", rotate, mx, my, mz, deg, i_deg, 180 - i_deg);
////            Log.wtf(Constant.TAG, "fragment ilp>>>>>>> " + out);
//
//            // only rotate if different more than margin
//            if (Math.abs(deg - prev_degree) < ROTATE_MARGIN)
//                return;
//
//            if (!flag_navigate_mode) {
//                int tmp_to_rotate = MAP_ROTATION_FIX - i_deg + rotate;
//                marker_position.setRotation(tmp_to_rotate);
//            }
//            else if (!flag_user_gesture) {
//                int tmp_to_rotate = MAP_ROTATION_FIX - i_deg;
//
////                GeodeticCalculator geo_calc   = new GeodeticCalculator();
////                GlobalCoordinates coor_start  = new GlobalCoordinates(current_pos.latitude, current_pos.longitude);
////                GlobalCoordinates coor_target = geo_calc.calculateEndingGlobalCoordinates(Ellipsoid.WGS84, coor_start, tmp_to_rotate, 100f);
////                LatLng map_target             = new LatLng(coor_target.getLatitude(), coor_target.getLongitude());
//
//
////                Projection projection = google_map.getProjection();
////                Point marker_pos      = projection.toScreenLocation(current_pos);
////                Point pos_half_above  = new Point(marker_pos.x, marker_pos.y - (getView().getMeasuredWidth() / 2));
////                LatLng new_pos        = projection.fromScreenLocation(pos_half_above);
//
////                Log.wtf(Constant.TAG, "main activity>>> camera - width: " + getView().getMeasuredWidth() + " - height: " + getView().getMeasuredHeight() + " - i deg: " + i_deg + " - current bearing: " + rotate + " - to rotate: " + tmp_to_rotate + "...");
//                CameraPosition tmp_pos                 = google_map.getCameraPosition();
//                CameraPosition.Builder cam_pos_builder = new CameraPosition.Builder(tmp_pos);
//                cam_pos_builder.bearing(tmp_to_rotate);
//                cam_pos_builder.target(current_pos);
////                if (flag_navigate_mode)
////                    cam_pos_builder.tilt(MAP_NAVIGATION_TILT);
////                else {
////                    cam_pos_builder.tilt(0);
////                    cam_pos_builder.bearing(0f);
////                }
//
//                tobe_pos = cam_pos_builder.build();
//                if (!flag_animation_running.get())
//                    animate(tobe_pos);
//            }
//            prev_degree = i_deg;
//        }
//
//        @Override
//        public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        }
//
//        private void animate(CameraPosition cam_pos) {
//            tobe_pos = null;
//            flag_animation_running.set(true);
//            google_map.animateCamera(CameraUpdateFactory.newCameraPosition(cam_pos), new GoogleMap.CancelableCallback() {
//                @Override
//                public void onFinish() {
//                    restart();
//                }
//
//                @Override
//                public void onCancel() {
//                    restart();
//                }
//
//                private void restart() {
//                    if (tobe_pos != null) {
//                        new Thread(new Runnable() { @Override public void run() {
//                            handler.post(new Runnable() { @Override public void run() { animate(tobe_pos); } });
//                        } }).start();
//                    }
//                    else
//                        flag_animation_running.set(false);
//                }
//            });
//        }
//    }
}
