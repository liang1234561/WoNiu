package com.data.util;


import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;


public class MobileSystemUtil {
    private static String PROFILEDIR = "";

    /**
     * @return 手机型号
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

    /**
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 获取当前手机日记存的目录
     *
     * @param context
     * @return
     */
    public static String getProfileDir(Context context) {
        if (!PROFILEDIR.equals(""))
            return PROFILEDIR;
        else if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            PROFILEDIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "wx";
            return PROFILEDIR;
        } else {
            PROFILEDIR = context.getFilesDir().getAbsolutePath();
            return PROFILEDIR;
        }
    }

    /**
     * 判断指定Service是否启动
     */
    public static boolean serviceIsRun(Context context, String serviceName) {
        ActivityManager actMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = actMgr.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo serviceInfo : services) {
            if (serviceName.equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查当前是不是有网络连接
     *
     * @return
     */
    public static boolean isOnline(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return mConnectivityManager.getActiveNetworkInfo() != null && mConnectivityManager.getActiveNetworkInfo().getState() == NetworkInfo.State.CONNECTED;
        }
        return false;
    }


    /**
     * 判断是否有移动网络连接
     *
     * @param context
     * @return
     */
    @SuppressLint("MissingPermission")
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 获取版本名字
     * @param context
     * @return
     */
    public static String getVersion(Context context) {
        String versionName = "";
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;

    }

    /**
     * 概述：删除dir目录下除指定文件名currentwritelog的所有文件
     *
     * @param dir             文件目录
     * @param currentwritelog 当前在写的log文件名 ,没有则为空
     * @return 返回删除文件个数
     * int
     * @Title: clearCacheFolder
     */
    public static void clearFolder(File dir, String currentwritelog) {
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {
                    if (child.isDirectory()) {
                        clearFolder(child, currentwritelog);
                    } else {
                        if (!child.getName().equals(currentwritelog))
                            child.delete();
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    public static byte[] File2byte(File file) {
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }
}