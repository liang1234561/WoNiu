package com.data.util.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class WebImage implements SmartImage {
    ImageViewCache cache;
    private String url;

    public WebImage(String url, Context context) {
        this.url = url;
        this.cache = ImageViewCache.getInstance(context);
    }

    public Bitmap getBitmap() {
        Bitmap bitmap = null;
        bitmap = cache.get(url);
        if (bitmap == null) {
            bitmap = getBitmapFromUrl(url);
            if (bitmap != null) {
                cache.put(url, bitmap);
            }
        }
        return bitmap;
    }

    private Bitmap getBitmapFromUrl(String urlStr) {
        Bitmap bitmap = null;
        try {
            URL url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(5000);
            InputStream in = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(in);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
