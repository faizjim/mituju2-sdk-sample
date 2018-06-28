package my.mimos.mituju.v2.maptool.overlay;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import my.mimos.mituju.v2.maptool.Constant;

public class CustomMapOverlay implements TileProvider {
    public static final float TILE_SIZE    = 256f;
    protected static final int BUFFER_SIZE = 16 * 1024;

    public int zoom_level_base             = 0;
    public int zoom_level_googlemaps       = 0;
    public int zoom_level_actual           = 0;

    public String path                     = "/";

    public float getWidthRatio(float x, float bound_width) {
        return x / bound_width;

    }

    public float getHeightRatio(float y, float bound_height) {
        return y / bound_height;
    }

    public LatLngBounds getCameraBound() {
        return new LatLngBounds(new LatLng(-85.05113, -170), new LatLng(85.05113, 170));
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        zoom_level_googlemaps = zoom;
        zoom_level_actual     = zoom_level_googlemaps - zoom_level_base;
        byte[] image          = readTileImage(x, y, zoom);
        return image != null ? new Tile((int)TILE_SIZE, (int)TILE_SIZE, image) : NO_TILE; // NO_TILE;
    }

    protected byte[] readTileImage(int x, int y, int zoom) {
        InputStream in               = null;
        ByteArrayOutputStream buffer = null;

        try {
            String map_path = path + zoom + '/' + x + '/' + y + ".png";
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
            return null;
        }
        catch (OutOfMemoryError oome) {
            oome.printStackTrace();
            return null;
        }
        finally {
            if (in != null) try { in.close(); } catch (Exception e) {}
            if (buffer != null) try { buffer.close(); } catch (Exception e) {}
        }
    }
}
