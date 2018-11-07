package com.data.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Created by root on 18-3-28.
 */

public class ClientId {

    private static final String TAG = ClientId.class.getName();

    @SuppressLint("NewApi")
    public static void setClientId(byte[] b, Context context) {
        File file = new File(MobileSystemUtil.getProfileDir(context) + File.separator + "clientid");
        try {
            if (file.exists()) {
                if (file.createNewFile())
                    file.setReadable(true, true);
            }
            file.setReadable(true, true);
            FileOutputStream out = new FileOutputStream(file);
            out.write(b);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 优先使用IMEI/MEID码进行md5加密生成clientid
     *
     * @return
     */
    public static byte[] makeClientID(Context context) {
        return makeClientIdByUUID(context);
    }


    @SuppressLint("MissingPermission")
    public static String getIMEI(Context context) {
        return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    }

    /**
     * 概述：获得手机Macaddress
     *
     * @param context
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    /**
     * 用随机码生成客户端id
     *
     * @param context
     * @return
     */
    public static byte[] makeClientIdByUUID(Context context) {
        byte[] b = getUUIDClientId(context);
        if (b == null || b.length < 16) {
            UUID fromString = UUID.randomUUID();
            ByteBuffer bf = ByteBuffer.allocate(16);
            bf.putLong(fromString.getLeastSignificantBits());
            bf.putLong(fromString.getMostSignificantBits());
            setUUIDClientId(bf.array(), context);
            return bf.array();
        } else {
            return b;
        }
    }

    /**
     * 保存客户端id
     *
     * @param b
     * @param context
     */
    public static void setUUIDClientId(byte[] b, Context context) {
        File file = new File(MobileSystemUtil.getProfileDir(context) + "/UUIDClientid.txt");
        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(b);
            out.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

    /**
     * 获取文件里面储存的随机码
     *
     * @param context
     * @return
     */
    public static byte[] getUUIDClientId(Context context) {
        File file = new File(MobileSystemUtil.getProfileDir(context) + "/UUIDClientid.txt");
        try {
            FileInputStream in = new FileInputStream(file);
            byte[] b = new byte[16];
            in.read(b);
            return b;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return null;
    }

    /**
     * 获取文件里面储存的客户端id
     *
     * @param context
     * @return
     */
    public static String getClientId(Context context) {
        File file = new File(MobileSystemUtil.getProfileDir(context) + File.separator + "clientid");
        try {
            FileInputStream in = new FileInputStream(file);
            byte[] b = new byte[16];
            in.read(b);
            in.close();
            if (b != null && b.length == 16) {
                String client = NumberUtil.byteArray2HEXString(b);
                return client;
            }
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
