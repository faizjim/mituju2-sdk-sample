package my.mimos.m3gnet.libraries.auth;

import android.graphics.Bitmap;
import my.mimos.m3gnet.libraries.util.ImageFetcher;

/**
 * Created by ariffin.ahmad on 8/30/2016.
 */
public class SignedInDataStructure {
    public final String name;
    public final String name_given;
    public final String name_family;
    public final String email;
    public final String google_id;
    public Bitmap photo         = null;

    public ImageFetcher.IImageFetcher callback;

    public SignedInDataStructure(String name, String name_given, String name_family, String email, String google_id, String photo_url) {
        this.name        = name;
        this.name_given  = name_given;
        this.name_family = name_family;
        this.google_id   = google_id;
        this.email       = email;

        if (photo_url != null) {
            ImageFetcher fetcher    = new ImageFetcher(photo_url);
            fetcher.callback        = new ImageFetcher.IImageFetcher() {
                @Override
                public void onImageReady(Bitmap image) {
                    photo = image;
                    if (callback != null)
                        callback.onImageReady(photo);
                }
            };
            fetcher.start();
        }
    }
}
