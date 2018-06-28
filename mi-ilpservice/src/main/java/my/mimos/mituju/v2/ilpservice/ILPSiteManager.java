package my.mimos.mituju.v2.ilpservice;

import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import my.mimos.m3gnet.libraries.util.FilesHandler;
import my.mimos.m3gnet.libraries.util.JSONRequester;
import my.mimos.mituju.v2.ilpservice.db.TblSites;
import my.mimos.mituju.v2.ilpservice.struc.ILPSiteInfo;

/**
 * Created by ariffin.ahmad on 21/07/2017.
 */

public class ILPSiteManager {
    private final ILPConstants constants;
    private JSONRequester site_requester          = new JSONRequester();
    private SitesListener site_request_listener   = new SitesListener();
    private int requester_count                   = 5;
    private int requester_interval                = 5; // seconds
    private ILPSiteInfo active_site              = null;
    public final ArrayList<ILPSiteInfo> sites     = new ArrayList<>();
    public boolean flag_refreshing                = false;

    public IILPSiteListener callback;

    public interface IILPSiteListener {
        void onILPSiteRequestStart();
        void onILPSiteRequestDone();
        void onILPSiteRequestError(String message);
    }

    public ILPSiteManager(ILPConstants constants) {
        this.constants          = constants;
        site_requester.callback = site_request_listener;

        init();
    }

    private void init() {
        refreshList();
        int site_id = constants.preferences.getInt(ILPConstants.PREF_NAMES.PREF_MAP_DEFAULT_SITEID, 0);
        for (ILPSiteInfo site : sites) {
            if (site.id == site_id) {
                active_site = site;
                break;
            }
        }
    }

    private void refreshList() {
        Log.wtf(ILPConstants.TAG, "site manager: refreshing site list....");
        ArrayList<ILPSiteInfo> tmp = constants.database.tbl_sites.get();
        sites.clear();
        for (ILPSiteInfo tmp_site : tmp) {
            sites.add(tmp_site);
        }
    }

    public boolean refreshSites(String app_id) { //GoogleSignInAccount account) {
//        if (account == null)
//            return false;
//        site_request_listener.account = account;
//        site_requester.start(ILPConstants.MIILP_SERVER_PROTOCOL + ILPConstants.M3GNET_URL + ILPConstants.MIILP_SERVER_PATH_SITES + "?token=" + account.getIdToken(), requester_interval);
        site_request_listener.app_id = app_id;
        site_requester.start(ILPConstants.MIILP_SERVER_PROTOCOL + ILPConstants.M3GNET_URL + ILPConstants.MIILP_SERVER_PATH_SITES + "?app_id=" + app_id, requester_interval);
        return true;
    }

    public void setCurrent(ILPSiteInfo site) {
        if (site != null) {
            active_site = site;
            constants.editor.putInt(ILPConstants.PREF_NAMES.PREF_MAP_DEFAULT_SITEID, site.id);
            constants.editor.commit();
        }
    }

    public ILPSiteInfo getActiveSite() {
        return active_site;
    }

    private class SitesListener implements JSONRequester.IJSONRequester {
//        public GoogleSignInAccount account;
        public String app_id = "";

        @Override
        public void onJSONRequestStart() {
            flag_refreshing = true;
            if (callback != null)
                callback.onILPSiteRequestStart();
        }

