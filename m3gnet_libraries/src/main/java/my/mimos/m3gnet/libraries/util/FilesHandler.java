package my.mimos.m3gnet.libraries.util;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import my.mimos.m3gnet.libraries.Constant;

/**
 * Created by ariffin.ahmad on 22/11/2017.
 */

public class FilesHandler {

    public interface IFileHandlerMove {
        void onStart(String destination);
        void onDone();
        void onError(String message);
    }

    public interface IZipExtract {
        void onStart(int size, String target);
        void onProgress(int count, String current_path);
        void onDone();
        void onError(String message);
    }

    public static void dirChecker(String dir) {
        File f = new File(dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }

    public static void deletePath(File path) {
        if (path.isDirectory())
            for (File child : path.listFiles())
                deletePath(child);

        path.delete();
    }

    public static void moveFile(String source, String target_path, String file_name, IFileHandlerMove callback) {
        // create target location folder if not exist
        dirChecker(target_path);

        if (callback != null)
            callback.onStart(target_path + file_name);
        try {
            int length      = 0;
            byte[] buffer   = new byte[1024];
            File f_source   = new File(source);
            File f_dest     = new File(target_path + file_name);
            InputStream is  = new FileInputStream(f_source);
            OutputStream os = new FileOutputStream(f_dest);

            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }

            is.close();
            os.close();

            f_source.delete();
            if (callback != null)
                callback.onDone();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onError(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            if (callback != null)
                callback.onError(e.getMessage());
        }
    }


    public static void unzip(String _zipFile, String _targetLocation, AtomicBoolean flag_stop, IZipExtract callback) {
        // create target location folder if not exist
        dirChecker(_targetLocation);

        Log.wtf(Constant.TAG, ">>> download: unzipping to '" + _targetLocation + "'....");
        try {
            if (!flag_stop.get()) {
                ZipFile zip         = new ZipFile(_zipFile);
                FileInputStream fin = new FileInputStream(_zipFile);
                ZipInputStream zin  = new ZipInputStream(fin);
                ZipEntry ze         = null;
                int count           = 0;

                if (callback != null)
                    callback.onStart(zip.size(), _targetLocation);
                while (!flag_stop.get() && (ze = zin.getNextEntry()) != null) {
                    count += 1;
                    if (callback != null)
                        callback.onProgress(count, ze.getName());

                    // create dir if required while unzipping
                    if (ze.isDirectory()) {
                        dirChecker(_targetLocation + "/" + ze.getName());
                    }
                    else {
                        FileOutputStream fout       = new FileOutputStream(_targetLocation + "/" + ze.getName());
                        BufferedOutputStream bufout = new BufferedOutputStream(fout);
                        byte[] buffer               = new byte[16 * 1024];
                        int read                    = 0;
                        while ((read = zin.read(buffer)) != -1) {
                            bufout.write(buffer, 0, read);
                        }

                        zin.closeEntry();
                        bufout.close();
                        fout.close();
                    }
                }
                zin.close();

                if (!flag_stop.get() && callback != null)
                    callback.onDone();
            }
            File tmp_file = new File(_zipFile);
            tmp_file.delete();
        } catch (Exception e) {
            if (callback != null)
                callback.onError(e.getMessage());
        }
    }

}
