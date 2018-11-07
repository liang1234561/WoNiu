package com.data.util.image;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressLint("AppCompatCustomView")
public class SmartImageView extends ImageView {
    private Context context;
    private ExecutorService threadPool = Executors.newCachedThreadPool();

    public SmartImageView(Context context) {
        super(context);
        this.context = context;
    }

    public SmartImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public SmartImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    public void setImageUrl(String url) {
        setImageUrl(url, null);
    }

    /**
     * @param url
     * @param
     */
    public void setImageUrl(String url, Integer fallback) {
        setImageUrl(url, fallback, null, null);
    }

    @SuppressLint("HandlerLeak")
    public void setImageUrl(String url, final Integer failureCallback, Integer downloadingCallbak, final Integer toRound) {
        WebImage webImage = new WebImage(url, context);
        SmartImageTask task = new SmartImageTask(webImage);
        task.setListener(new SmartImageTask.OnCompleteListener() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                if (bitmap != null) {
                    if (toRound != null) {
                        bitmap = toRoundBitmap(bitmap);
                    }
                    setImageBitmap(bitmap);
                } else if (failureCallback != null) {
                    setImageResource(failureCallback);
                }
            }
        });
        if (downloadingCallbak != null) {
            setImageResource(downloadingCallbak);
        }
        threadPool.submit(task);
    }

    /**
     * 转换图片成圆形
     *
     * @param bitmap 传入Bitmap对象
     * @return
     */
    public Bitmap toRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }
        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right,
                (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top,
                (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        return output;
    }

    public void setImageUrltoRound(String url) {
        setImageUrl(url, null, 1);
    }

    public void setImageUrl(String url, Integer fallback, Integer toRound) {
        setImageUrl(url, fallback, null, toRound);
    }

    public void setURL(String url) {
        WebImage webImage = new WebImage(url, context);
        Bitmap bitmap = webImage.getBitmap();
        if (bitmap != null) {
            setImageBitmap(bitmap);
        }
    }
}
