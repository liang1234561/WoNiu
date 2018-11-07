package com.data.util.image;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;


public class SmartImageTask implements Runnable {
    private WebImage webImage;
    private OnCompleteListener listener;

    public SmartImageTask(WebImage webImage) {
        this.webImage = webImage;
    }

    public void setListener(OnCompleteListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        Bitmap bitmap = webImage.getBitmap();
        if (listener != null) {
            Message msg = Message.obtain();
            msg.obj = bitmap;
            listener.sendMessage(msg);
        }
    }

    public static abstract class OnCompleteListener extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bitmap bitmap = (Bitmap) msg.obj;
            onSuccess(bitmap);
        }

        public abstract void onSuccess(Bitmap bitmap);
    }


}
