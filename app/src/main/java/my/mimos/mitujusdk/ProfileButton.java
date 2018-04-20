package my.mimos.mitujusdk;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * Created by ariffin.ahmad on 07/07/2017.
 */

public class ProfileButton extends ImageButton {

//    private View.OnClickListener listener = null;
    private final Drawable img_enable;
    private final Drawable img_disable;

    public ProfileButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        Resources resources = context.getResources();
        img_enable          = resources.getDrawable(R.drawable.ic_border_box_enable); // context.getDrawable(R.drawable.ic_border_box_enable);
        img_disable         = resources.getDrawable(R.drawable.ic_border_box_disable);
    }

//
//    @Override
//    public void setOnClickListener(View.OnClickListener listener) {
//        this.listener = listener;
//    }
//
//    @Override
//    public void onClick(View view) {
//
//    }

    @Override
    public void setEnabled(boolean enable) {
        super.setEnabled(enable);
        if (enable)
            setBackground(img_enable);
        else
            setBackground(img_disable);
    }
}
