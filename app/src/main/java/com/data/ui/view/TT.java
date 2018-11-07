package com.data.ui.view;

/**
 * Created by Administrator on 2017/4/17.
 */

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.widget.Toast;

/**
 * Toast统一管理类
 */
public class TT {

    private static Toast TOAST;
    private static final String TAG = "ToastUtil";
    private static Handler sHandler = new Handler(Looper.getMainLooper());


    public static void show(Context context, int resourceID) {
        show(context, resourceID, Toast.LENGTH_SHORT);
    }

    public static void show(Context context, String text) {

        safeShow(context, text, Toast.LENGTH_SHORT);
    }


    public static void show(Context context, Integer resourceID, int duration) {
        String text = context.getResources().getString(resourceID);
        safeShow(context, text, duration);
    }


    public static void show(@NonNull final Context context, @NonNull final String text, final int duration) {

        if (TOAST == null) {
            TOAST = Toast.makeText(context, text, duration);
        } else {
            TOAST.setText(text);
            TOAST.setDuration(duration);
        }

        TOAST.show();
    }



    private static void safeShow(final Context context, final String text, final int durarion) {
        if (Looper.myLooper() != Looper.getMainLooper()) {//如果不是在主线程弹出吐司，那么抛到主线程弹
            sHandler.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            show(context, text, durarion);
                        }
                    }
            );
        } else {
            show(context, text, durarion);
        }
    }
}
