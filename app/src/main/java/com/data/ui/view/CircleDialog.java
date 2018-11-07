package com.data.ui.view;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import com.juns.wechat.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by GodRui on 2017/12/28.
 */

public class CircleDialog {

    private Dialog proDialog;
    //private Context context;

    private static volatile CircleDialog circleDialog;
    private Timer timer;

    public static CircleDialog getInstance() {
        if (circleDialog == null) {
            synchronized (CircleDialog.class) {
                if (circleDialog == null) {
                    circleDialog = new CircleDialog();
                }
            }
        }
        return circleDialog;
    }

    private CircleDialog() {
    }

    public void showDialog(Context context) {
        if (proDialog == null || (!proDialog.isShowing())) {
            if (context == null || ((Activity) context).isFinishing()) {
                return;
            }
            proDialog = new Dialog(context, R.style.new_circle_progress);
            proDialog.setCancelable(false);
            proDialog.show();
            proDialog.setContentView(R.layout.view_circle_progress);//自己定义布局

            Animation operatingAnim = AnimationUtils.loadAnimation(context, R.anim.imrotate);
            LinearInterpolator lin = new LinearInterpolator();
            operatingAnim.setInterpolator(lin);
            proDialog.findViewById(R.id.progress_circle_imageview)
                    .setAnimation(operatingAnim);
           /* animationDrawable = (AnimationDrawable) progress_imageview.getDrawable();
            animationDrawable.start();*/

            timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    dismissDialog();
                }
            }, 30000);
        } else {
            dismissDialog();
        }
    }

    /**
     * 加载控件消失
     */
    public void dismissDialog() {
        if (proDialog != null) {
            if (proDialog.isShowing()) {
                proDialog.dismiss();
                proDialog = null;
            }

        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }
}
