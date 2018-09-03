package my.mimos.mituju.v2.maptool.overlay;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import my.mimos.mituju.v2.maptool.Constant;

/**
 * Created by ariffin.ahmad on 01/06/2017.
 */

public class ZoomLevelOverwriteOverlay extends CustomMapOverlay {
    private static final int BUFFER_SIZE      = 16 * 1024;
    private byte[] color_white;
    private float total_pixels                = TILE_SIZE;
    private float first_tile_size             = TILE_SIZE;


    public ZoomLevelOverwriteOverlay(int null_color, float dp_scale) {
        setZoomLevelBase(5);
        int new_size    = (int)((float)TILE_SIZE * dp_scale);
        Bitmap bitmap   = Bitmap.createBitmap(new_size , new_size, Bitmap.Config.ARGB_8888);
        Canvas canvas   = new Canvas(bitmap);
        Paint paint     = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(null_color);
        canvas.drawRect(0F, 0F, (float) new_size, (float) new_size, paint);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        color_white                  = stream.toByteArray();
//        color_white                  = null;
    }

    public void setZoomLevelBase(int base) {
        zoom_level_base = base;
        total_pixels    = (float) Math.pow(2, zoom_level_base) * TILE_SIZE;
        first_tile_size = 1f * TILE_SIZE;
    }

    @Override
    public float getWidthRatio(float x, float bound_width) {
        return (first_tile_size + x / bound_width * TILE_SIZE) / total_pixels;

    }

    @Override
    public float getHeightRatio(float y, float bound_height) {
        return (first_tile_size + y / bound_height * TILE_SIZE) / total_pixels;
    }

    @Override
    public LatLngBounds getCameraBound() {
        int no_tiles = (1 << zoom_level_base);
        float lon_span = 360f / no_tiles;
        float lon_min  = -180f + 1 * lon_span;

        float merc_max = 180f - (1f / (float)no_tiles) * 360f;
        float merc_min = 180f - ((1f + 1f) / (float)no_tiles) * 360f;
        float lat_max  = (float) toLatitude(merc_max);
        float lat_min  = (float) toLatitude(merc_min);
//        Log.wtf(Constant.TAG, "local tile: no tiles '" + no_tiles + "' - lon span '" + lon_span + "' - lon min '" + lon_min + "' - merc min '" + merc_min + "' - merc max '" + merc_max + "'");
        return new LatLngBounds(new LatLng(lat_min, lon_min), new LatLng(lat_max, lon_min + lon_span));
    }

    private double toLatitude(double merc) {
        double radians = Math.atan(Math.exp(Math.toRadians(merc)));
        return Math.toDegrees(2 * radians) - 90;
    }

    @Override
    protected byte[] readTileImage(int x, int y, int zoom) {
        int first_tile               = Math.round(Math.round(Math.pow(2, zoom_level_actual)));
        InputStream in               = null;
        ByteArrayOutputStream buffer = null;
//        Log.wtf(Constant.TAG, "local tile: x - " + x + ", y - " + y + ", z - " + zoom + " : first tile '" + first_tile + "'");

        if ((x - first_tile) < 0 || (y - first_tile) < 0)
            return color_white;

        try {
            String map_path = path + zoom_level_actual + '/' + (x - first_tile) + '/' + (y - first_tile) + ".png";
//            Log.wtf(Constant.TAG, "local tile: x - " + x + ", y - " + y + ", z - " + zoom + " : '" + map_path + "'");
            File file       = new File(map_path);
            in              = new FileInputStream(file);
            buffer          = new ByteArrayOutputStream();
            Log.wtf(Constant.TAG, "local tile: '" + map_path + "'");

            int n_read;
            byte[] data = new byte[BUFFER_SIZE];
            while ((n_read = in.read(data, 0, BUFFER_SIZE)) != -1) {
                buffer.write(data, 0, n_read);
            }
            buffer.flush();

            return buffer.toByteArray();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            return color_white;
        }
        catch (OutOfMemoryError oome) {
            oome.printStackTrace();
            return color_white;
        }
        finally {
            if (in != null) try { in.close(); } catch (Exception e) {}
            if (buffer != null) try { buffer.close(); } catch (Exception e) {}
        }
    }

}