        @Override
        public void onJSONRequestDone(JSONArray json_array) {
            flag_refreshing      = false;
            boolean flag_updated = false;
            Log.wtf(ILPConstants.TAG, "site activity: site query - done: count " + json_array.length());
            // clear accesiblity flag
            for (ILPSiteInfo tmp_site : sites) {
                tmp_site.flag_accesible = false;
            }
            for (int i = 0; i < json_array.length(); i++) {
                try {
                    JSONObject json         = json_array.getJSONObject(i);
                    int id                  = json.getInt(TblSites.col_id[0]);
                    int fp_id               = -1;
                    int map_id              = -1;
                    int map_type            = -1;
                    String str_org          = json.getString(TblSites.col_org[0]);
                    String str_site         = json.getString(TblSites.col_site[0]);
                    String str_fp_datetime  = json.getString("fp_datetime");
                    String str_map_datetime = json.getString("map_datetime");
                    long fp_datetime        = 0;
                    long map_datetime       = 0;
                    try { fp_id        = json.getInt(TblSites.col_fp_id[0]); } catch (Exception e) { fp_id = -1; }
                    try { map_id       = json.getInt(TblSites.col_map_id[0]); } catch (Exception e) { map_id = -1; }
                    try { map_type     = json.getInt(TblSites.col_map_type[0]); } catch (Exception e) { map_type = -1; }
                    try { fp_datetime  = ILPConstants.UTC_FORMATER.parse(str_fp_datetime).getTime(); } catch (ParseException e) { fp_datetime = 0; }
                    try { map_datetime = ILPConstants.UTC_FORMATER.parse(str_map_datetime).getTime(); } catch (ParseException e) { map_datetime = 0; }
                    Log.wtf(ILPConstants.TAG, "site manager: site query - done: id '" + id + "' - fp id '" + fp_id + "' - map id '" + map_id + "' - map type '" + map_type + "' - org '" + str_org + "' - site '" + str_site + "' - str fp datetime '" + str_fp_datetime + "' - fp datetime '" + fp_datetime + "' - str map datetime '" + str_map_datetime + "' - datetime '" + map_datetime + "'");

                    ILPSiteInfo site = null;
                    for (ILPSiteInfo tmp_site : sites) {
                        if (id == tmp_site.id && map_type == tmp_site.map_type) {
                            site                = tmp_site;
                            site.fp_id          = fp_id;
                            site.map_id         = map_id;
                            site.org            = str_org;
                            site.site           = str_site;
//                            site.flag_updated   = false;
                            site.flag_accesible = true;
                            site.updateDB();
                            break;
                        }
                    }
                    if (site == null) {
                        flag_updated = true;
                        site = constants.database.tbl_sites.addSite(id, fp_id, map_id, map_type, str_org, str_site);
                        sites.add(site);
                    }
                    site.server_fp_timestamp  = fp_datetime;
                    site.server_map_timestamp = map_datetime;
                } catch (JSONException e) { e.printStackTrace(); }
            }
            //sites = database.tbl_sites.get();
            boolean ret_updated = cleanList();
            if (ret_updated || flag_updated)
                refreshList();
            if (callback != null)
                callback.onILPSiteRequestDone();

            requester_count = 5;
//            account         = null;
        }

        @Override
        public void onJSONRequestDone(JSONObject json) {
            flag_refreshing = false;
            if (callback != null)
                callback.onILPSiteRequestDone();
            requester_count = 5;
//            account         = null;
            Log.wtf(ILPConstants.TAG, "site manager: site query - done");
            Iterator<String> itr = json.keys();
            while(itr.hasNext()) {
                String str = itr.next();
                try {
                    Log.wtf(ILPConstants.TAG, "site manager: site query - done: json[" + str + "]: " + json.get(str).toString());
                } catch (JSONException e) {}
            }
        }

        @Override
        public void onJSONRequestError(Exception e) {
            flag_refreshing = false;
            if (callback != null)
                callback.onILPSiteRequestError(e.getMessage());
            Log.wtf(ILPConstants.TAG, "site manager: site query - error: " + e.getMessage());
            if (requester_count > 0) {
                requester_count -= 1;
                Log.wtf(ILPConstants.TAG, "site manager: site query - make another request in next " + requester_interval + " seconds...");
                new Thread(new Runnable() {
                    @Override
                    public void run() { refreshSites(app_id); }
//                    public void run() { refreshSites(account); }
                }).start();
            }
            else {
                requester_count = 5;
//                account         = null;
                Log.wtf(ILPConstants.TAG, "site manager: site query - timeout...");
            }
        }

        private boolean cleanList() {
            boolean flag_updated               = false;
            ArrayList<ILPSiteInfo> tobe_remove = new ArrayList<>();

            for (ILPSiteInfo tmp_site : sites) {
                if (!tmp_site.flag_accesible && (active_site == null || active_site.id != tmp_site.id || active_site.map_type != tmp_site.map_type)) {
                    flag_updated = true;
                    tobe_remove.add(tmp_site);
                }
            }

            for (ILPSiteInfo tmp_site : tobe_remove) {
                int ret = constants.database.tbl_sites.deleteSite(tmp_site.id, tmp_site.map_type);
                sites.remove(tmp_site);
                FilesHandler.deletePath(new File(tmp_site.path));
                Log.wtf(ILPConstants.TAG, "site manager: removing - org '" + tmp_site.org + "' - site '" + tmp_site.site + "' - map type '" + tmp_site.map_type + "' -> result: " + ret);
            }

            return flag_updated;
        }

        private void refreshList() {
            ArrayList<ILPSiteInfo> tmp_sites = (ArrayList<ILPSiteInfo>) sites.clone();
            ILPSiteManager.this.refreshList();
            for (ILPSiteInfo site : sites) {
                for (ILPSiteInfo tmp_site : tmp_sites) {
                    if (site.id == tmp_site.id && site.map_type == tmp_site.map_type) {
                        site.server_fp_timestamp  = tmp_site.server_fp_timestamp;
                        site.server_map_timestamp = tmp_site.server_map_timestamp;
                        break;
                    }
                }
            }
        }
    }
}
