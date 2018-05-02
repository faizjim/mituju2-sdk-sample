package my.mimos.m3gnet.libraries.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import my.mimos.m3gnet.libraries.Constant;

/**
 * Created by ariffin.ahmad on 8/26/2016.
 */
public class ImageFetcher extends AbsWorkerThread {

    public interface IImageFetcher {
        void onImageReady(Bitmap image);
    }

    private final String url;
    public IImageFetcher callback;

    public ImageFetcher(String url) {
        super();
        this.url = url;
    }

    @Override
    public void process() {
        InputStream is          = null;
        BufferedInputStream bis = null;
        Log.wtf(Constant.TAG, "image fetcher>>> fetching: " + url);
        try {
            URLConnection conn = new URL(url).openConnection();
            conn.connect();
            is          = conn.getInputStream();
            bis         = new BufferedInputStream(is, 8192);
            Bitmap bmp  = BitmapFactory.decodeStream(bis);
            if (callback != null)
                callback.onImageReady(bmp);
            Log.wtf(Constant.TAG, "image fetcher>>> done");
        }
        catch (IOException e) {
            Log.wtf(Constant.TAG, "image fetcher>>> exception: " + e.getMessage());
        }
        finally {
            try { bis.close(); } catch (Exception e) {}
            try { is.close(); } catch (Exception e) {}
        }
    }

    public static Bitmap Base64toBitmap(String base64) {
        InputStream is = new ByteArrayInputStream(Base64.decode(base64.getBytes(), Base64.DEFAULT));
        Bitmap ret     = BitmapFactory.decodeStream(is);

        return ret;
    }
}
