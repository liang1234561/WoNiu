package com.data.util.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author admin
 */
public class ImageViewCache {
    private static final String DISK_CACHE_PATH = "/diebao/image";
    private static final String DATA_CATHE_PATH = "/webimage_cache";
    private static ImageViewCache instance;
    private Map<String, SoftReference<Bitmap>> cache = new HashMap<String, SoftReference<Bitmap>>();
    private String cachePath;
    private ExecutorService writeThreadPool;

    private ImageViewCache(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cachePath = Environment.getExternalStorageDirectory() + DISK_CACHE_PATH;
        } else {
            cachePath = context.getCacheDir() + DATA_CATHE_PATH;
        }
        File file = new File(cachePath);
        if (!file.exists()) {
            file.mkdirs();
        } else if (file.isFile()) {
            file.delete();
            file.mkdirs();
        }
        writeThreadPool = Executors.newSingleThreadExecutor();
    }

    public static ImageViewCache getInstance(Context context) {
        if (instance == null) {
            instance = new ImageViewCache(context);
        }
        return instance;
    }

    public Bitmap get(String url) {
        Bitmap bitmap = null;
        SoftReference<Bitmap> sr = cache.get(url);
        if (sr != null) {
            bitmap = sr.get();
        }
        if (bitmap == null) {
            File file = new File(cachePath, getCachekey(url));
            if (file.exists()) {
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                cacheToMemory(url, bitmap);
            }
        }
        return bitmap;
    }

    public String getCachekey(String url) {
        return url.replace("[:/?.#%=&,]", "+").replace("[+]+", "+");//[+]{2,}
    }

    private void cacheToMemory(String url, Bitmap bitmap) {
        cache.put(url, new SoftReference<Bitmap>(bitmap));
    }

    public void put(String url, Bitmap bitmap) {
        cacheToMemory(url, bitmap);
        cacheToDisk(url, bitmap);
    }

    private void cacheToDisk(final String url, final Bitmap bitmap) {
        writeThreadPool.equals(new Runnable() {
            @Override
            public void run() {
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(new File(cachePath, getCachekey(url)));
                    bitmap.compress(CompressFormat.JPEG, 100, out);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public void clear() {
        File file = new File(cachePath);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                int len = files.length;
                for (int i = 0; i < len; i++) {
                    File f = files[i];
                    f.delete();
                }
            }
        }
    }
}
