package my.mimos.mituju.v2.ilpservice;

import android.content.Context;
import android.util.Log;

import my.mimos.m3gnet.libraries.util.AbsWorkerThread;
import my.mimos.m3gnet.libraries.util.CircularBuffer;
import my.mimos.miilp.core.algo.Location;
import my.mimos.miilp.core.model.Profile;
import my.mimos.miilplib.android.LocEngine;
import my.mimos.mituju.v2.ilpservice.db.DBMiTuju;
import my.mimos.mituju.v2.ilpservice.struc.ProfilesInfo;
import my.mimos.utility.core.license.LicenseException;

/**
 * Created by ariffin.ahmad on 07/04/2017.
 */

public class LocEngineWorker {

    private final QueryWorker query_worker;
    public final ProfilesInfo profiles      = new ProfilesInfo();
    public final int algo_mode;

    protected final DBMiTuju database;
    protected final LocEngine loc_engine;

    public boolean flag_profile_loaded      = false;
    public String failed_message            = "";
    public String data_path                 = "";
    public ILocEngineWorker callback;

    public interface ILocEngineWorker {
        void onILPStarted();
        void onILPStopped();
        void onILPFailed(String message);
        void onILPProfileLoaded();
        void onILPProfilePointUpdated(int voting_position, Location user_loc);
        void onILPPinsLoaded();
    }

    public LocEngineWorker(Context context, DBMiTuju database) {
        this(context, database, ILPConstants.ALGO_WIFI_DEFAULT);
    }

    public LocEngineWorker(Context context, DBMiTuju database, int algo_mode) {
        this.algo_mode = algo_mode;
        if (algo_mode == ILPConstants.ALGO_BLE_DEFAULT || algo_mode == ILPConstants.ALGO_BLE_BAYESIAN || algo_mode == ILPConstants.ALGO_BLE_KNN) {
            query_worker  = new QueryWorker(ILPConstants.BLE_SCAN_INTERVAL);
//            algo = LocEngine.LocAlgo.BLE_BAYESIAN; //.BLE_DEFAULT;
        }
        else
            query_worker  = new QueryWorker(ILPConstants.WIFI_SCAN_INTERVAL);

        Log.wtf(ILPConstants.TAG, "Mi-ILP - Selected Algorithm:>>>>> " + getAlgoName(algo_mode) + " - update interval: " + (query_worker.interval_msecs / 1000) + " seconds");
        this.loc_engine = new LocEngine(context, getAlgo(algo_mode));
        this.database   = database;
    }

    private LocEngine.LocAlgo getAlgo(int algo_mode) {
        LocEngine.LocAlgo ret = LocEngine.LocAlgo.ROUND_ROBIN;
        switch (algo_mode) {
            case ILPConstants.ALGO_WIFI_DEFAULT:
                ret = LocEngine.LocAlgo.WIFI_DEFAULT;
                break;
            case ILPConstants.ALGO_WIFI_BAYESIAN:
                ret = LocEngine.LocAlgo.WIFI_BAYESIAN;
                break;
            case ILPConstants.ALGO_WIFI_KNN:
                ret = LocEngine.LocAlgo.WIFI_KNN;
                break;
            case ILPConstants.ALGO_BLE_DEFAULT:
                ret = LocEngine.LocAlgo.BLE_DEFAULT;
                break;
            case ILPConstants.ALGO_BLE_BAYESIAN:
                ret = LocEngine.LocAlgo.BLE_BAYESIAN;
                break;
            case ILPConstants.ALGO_BLE_KNN:
                ret = LocEngine.LocAlgo.BLE_KNN;
                break;
            case ILPConstants.ALGO_GPS_PURE:
                ret = LocEngine.LocAlgo.GPS_PURE;
                break;
            case ILPConstants.ALGO_GPS_SNAP:
                ret = LocEngine.LocAlgo.GPS_SNAP;
                break;
        }
        return ret;
    }

    private String getAlgoName(int algo_mode) {
        String ret = "Round Robin";
        switch (algo_mode) {
            case ILPConstants.ALGO_WIFI_DEFAULT:
                ret = "Wifi Default";
                break;
            case ILPConstants.ALGO_WIFI_BAYESIAN:
                ret = "Wifi Bayesian";
                break;
            case ILPConstants.ALGO_WIFI_KNN:
                ret = "Wifi KNN";
                break;
            case ILPConstants.ALGO_BLE_DEFAULT:
                ret = "BLE Default";
                break;
            case ILPConstants.ALGO_BLE_BAYESIAN:
                ret = "BLE Bayesian";
                break;
            case ILPConstants.ALGO_BLE_KNN:
                ret = "BLE KNN";
                break;
            case ILPConstants.ALGO_GPS_PURE:
                ret = "GPS Pure";
                break;
            case ILPConstants.ALGO_GPS_SNAP:
                ret = "GPS Snap";
                break;
        }
        return ret;
    }

    public boolean start() {
        return start(this.data_path);
    }

    public boolean start(String data_path) {
        failed_message      = "";
        flag_profile_loaded = false;
        this.data_path      = data_path;
        Log.wtf(ILPConstants.TAG, "Mi-ILP - init>>> loading data from '" + this.data_path + "'");
        try {
            loc_engine.loadData(this.data_path);
            Log.wtf(ILPConstants.TAG, "Mi-ILP - init>>> loading data: done");
            query_worker.start();
            return true;
        }
        catch (Exception e) {
            String message = "error loading ILP data: " + e.getMessage();
            Log.wtf(ILPConstants.TAG, message);
            if (callback != null) {
                failed_message = message;
                callback.onILPFailed(message);
            }
            e.printStackTrace();
            return false;
        }
    }

