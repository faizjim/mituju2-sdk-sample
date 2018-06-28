package my.mimos.mituju.v2.ilpservice.util;

import android.os.Build;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.net.ssl.HttpsURLConnection;

import my.mimos.m3gnet.libraries.util.AbsWorkerThread;
import my.mimos.m3gnet.libraries.util.FilesHandler;
import my.mimos.m3gnet.libraries.util.TLSSocketFactory;
import my.mimos.mituju.v2.ilpservice.ILPConstants;

/**
 * Created by ariffin.ahmad on 14/06/2017.
 */

public class BundleDownloader extends AbsWorkerThread {
    private int site_id              = 0;
//    private String id_token;
    private String app_id            = "";
    private int fingerprint_id       = -1;
    private int map_id               = -1;
    private String download_fp_path  = "";
    private String download_map_path = "";
    private String extract_path      = "";

    public IBundleDownloader callback;

    private interface IDownloadProgress {
        void onDownloaderStart(int content_length);
        void onDownloaderProgress(int written);
        void onDownloaderDone(long server_datetime);
    }

    public interface IBundleDownloader {
        void onDownloaderInit();
        void onDownloaderFingerprintStart(int content_length);
        void onDownloaderMapStart(int content_length);
        void onDownloaderFingerprintProgress(int written);
        void onDownloaderMapProgress(int written);
        void onDownloaderFingerprintDone(long server_datetime);
        void onDownloaderMapDone(long server_datetime);
        void onDownloaderMovingFileStart(String output_path);
        void onDownloaderMovingFileDone();
        void onDownloaderUnzipStart(int files_number, String output_path);
        void onDownloaderUnzipProgress(int number, String filename);
        void onDownloaderUnzipDone();
        void onDownloaderDone();
        void onDownloaderError(String message);
    }

    public BundleDownloader(int site_id) {
        super();
        this.site_id      = site_id;
        download_fp_path  = ILPConstants.DOWNLOADPATH + "tmp_bundle" + site_id + "_fp.db";
        download_map_path = ILPConstants.DOWNLOADPATH + "tmp_bundle" + site_id + "_map.zip";
    }

    public void start(String app_id, int fingerprint_id, int map_id, String extract_path) {
        this.fingerprint_id = -1;
        this.map_id         = -1;

//        this.id_token     = id_token;
        this.app_id         = app_id;
        this.fingerprint_id = fingerprint_id;
        this.map_id         = map_id;
        this.extract_path   = extract_path;

        Log.wtf(ILPConstants.TAG, ">>> download: request to download for app id '" + app_id + "' - fingerprint id '" + fingerprint_id + "' - map id '" + map_id + "'");
        super.start();
    }

    @Override
    public void process() {
        if (callback != null)
            callback.onDownloaderInit();
        try {
            if (fingerprint_id > -1) {
                downloadFile(ILPConstants.MIILP_SERVER_PATH_FINGERPRINT + "?app_id=" + app_id + "&fingerprint_id=" + fingerprint_id, download_fp_path, new FingerprintDownloadListener());
                Log.wtf(ILPConstants.TAG, ">>> download: removing existing fingerprint database...");
                FilesHandler.deletePath(new File(extract_path + ILPConstants.MIILP_DB_NAME));
                FilesHandler.moveFile(download_fp_path, extract_path, ILPConstants.MIILP_DB_NAME, new DBStoreListener());
            }

            if (map_id > -1) {
                downloadFile(ILPConstants.MIILP_SERVER_PATH_MAP + "?app_id=" + app_id + "&map_id=" + map_id, download_map_path, new MapDownloadListener());
                if (!stop.get())
                    FilesHandler.unzip(download_map_path, extract_path, stop, new ZipListener());
            }

            if (callback != null)
                callback.onDownloaderDone();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onDownloaderError("URL error");
        }
        catch (IOException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onDownloaderError("connection error");
        }
        catch (Exception e) {
            e.printStackTrace();
            if (callback != null)
                callback.onDownloaderError(e.getMessage());
        }
    }

