package my.mimos.mitujusdk.search;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import my.mimos.mitujusdk.R;
import my.mimos.mituju.v2.ilpservice.db.TblPoints;

/**
 * Created by ariffin.ahmad on 16/06/2017.
 */

public class CardLocationItem extends RelativeLayout {

    public String id            = null;
    private TextView txt_name;
    private TextView txt_profile;

    public CardLocationItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.card_location_item, this, true);

        txt_name    = (TextView) findViewById(R.id.card_location_name);
        txt_profile = (TextView) findViewById(R.id.card_location_profile);
    }

    public void update(TblPoints.ILPPoint point) {
        this.id = point.id;
        txt_name.setText(point.name);
        txt_profile.setText(point.profile_name);
    }
}