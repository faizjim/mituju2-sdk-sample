package my.mimos.mitujusdk;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import my.mimos.miilp.core.algo.Location;
import my.mimos.miilp.plugin.android.model.ProfileWithMap;
import my.mimos.mituju.v2.ilpservice.LocEngineWorker;
import my.mimos.mituju.v2.ilpservice.struc.ProfilesInfo;
import my.mimos.mituju.v2.ilpservice.util.AbsWorkerThread;
import my.mimos.mitujusdk.search.SearchView;
import my.mimos.mituju.v2.ilpservice.ILPConstants;
import my.mimos.mituju.v2.ilpservice.db.TblPoints;
import my.mimos.mituju.v2.ilpservice.struc.NavigationPathInfo;
import my.mimos.mituju.v2.maptool.MyMapFragment;
import my.mimos.mitujusdk.search.LocationListing;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL = 9999;
    public static int SCREEN_WIDTH                                = 0;
    public static int SCREEN_HEIGHT                               = 0;

    private Handler handler;
    private RelativeLayout info_layout;
    private SearchView search_view;
    private LocationListing location_listing;
    private LinearLayout btn_origin;
    private TextView text_info_title;
    private TextView text_info_description;
    private TextView text_name;
    private TextView text_user;
    private TextView text_xy;
    private TextView text_latlon;
    private TextView text_datetime;
    private TextView text_status;
    private ImageView img_ble_mode;
    private ImageView img_ilp_status;
    private ImageView img_vote0;
    private ImageView img_vote1;
    private ImageView img_vote2;
    private Button btn_direction;
    private Button btn_navigate;
    private ProfileButton btn_up;
    private ProfileButton btn_down;
    private MyMapFragment map_fragment;

    private boolean flag_restart_ilp                           = false;
    private boolean flag_acquiring                             = false;
    private float current_x                                    = -1f;
    private float current_y                                    = -1f;
    private int infobar_height                                 = 0;

    private Animation anim_blink;
    private int color_grey500;
    private int color_deep_orange200;
    private int color_blueA400;

    private MiTujuApplication app;
    private TblPoints.ILPPoint target_navigation_point         = null;
    private NavigationWorker navigation_worker                 = new NavigationWorker();
    private ModeSelectorWorker algo_worker                     = new ModeSelectorWorker();

    private ILPListener ilp_listener                           = new ILPListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        current_x         = -1f;
        current_y         = -1f;
        app               = (MiTujuApplication) getApplication();
        handler           = new Handler();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        SCREEN_HEIGHT     = dm.heightPixels;
        SCREEN_WIDTH      = dm.widthPixels;
        infobar_height    = (int) getResources().getDimension(R.dimen.info_bar_height);

        anim_blink            = AnimationUtils.loadAnimation(this, R.anim.blink);

        color_grey500         = getResources().getColor(R.color.grey500);
        color_deep_orange200  = getResources().getColor(R.color.deep_orange_200);
        color_blueA400        = getResources().getColor(R.color.blueA400);

        ActionBar action_bar = getSupportActionBar();
        if (action_bar != null)
            action_bar.hide();

        setContentView(R.layout.activity_main);

        info_layout              = (RelativeLayout) findViewById(R.id.main_info_layout);
        btn_origin               = (LinearLayout) findViewById(R.id.main_origin_button);
        search_view              = (SearchView) findViewById(R.id.main_searchview);
        location_listing         = (LocationListing) findViewById(R.id.main_location_listing);
        text_info_title          = (TextView) findViewById(R.id.main_info_title);
        text_info_description    = (TextView) findViewById(R.id.main_info_description);
        text_user                = (TextView) findViewById(R.id.main_user);
        text_name                = (TextView) findViewById(R.id.main_name);
        TextView text_version    = (TextView) findViewById(R.id.main_version);
        text_xy                  = (TextView) findViewById(R.id.main_xy);
        text_latlon              = (TextView) findViewById(R.id.main_latlon);
        text_datetime            = (TextView) findViewById(R.id.main_datetime);
        text_status              = (TextView) findViewById(R.id.main_status);
        img_ble_mode             = (ImageView) findViewById(R.id.main_ble_mode);
        img_ilp_status           = (ImageView) findViewById(R.id.main_ilp_status);
        img_vote0                = (ImageView) findViewById(R.id.main_voting_0);
        img_vote1                = (ImageView) findViewById(R.id.main_voting_1);
        img_vote2                = (ImageView) findViewById(R.id.main_voting_2);
        btn_direction            = (Button) findViewById(R.id.main_info_direction);
        btn_navigate             = (Button) findViewById(R.id.main_info_navigate);
        map_fragment             = (MyMapFragment) getSupportFragmentManager().findFragmentById(R.id.main_map);
        btn_up                   = (ProfileButton) findViewById(R.id.main_profile_up);
        btn_down                 = (ProfileButton) findViewById(R.id.main_profile_down);

        displayAlgoMode(-1);
        img_ble_mode.setClickable(true);
        img_ble_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int algo = app.algo_selector.getNextAlgo();
                displayAlgoMode(algo);
                algo_worker.start(algo);
            }
        });

        try {
            PackageInfo info = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
            String version   = info.versionName;
            text_version.setText("version: " + version);
        }
        catch (PackageManager.NameNotFoundException e) {}

        btn_up.setEnabled(false);
        btn_down.setEnabled(false);
        info_layout.setClickable(true);
        info_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { map_fragment.showTargetPoint(); }
        });
        btn_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                map_fragment.displayNextProfile();
                btn_origin.setVisibility(View.VISIBLE);
            }
        });
        btn_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                map_fragment.displayPreviousProfile();
                btn_origin.setVisibility(View.VISIBLE);
            }
        });

        btn_origin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                map_fragment.cancelUserGesture();
                btn_origin.setVisibility(View.GONE);
            }
        });
        btn_direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_direction.setVisibility(View.INVISIBLE);
                navigation_worker.start();
            }
        });
        btn_navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_direction.setVisibility(View.INVISIBLE);
                btn_navigate.setVisibility(View.INVISIBLE);
                map_fragment.startNavigation();
            }
        });
        img_ilp_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (text_xy.getVisibility() == View.VISIBLE) {
                    text_xy.setVisibility(View.GONE);
                    text_latlon.setVisibility(View.GONE);
                    text_datetime.setVisibility(View.GONE);
                }
                else {
                    text_xy.setVisibility(View.VISIBLE);
                    text_latlon.setVisibility(View.VISIBLE);
                    text_datetime.setVisibility(View.VISIBLE);
                }
            }
        });
        search_view.callback = new SearchView.ISearchView() {
            @Override
            public void onSearchMenuItemSelected(MenuItem item) {
            }

            @Override
            public void onSearchTextChanged(String old_query, String new_query) {
                List<TblPoints.ILPPoint> points;
                search_view.showProgress();
                if (new_query == null || new_query.length() <= 0)
                    points = app.ilp_constant.database.tbl_point.getAll();
                else
                    points = app.ilp_constant.database.tbl_point.getMatched(new_query);
                search_view.hideProgress();

                location_listing.updatePoints(points);
            }

            @Override
            public void onSearchAction(String query) {
            }

            @Override
            public void onSearchFocused(boolean focused) {
                if (focused) {
                    if (location_listing.getLayoutParams().height <= 1) {
                        ViewUtils.expand(location_listing, SCREEN_HEIGHT, 1000);
                        btn_up.setVisibility(View.INVISIBLE);
                        btn_down.setVisibility(View.INVISIBLE);

                        search_view.showProgress();
                        List<TblPoints.ILPPoint> points = app.ilp_constant.database.tbl_point.getAll();
                        search_view.hideProgress();
                        location_listing.updatePoints(points);
                    }
                }
                else
                    hideSearchResult();
            }
        };
        location_listing.callback = new LocationListing.ILocationListing() {
            @Override
            public void onLocationSelected(TblPoints.ILPPoint point) { showTargetLocation(point); }
        };
        map_fragment.callback = new MyMapFragment.IMyMapFragment() {
            @Override
            public void onMapReady() {
                Log.wtf(MiTujuApplication.TAG, ">>>>>>>> map ready....");
                ProfileWithMap profile = null;
                if (app.ilp_constant.isServiceRunning())
                    profile = map_fragment.displayCurrentProfile();
                if (current_x > 0f && current_y > 0f)
                    map_fragment.setCurrentPosition(); //current_x, current_y);
                if (profile != null) {
                    text_name.setText(profile.getName());
                }
            }

            @Override
            public void onUserGesture(int reason) {
                if (current_x > 0f && current_y > 0f)
                    btn_origin.setVisibility(View.VISIBLE);
            }

            @Override
            public void onDisplayProfileChanges(ProfileWithMap profile, boolean has_previous, boolean has_next) {
                btn_up.setEnabled(has_next);
                btn_down.setEnabled(has_previous);
                if (profile != null)
                    text_name.setText(profile.getName());
            }
        };
        map_fragment.startMarkerCompass(this);

        Log.wtf(MiTujuApplication.TAG, ">>>>>>>> main activity - oncreate: screen width '" + SCREEN_WIDTH + "' - screen height '" + SCREEN_HEIGHT + "'");
        app.ilp_constant.listeners.add(ilp_listener);
        gotPermissions();
    }

    private void displayAlgoMode(int algo_mode) {
        if (algo_mode < 0)
            algo_mode = app.algo_selector.getStoredAlgoMode();
        switch(algo_mode) {
            case ILPConstants.ALGO_WIFI_DEFAULT:
            case ILPConstants.ALGO_WIFI_BAYESIAN:
            case ILPConstants.ALGO_WIFI_KNN:
                img_ble_mode.setImageResource(R.drawable.ic_wifi);
                break;
            case ILPConstants.ALGO_BLE_DEFAULT:
            case ILPConstants.ALGO_BLE_BAYESIAN:
            case ILPConstants.ALGO_BLE_KNN:
                img_ble_mode.setImageResource(R.drawable.ic_bluetooth);
                break;
            default:
                img_ble_mode.setImageResource(R.drawable.ic_sync);
                break;
        }
    }

    private void gotPermissions() {
        ArrayList<String> tmp_permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
            tmp_permissions.add(Manifest.permission.INTERNET);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            tmp_permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            tmp_permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            tmp_permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED)
            tmp_permissions.add(android.Manifest.permission.ACCESS_WIFI_STATE);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED)
            tmp_permissions.add(android.Manifest.permission.CHANGE_WIFI_STATE);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED)
            tmp_permissions.add(android.Manifest.permission.BLUETOOTH);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED)
            tmp_permissions.add(android.Manifest.permission.BLUETOOTH_ADMIN);

        if (tmp_permissions.size() > 0)
            ActivityCompat.requestPermissions(this, tmp_permissions.toArray(new String[tmp_permissions.size()]), MY_PERMISSIONS_REQUEST_READ_EXTERNAL);
        else {
            boolean extracted = MilocDBUtil.isFingerPrintAvailable();
            if (extracted) {
                initILP();
            } else {
                MilocDBUtil.unzip(this, new MilocDBUtil.IMilocDBUtil() {
                    @Override
                    public void onExtractStarted() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                displayStatus("extracting fingerprint database...");
                            }
                        });
                    }

                    @Override
                    public void onExtractDone() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                displayStatus(null);
                                initILP();
                            }
                        });
                    }

                    @Override
                    public void onExtractError(final String error) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                displayStatus("extracting fingerprint database - error: " + error);
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.wtf(MiTujuApplication.TAG, "got permission result");
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL:
                gotPermissions();
                return;
        }
    }

    private void hideSearchResult() {
        ViewUtils.collapse(location_listing, SCREEN_HEIGHT, 1000);
        btn_up.setVisibility(View.VISIBLE);
        btn_down.setVisibility(View.VISIBLE);
        location_listing.clear();
    }

    @Override
    public void onBackPressed() {
        if (location_listing.getLayoutParams().height > 1) { //.getVisibility() == View.VISIBLE) {
            hideSearchResult();
            search_view.setQueryText("");
            search_view.setSearchFocusedInternal(false);
        }
        else if (map_fragment.onBackPressed()) {
            if (info_layout.getLayoutParams().height > 1)
                ViewUtils.collapse(info_layout, infobar_height, 1000);
            target_navigation_point = null;
            search_view.setVisibility(View.VISIBLE);
        }
        else
            super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            app.ilp_constant.stopService();
            app.ilp_constant.listeners.remove(ilp_listener);
            map_fragment.stopMarkerCompass();
            map_fragment.destroyTileOverlay();
        }
    }

    private void initILP() {
        if (!app.ilp_constant.isServiceRunning()) {
            app.ilp_constant.startService(MiTujuApplication.ROOTPATH, app.algo_selector.getStoredAlgoMode());
        } else {
            ProfilesInfo profile = app.ilp_constant.getProfiles();
            map_fragment.setProfiles(profile);
            displayVotingPosition(profile.current_voting_pos);

            if (profile.current_profile != null) {
                current_x = profile.current_x;
                current_y = profile.current_y;
                handler.post(new ILPLocationUpdater(profile.current_voting_pos, null));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }


    private void displayStatus(String message) {
        if (message != null && message.length() > 0) {
            text_status.setText(message);
            text_status.setVisibility(View.VISIBLE);
            search_view.setVisibility(View.INVISIBLE);
            btn_up.setVisibility(View.INVISIBLE);
            btn_down.setVisibility(View.INVISIBLE);
        }
        else {
            text_status.setText("");
            text_status.setVisibility(View.GONE);
            search_view.setVisibility(View.VISIBLE);
            btn_up.setVisibility(View.VISIBLE);
            btn_down.setVisibility(View.VISIBLE);
        }
    }

    private void showTargetLocation(TblPoints.ILPPoint point) {
        target_navigation_point = point;
        map_fragment.displayProfile(point.profile_id);

        search_view.setVisibility(View.INVISIBLE);
        ProfileWithMap target_profile = app.ilp_constant.getProfiles().get(point.profile_id);
        map_fragment.showTargetPoint(point.scaled_x, point.scaled_y, point.name, target_profile);
        text_info_title.setText(point.name);
        text_info_description.setText(target_profile.getName());
        btn_direction.setVisibility(View.VISIBLE);
        ViewUtils.expand(info_layout, infobar_height, 1000);
        btn_direction.setVisibility(View.VISIBLE);
        btn_navigate.setVisibility(View.INVISIBLE);
        onBackPressed();
    }

    private void displayVotingPosition(int pos) {
        img_vote0.setColorFilter(color_grey500);
        img_vote1.setColorFilter(color_grey500);
        img_vote2.setColorFilter(color_grey500);
        if (pos == 0)
            img_vote0.setColorFilter(color_deep_orange200);
        else if (pos == 1)
            img_vote1.setColorFilter(color_deep_orange200);
        else if (pos == 2)
            img_vote2.setColorFilter(color_deep_orange200);
    }

    private class ModeSelectorWorker extends AbsWorkerThread {
        private boolean flag_wait     = true;
        private final int sleep_msecs = 3 * 1000;    // 3 seconds
        private int algo_mode         = 0;

        public void start(int algo_mode) {
            this.algo_mode = algo_mode;
            flag_wait      = true;
            if (!running.get())
                super.start();
        }

        @Override
        public void process() {
            while (!stop.get() && flag_wait) {
                flag_wait = false;
                try { Thread.sleep(sleep_msecs); } catch (InterruptedException e) {}
            }
            if (!stop.get() && algo_mode != app.algo_selector.getStoredAlgoMode()) {
                Log.wtf(MiTujuApplication.TAG, "mode selector worker>>> timeout.....");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setCancelable(false);
                        builder.setMessage("restart ILP in " + app.algo_selector.getAlgoName(algo_mode) + " mode?...");
                        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                app.algo_selector.storeAlgoMode(algo_mode);
                                flag_restart_ilp = true;
                                app.ilp_constant.stopService();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                displayAlgoMode(-1);
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
            }
        }
    }

    private class NavigationWorker extends AbsWorkerThread {

        @Override
        public boolean preProcess() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    text_status.setText("Preparing path...");
                    text_status.setVisibility(View.VISIBLE);
                }
            });
            return true;
        }

        @Override
        public void process() {
            if (target_navigation_point != null) {
                Location target                           = new Location(target_navigation_point.profile_id, target_navigation_point.original_x, target_navigation_point.original_y, null);
                Iterable<Location> paths                  = app.ilp_constant.getNavigationPath(target);
                Log.wtf(MiTujuApplication.TAG, "");
                for (Location loc : paths) {
                    Log.wtf(MiTujuApplication.TAG, "navigation worker>>> location: " + loc.description + " - profile id: " + loc.profileId + " - x: " + loc.x + ", y: " + loc.y);
                }
                final NavigationPathInfo navigation_paths = new NavigationPathInfo(paths, app.ilp_constant.getProfiles(), target_navigation_point.name);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            map_fragment.setNavigationPath(navigation_paths);
                            btn_navigate.setVisibility(View.VISIBLE);
                        }
                    });
            }
        }

        @Override
        public void postProcess() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    text_status.setText("");
                    text_status.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    private class ILPLocationUpdater implements Runnable {
        private final int voting_pos;
        private final Location location;

        public ILPLocationUpdater(int voting_pos, Location location) {
            this.voting_pos = voting_pos;
            this.location   = location;
        }

        @Override
        public void run() {
            String profile_id = "";
            float x           = 0f;
            float y           = 0f;
            if (location != null) {
                profile_id    = location.profileId;
                x             = location.x;
                y             = location.y;
            } else {
                ProfilesInfo profiles =  app.ilp_constant.getProfiles();
                if (profiles.current_profile != null)
                    profile_id = profiles.current_profile.getId();
                x = profiles.current_x;
                y = profiles.current_y;
            }
            Log.wtf(MiTujuApplication.TAG, "main activity>>> ilp messaging - profile - voting pos '" + voting_pos + "' - profile id '" + profile_id + "' - x '" + x + "' - y '" + y + "'");

            if (flag_acquiring && (x >= 0f || y >= 0f)) {
                flag_acquiring = false;
                displayStatus(null);
            }

            map_fragment.displayCurrentProfile();
            int status_color = color_blueA400;
            if (x == -1 || y == -1) {
                status_color = color_grey500;
            }
            else {
                text_xy.setText("last x,y: " + String.format("%.4f", x) + ", " + String.format("%.4f", y));
                text_datetime.setText("last updated: " + new SimpleDateFormat("HH:mm:ss yyyy-MM-dd").format(new Date()));
                img_ilp_status.startAnimation(anim_blink);
                if (current_x != x || current_y != y) {
                    current_x = x;
                    current_y = y;
                    LatLng tmp = map_fragment.setCurrentPosition(); //current_x, current_y);
                    if (tmp != null)
                        text_latlon.setText("lat,lon: " + String.format("%.4f", tmp.latitude) + ", " + String.format("%.4f", tmp.longitude));

                    displayVotingPosition(voting_pos);
                }
            }
            img_ilp_status.setColorFilter(status_color);
        }
    }

    private class ILPListener implements LocEngineWorker.ILocEngineWorker {

        @Override
        public void onILPStarted() {
            Log.wtf(MiTujuApplication.TAG, "ILP Engine>>> ILP started....");
        }

        @Override
        public void onILPStopped() {
            Log.wtf(MiTujuApplication.TAG, "ILP Engine>>> ILP stopped....");
            if (flag_restart_ilp) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try { Thread.sleep(3000); } catch (InterruptedException e) {}
                        Log.wtf(MiTujuApplication.TAG, "ILP Engine>>> ILP restarting ilp service");
                        app.ilp_constant.startService(MiTujuApplication.ROOTPATH, app.algo_selector.getStoredAlgoMode());
                    }
                }).start();
            }
        }

        @Override
        public void onILPFailed(final String error) {
            Log.wtf(MiTujuApplication.TAG, "ILP Engine>>> ILP failed: " + error);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    displayStatus("location engine failed: " + error);
                }
            });
        }

        @Override
        public void onILPProfileLoaded() {
            Log.wtf(MiTujuApplication.TAG, "ILP Engine>>> ILP profile loaded....");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ProfilesInfo profile = app.ilp_constant.getProfiles();
                    map_fragment.setProfiles(profile);
                    map_fragment.displayCurrentProfile();
                    displayVotingPosition(profile.current_voting_pos);

                    if (profile.current_profile != null) {
                        current_x = profile.current_x;
                        current_y = profile.current_y;
                    } else {
                        flag_acquiring = true;
                        displayStatus("Acquiring location...");
                    }
                }
            });
        }

        @Override
        public void onILPProfilePointUpdated(final int voting_pos, Location location) {
            if (location != null) {
                handler.post(new ILPLocationUpdater(voting_pos, location));
            } else
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        img_ilp_status.setColorFilter(color_grey500);
                    }
                });
        }

        @Override
        public void onILPPinsLoaded() {

        }
    }
}
