package my.mimos.mituju.v2.maptool;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import my.mimos.mituju.v2.maptool.R;

/**
 * Created by ariffin.ahmad on 11/07/2017.
 */

public class MarkerInfoAdapter implements GoogleMap.InfoWindowAdapter {

    private View content_view;
    private ImageView img_icon;
    private TextView text_title;
    private TextView text_snippet;

    public MarkerInfoAdapter(LayoutInflater inflater) {
        content_view = inflater.inflate(R.layout.target_marker_info, null);
        img_icon     = (ImageView) content_view.findViewById(R.id.target_info_icon);
        text_title   = (TextView) content_view.findViewById(R.id.target_info_title);
        text_snippet = (TextView) content_view.findViewById(R.id.target_info_snippet);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        render(marker);
        return content_view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    private void render(Marker marker) {
        String title   = marker.getTitle();
        String snippet = marker.getSnippet();
        Object tag     = marker.getTag();

        text_title.setText(title);
        if (snippet != null && snippet.length() > 0) {
            text_snippet.setVisibility(View.VISIBLE);
            text_snippet.setText(snippet);
        }
        else
            text_snippet.setVisibility(View.GONE);

        if (tag != null) {
            int id = (int) tag;
            img_icon.setImageResource(id);
        }
        else
            img_icon.setImageBitmap(null);
    }
}
