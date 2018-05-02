package my.mimos.mitujusdk;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by ariffin.ahmad on 16/06/2017.
 */

public class ViewUtils {
    public static void expand(final View view, final int target_height, int duration_msecs) {
        view.getLayoutParams().height = 1;
//        view.setVisibility(View.VISIBLE);

        Animation animation           = new Animation() {
            @Override
            protected void applyTransformation(float interpolated_time, Transformation t) {
                view.getLayoutParams().height = interpolated_time == 1 ? target_height : (int)(target_height * interpolated_time);
                view.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setDuration(duration_msecs);
        view.startAnimation(animation);
    }

    public static void collapse(final View view, final int initial_height, int duration_msecs) {
        Animation animation           = new Animation() {
            @Override
            protected void applyTransformation(float interpolated_time, Transformation t) {
                if (interpolated_time == 1)
                    view.getLayoutParams().height =  1;//view.setVisibility(View.GONE);
                else {
                    view.getLayoutParams().height = initial_height - (int)(initial_height * interpolated_time);
                    view.requestLayout();
                }
            }
            @Override

            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setDuration(duration_msecs);
        view.startAnimation(animation);
    }

    public static Snackbar generateConfirmationSnakebar(View main_layout, String message, final Runnable on_expired_task) {
        return generateConfirmationSnakebar(main_layout, message, new View.OnClickListener() { @Override public void onClick(View view) { } }, on_expired_task);
    }

    public static Snackbar generateConfirmationSnakebar(View main_layout, String message, View.OnClickListener cancelled_listener, final Runnable on_expired_task) {
        Snackbar snackbar = Snackbar.make(main_layout, message, Snackbar.LENGTH_SHORT);
        snackbar.setAction("Cancel", cancelled_listener);
        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT && on_expired_task != null)
                    on_expired_task.run();
            }
        });
        return snackbar;
    }

    public static AlertDialog.Builder generateConfirmationDialog(Activity activity, String message, DialogInterface.OnClickListener confirm_listener, DialogInterface.OnClickListener cancel_listener) {
        if (confirm_listener == null) {
            confirm_listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {}
            };
        }
        if (cancel_listener == null) {
            cancel_listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {}
            };
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message)
                .setPositiveButton("Confirm", confirm_listener)
                .setNegativeButton("Cancel", cancel_listener);

        return builder;
    }
}