    public void stop() {
        query_worker.stop();
    }

    protected boolean isRunning() {
        return query_worker.running.get();
    }

    public void setBackgroundMode(boolean background_mode) {
        if (background_mode)
            query_worker.modifyInterval(ILPConstants.BACKGROUND_SCAN_INTERVAL);
        else
            query_worker.modifyInterval(query_worker.interval_msecs);
    }

    public Iterable<Location> getNavigationPath(Location target) {
        float tmp_scale       = profiles.current_profile.getMapScale();
        Location start        = new Location(profiles.current_profile.getId(), profiles.current_x / tmp_scale, profiles.current_y / tmp_scale, null);

        return loc_engine.getPath(start, target);
    }


    private class QueryWorker extends AbsWorkerThread {

        private final int interval_msecs;
        private LocationLoaderWorker loader_worker         = new LocationLoaderWorker();
        public final CircularBuffer<Location> point_buffer = new CircularBuffer<>(Location.class, 3);

        public QueryWorker(int interval_msecs) {
            super(interval_msecs);
            this.interval_msecs = interval_msecs;
        }

        @Override
        public boolean preProcess() {
            Log.i(ILPConstants.TAG, "starting Location Querying service...");
            String message = null;
            try {
                loc_engine.start();
                profiles.loadProfiles(loc_engine, data_path);
                flag_profile_loaded = true;
                if (callback != null)
                    callback.onILPProfileLoaded();
                loader_worker.start();
            } catch (LicenseException e) {
                message = "license error while starting Location Querying service: " + e.getMessage();
            } catch (Exception e) {
                message = "error while starting Location Querying service: " + e.getMessage();
            }

            if (message == null) {
                if (callback != null)
                    callback.onILPStarted();
                return true;
            }
            else {
                Log.wtf(ILPConstants.TAG, message);
                if (callback != null) {
                    failed_message = message;
                    callback.onILPFailed(message);
                }
                return false;
            }
        }

        @Override
        public void process() {
            if (callback != null) {
                try {
                    int tmp_pos         = -1;
                    Location user_loc   = loc_engine.getLoc();
                    Log.wtf(ILPConstants.TAG, "user_loc: " + (user_loc != null ? user_loc.profileId + ": " + user_loc.x + "," + user_loc.y : "-null-"));
                    if (ILPConstants.FLAG_ILP_LOC_VOTING) {
                        point_buffer.store(user_loc);
                        VotingData data = getVotingLocation();
                        user_loc        = data.prof_point;
                        tmp_pos         = data.position;
                        Log.wtf(ILPConstants.TAG, "voting_loc: (" + tmp_pos + ")" + (user_loc != null ? user_loc.profileId + ": " + user_loc.x + "," + user_loc.y : "-null-"));
                    }
                    if (user_loc != null)
                        profiles.setCurrent(tmp_pos, user_loc, loc_engine);
                    callback.onILPProfilePointUpdated(tmp_pos, user_loc);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void postProcess() {
            loader_worker.stop();
            profiles.clear();
            try {
                loc_engine.stop();
            }
            catch (Exception e) {
                Log.wtf(ILPConstants.TAG, "error while stopping Location Querying service: " + e.getMessage());
            }
            Log.i(ILPConstants.TAG, "Location Querying stopped...");

            if (callback != null)
                callback.onILPStopped();
        }

        public VotingData getVotingLocation() {
            Location[] array = point_buffer.getArrayDesc();
            VotingData ret        = new VotingData();
            ret.position          = 0;

            if (array[0] != null && array[1] != null && !array[0].profileId.equals(array[1].profileId) && array[2] != null && array[1].profileId.equals(array[2].profileId))
                ret.position      = 1;

            Location tmp_pp = array[ret.position];
            ret.prof_point       = tmp_pp != null ? new Location(tmp_pp.profileId, tmp_pp.x, tmp_pp.y, tmp_pp.description) : null;// array[ret.position];

            return ret;
        }

        private class VotingData {
            public int position              = -1;
            public Location prof_point  = null;
        }
    }

    private class LocationLoaderWorker extends AbsWorkerThread {

        @Override
        public void process() {
            Log.wtf(ILPConstants.TAG, "Mi-ILP >>> reloading location db: start");
            database.tbl_point.clearRows();
            try {
                int tmp_count = profiles.size();
                for (int i = 0; i < tmp_count; i++) {
                    Profile profile = profiles.get(i);
                    Log.wtf(ILPConstants.TAG, "Mi-ILP >>> updating pin for profile: " + profile.getName() + "....");
                    database.tbl_point.addPoint(loc_engine.getDestinationPin(profile.getId()), profile.getName(), profile.getMapScale());
                }
            }
            catch (Exception e) {
                Log.wtf(ILPConstants.TAG, "Mi-ILP >>> error while loading location db: " + e.getMessage());
            }
            if (callback != null)
                callback.onILPPinsLoaded();
            Log.wtf(ILPConstants.TAG, "Mi-ILP >>> reloading location db: done");
        }
    }
}
