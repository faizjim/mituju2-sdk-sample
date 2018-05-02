package my.mimos.m3gnet.libraries.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import my.mimos.m3gnet.libraries.R;

/**
 * Created by ariffin.ahmad on 10/04/2017.
 */

public class CircleButton extends RelativeLayout {

    public ImageView background;
    public ImageView icon;
    public TextView text_label;

    public final int color_white;
    public final int color_grey;
    public final int color_blue;

    public CircleButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        color_white = ContextCompat.getColor(context, R.color.white);
        color_grey  = ContextCompat.getColor(context, R.color.grey500);
        color_blue  = ContextCompat.getColor(context, R.color.color400);

        String label           = "";
        int icon_id            = -1;
        int color_icon         = color_blue;
        int color_circle       = color_white;
        TypedArray typed_array = context.obtainStyledAttributes(attrs, R.styleable.CircleButton);
        label                  = typed_array.getString(R.styleable.CircleButton_label);
        icon_id                = typed_array.getResourceId(R.styleable.CircleButton_icon, R.drawable.ic_videocam);
        color_icon             = typed_array.getColor(R.styleable.CircleButton_iconColor, color_blue);
        color_circle           = typed_array.getColor(R.styleable.CircleButton_iconColor, color_white);
        typed_array.recycle();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.circle_button, this, true);

        background  = (ImageView) findViewById(R.id.circlebutton_background);
        icon        = (ImageView) findViewById(R.id.circlebutton_icon);
        text_label  = (TextView) findViewById(R.id.circlebutton_label);

        if (label == null)
            label = "";

        text_label.setText(label);
        icon.setImageResource(icon_id);
        icon.setColorFilter(color_icon);
        background.setColorFilter(color_circle);
    }
}