    private void downloadFile(String download_path, String tmp_path, IDownloadProgress progress_callback)
        throws MalformedURLException, IOException, Exception {

        URL url                = new URL(ILPConstants.MIILP_SERVER_PROTOCOL + ILPConstants.M3GNET_URL + download_path);
//        Log.wtf(ILPConstants.TAG, ">>> download: downloading from - " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        if (conn instanceof HttpsURLConnection && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            ((HttpsURLConnection)conn).setSSLSocketFactory(new TLSSocketFactory());
        conn.setReadTimeout(10 * 1000);
        conn.setConnectTimeout(15 * 1000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);

        conn.connect();
        int response           = conn.getResponseCode();
        int content_length     = conn.getContentLength();
        long server_datetime   = conn.getHeaderFieldDate("ServerMap-Datetime", 0);
        Log.i(ILPConstants.TAG, ">>> download: server datetime '" + server_datetime + "' - '" + ILPConstants.UTC_FORMATER.format(new Date(server_datetime)) + "'");

        if (progress_callback != null)
            progress_callback.onDownloaderStart(content_length);

        InputStream is = new BufferedInputStream(conn.getInputStream());//url.openStream());
        OutputStream os = new FileOutputStream(tmp_path);

        byte data[]     = new byte[32 * 1024];
        int total       = 0;
        int count       = -1;

        Log.wtf(ILPConstants.TAG, ">>> download: begin downloading to '" + tmp_path + "'....");
        while (!stop.get() && (count = is.read(data)) != -1) {
            total += count;
            //Log.i(MiTujuApplication.TAG, ">>> download: " + total + " bytes written");
            os.write(data, 0, count);
            os.flush();
            if (progress_callback != null)
                progress_callback.onDownloaderProgress(total);
        }
        Log.wtf(ILPConstants.TAG, ">>> download: download done....");

        os.close();
        is.close();

        if (progress_callback != null)
            progress_callback.onDownloaderDone(count == -1 ? server_datetime : 0);
    }


    private class FingerprintDownloadListener implements IDownloadProgress {
        @Override
        public void onDownloaderStart(int content_length) {
            if (callback != null)
                callback.onDownloaderFingerprintStart(content_length);
        }

        @Override
        public void onDownloaderProgress(int written) {
            if (callback != null)
                callback.onDownloaderFingerprintProgress(written);
        }

        @Override
        public void onDownloaderDone(long server_datetime) {
            if (callback != null)
                callback.onDownloaderFingerprintDone(server_datetime);
        }
    }

    private class MapDownloadListener implements IDownloadProgress {
        @Override
        public void onDownloaderStart(int content_length) {
            if (callback != null)
                callback.onDownloaderMapStart(content_length);
        }

        @Override
        public void onDownloaderProgress(int written) {
            if (callback != null)
                callback.onDownloaderMapProgress(written);
        }

        @Override
        public void onDownloaderDone(long server_datetime) {
            if (callback != null)
                callback.onDownloaderMapDone(server_datetime);
        }
    }

    private class DBStoreListener implements FilesHandler.IFileHandlerMove {
        @Override
        public void onStart(String destination) {
            if (callback != null)
                callback.onDownloaderMovingFileStart(destination);
        }

        @Override
        public void onDone() {
            if (callback != null)
                callback.onDownloaderMovingFileDone();
        }

        @Override
        public void onError(String message) {
            if (callback != null)
                callback.onDownloaderError(message);
        }
    }

    private class ZipListener implements FilesHandler.IZipExtract {
        @Override
        public void onStart(int size, String target) {
            if (callback != null)
                callback.onDownloaderUnzipStart(size, target);
        }

        @Override
        public void onProgress(int count, String current_path) {
            if (callback != null)
                callback.onDownloaderUnzipProgress(count, current_path);
        }

        @Override
        public void onDone() {
            if (callback != null)
                callback.onDownloaderUnzipDone();
        }

        @Override
        public void onError(String message) {
            if (callback != null)
                callback.onDownloaderError(message);
        }
    }
}
