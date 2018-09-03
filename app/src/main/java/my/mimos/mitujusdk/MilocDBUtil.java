package my.mimos.mitujusdk;

import android.content.Context;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import my.mimos.mituju.v2.ilpservice.util.AbsWorkerThread;


/**
 * Created by ariffin.ahmad on 21/08/2017.
 */

public class MilocDBUtil {

    public interface IMilocDBUtil {
        void onExtractStarted();
        void onExtractDone();
        void onExtractError(String error);
    }

    public static boolean isFingerPrintAvailable() {
        File file = new File(MiTujuApplication.ROOTPATH, MiTujuApplication.FINGERPRINT_DB);
        if (file != null) {
            Log.wtf(MiTujuApplication.TAG, ">>> " + MiTujuApplication.FINGERPRINT_DB + " utils - fingerprint db path: " + file.getAbsolutePath());
            return file.exists();
        }

        return false;
    }

    public static Extractor unzip(Context context, IMilocDBUtil callback) {
        Extractor worker = new Extractor(context, callback);
        worker.start();
        return worker;
    }

    private static void dirChecker(String dir) {
        File f = new File(dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }

    private static class Extractor extends AbsWorkerThread {
        private final Context context;
        private final IMilocDBUtil callback;

        public Extractor(Context context, final IMilocDBUtil callback) {
            this.context  = context;
            this.callback = callback;
        }

        @Override
        public void process() {
            long time_start = new Date().getTime();
            Log.wtf(MiTujuApplication.TAG, ">>> " + MiTujuApplication.FINGERPRINT_DB + " util: unzipping to '" + MiTujuApplication.ROOTPATH + "'....");
            try {
                ZipInputStream zin  = new ZipInputStream(context.getAssets().open(MiTujuApplication.COMPRESSED_DB));
                ZipEntry ze         = null;
                int total           = 0;

                if (callback != null)
                    callback.onExtractStarted();
                while ((ze = zin.getNextEntry()) != null) {
                    total += 1;

                    // create dir if required while unzipping
//                    Log.wtf(ILPConstants.TAG, ">>> miloc.db util: extracting '" + NicerMiTujuApplication.ROOTPATH + ze.getName() + "'....");
                    if (ze.isDirectory()) {
                        dirChecker(MiTujuApplication.ROOTPATH + ze.getName());
                    }
                    else {
                        FileOutputStream fout       = new FileOutputStream(MiTujuApplication.ROOTPATH + ze.getName());
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

                long time_end = new Date().getTime();
                Log.wtf(MiTujuApplication.TAG, ">>> " + MiTujuApplication.FINGERPRINT_DB + " util: extraction done: took " + ((time_end - time_start) / 1000) + " seconds");
                if (callback != null)
                    callback.onExtractDone();
            } catch (Exception e) {
                Log.wtf(MiTujuApplication.TAG, ">>> " + MiTujuApplication.FINGERPRINT_DB + " util: extraction error: " + e.getMessage());
                if (callback != null)
                    callback.onExtractError(e.getMessage());
            }
        }
    }
}
