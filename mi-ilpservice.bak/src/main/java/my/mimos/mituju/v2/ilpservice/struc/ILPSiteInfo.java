package my.mimos.mituju.v2.ilpservice.struc;

import android.util.Log;

import my.mimos.mituju.v2.ilpservice.ILPConstants;
import my.mimos.mituju.v2.ilpservice.db.TblSites;
import my.mimos.mituju.v2.ilpservice.util.BundleDownloader;

/**
 * Created by ariffin.ahmad on 14/06/2017.
 */

public class ILPSiteInfo {
    public final int id;
    public int fp_id;
    public int map_id;
    public int map_type;
    public String org;
    public String site;
    public long fp_timestamp;
    public long map_timestamp;
    public final String path;
    public boolean flag_extracting   = false;
    public long server_fp_timestamp  = 0;
    public long server_map_timestamp = 0;
//    public boolean flag_updated      = false;
    public boolean flag_accesible    = false;
    public int current_fp_percent    = -1;
    public int current_map_percent   = -1;

    private final BundleDownloader downloader;
    private final TblSites tbl_sites;

    public IILPSiteInfo callback;

    public interface IILPSiteInfo {
        void onDownloadStart();
        void onFingerprintDownloadStart();
        void onMapDownloadStart();
        void onFingerprintDownloading(int percent);
        void onMapDownloading(int percent);
        void onFingerprintStoring();
        void onMapUnzipping();
        void onFingerprintDone(long timestamp);
        void onMapDone(long timestamp);
        void onMapError(String message);
        void onDone();
    }

    public ILPSiteInfo(TblSites tbl_sites, int id, int fp_id, int map_id, int map_type, String org, String site, long fp_timestamp, long map_timestamp) {
        this.tbl_sites      = tbl_sites;
        this.id             = id;
        this.fp_id          = fp_id;
        this.map_id         = map_id;
        this.map_type       = map_type;
        this.org            = org;
        this.site           = site;
        this.fp_timestamp   = fp_timestamp;
        this.map_timestamp  = map_timestamp;
        this.path           = ILPConstants.ROOTPATH + "siteid." + id + "/";
        this.flag_accesible = true;

        downloader          = new BundleDownloader(this.id);
        downloader.callback = new DownloadListener();
    }

    public int updateDB() {
        return tbl_sites.updateSite(id, fp_id, map_id, map_type, org, site);
    }

    public void startDownload(String app_id) {
        Log.wtf(ILPConstants.TAG, ">>> ilp site info: request to download for org '" + org + "' site '" + site + "' - fingerprint timestamp '" + fp_timestamp + "' - server fingerprint timestamp '" + server_fp_timestamp + "'");
        Log.wtf(ILPConstants.TAG, ">>> ilp site info: request to download for org '" + org + "' site '" + site + "' - map timestamp '" + map_timestamp + "' - server map timestamp '" + server_map_timestamp + "'");
        downloader.start(app_id, server_fp_timestamp > fp_timestamp ? fp_id : -1, server_map_timestamp > map_timestamp ? map_id : -1, path);
    }

    public void stopDownload() {
        downloader.stop();
    }

    private class DownloadListener implements BundleDownloader.IBundleDownloader {
        private long fp_content_length   = 0;
        private long map_content_length  = 0;
        private long server_fp_datetime  = 0;
        private long server_map_datetime = 0;

        @Override
        public void onDownloaderInit() {
//            flag_updated        = false;
            if (callback != null)
                callback.onDownloadStart();
        }

        @Override
        public void onDownloaderFingerprintStart(int content_length) {
            current_fp_percent     = -1;
            this.fp_content_length = content_length;
            if (callback != null)
                callback.onFingerprintDownloadStart();
        }

        @Override
        public void onDownloaderMapStart(int content_length) {
            current_map_percent     = -1;
            this.map_content_length = content_length;
            if (callback != null)
                callback.onMapDownloadStart();
        }

        @Override
        public void onDownloaderFingerprintProgress(int written) {
            int tmp_percent = (int) ((long)written * (long)100 / fp_content_length);
            if (tmp_percent != current_fp_percent) {
                current_fp_percent = tmp_percent;
                if (callback != null)
                    callback.onFingerprintDownloading(current_fp_percent);
            }
        }

        @Override
        public void onDownloaderMapProgress(int written) {
            int tmp_percent = (int) ((long)written * (long)100 / map_content_length);
            if (tmp_percent != current_map_percent) {
                current_map_percent = tmp_percent;
                if (callback != null)
                    callback.onMapDownloading(current_map_percent);
            }
        }

        @Override
        public void onDownloaderFingerprintDone(long server_datetime) {
            current_fp_percent = -1;
            server_fp_datetime = server_datetime;
            if (server_fp_datetime > 0)
                tbl_sites.updateFingerprintTimestamp(id, map_type, server_fp_datetime);
            Log.wtf(ILPConstants.TAG, "ilp info: download - done - server fingerprint datettime '" + this.server_fp_datetime + "'");
        }

        @Override
        public void onDownloaderMapDone(long server_datetime) {
            current_map_percent = -1;
            server_map_datetime = server_datetime;
            if (server_map_datetime > 0)
                tbl_sites.updateMapTimestamp(id, map_type, server_map_datetime);
            Log.wtf(ILPConstants.TAG, "ilp info: download - done - server map datettime '" + this.server_map_datetime + "'");
        }

        @Override
        public void onDownloaderMovingFileStart(String output_path) {
            if (callback != null)
                callback.onFingerprintStoring();
        }

        @Override
        public void onDownloaderMovingFileDone() {
            fp_timestamp = server_fp_datetime;
            if (callback != null)
                callback.onFingerprintDone(fp_timestamp);
        }

        @Override
        public void onDownloaderUnzipStart(int files_number, String output_path) {
            flag_extracting = true;
            if (callback != null)
                callback.onMapUnzipping();
        }

        @Override
        public void onDownloaderUnzipProgress(int number, String filename) {

        }

        @Override
        public void onDownloaderUnzipDone() {
            flag_extracting = false;
            map_timestamp = server_map_datetime;
            if (callback != null)
                callback.onMapDone(map_timestamp);
        }

        @Override
        public void onDownloaderDone() {
//            flag_updated    = true;
            if (callback != null)
                callback.onDone();
        }

        @Override
        public void onDownloaderError(String message) {
            current_fp_percent  = -1;
            current_map_percent = -1;
            Log.wtf(ILPConstants.TAG, "ilp info: download - error  '" + message + "'");
            if (callback != null)
                callback.onMapError(message);
        }
    }
}
