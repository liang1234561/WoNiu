package com.data.operation;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.alibaba.fastjson.JSON;
import com.data.util.ClientId;
import com.data.util.NumberUtil;


import java.util.ArrayList;
import java.util.List;


/**
 * 保存客户端信息
 *
 * @author Administrator
 */
public class PreferenceHelper extends BasePreferenceHelper {
    public static final String PHOTO = "photo";
    public static final String SOUND = "sound";
    public static final String USERID = "user_id";
    private static final String CLIENTID = "clientID";
    private static final String FIRSTONLINE = "firstonline";
    private static final String VERSIONEND = "versionend";
    private static final String VERSIONCOUNT = "versioncount";
    static String clientId = null;


    public static String getClientID(Context context) {
        if (clientId == null) {
            clientId = ClientId.getClientId(context);
            if (clientId == null) {
                clientId = getPreference(context).getString(CLIENTID, null);
                if (clientId == null) {
                    return makeNewClientID(context);
                } else {
                    ClientId.setClientId(NumberUtil.hexString2ByteArray(clientId), context);
                }
            } else {
                setClientShared(clientId, context);
            }
        }
        return clientId;
    }

    public static String makeNewClientID(Context context) {
        byte[] newid = ClientId.makeClientID(context);
        ClientId.setClientId(newid, context);
        String clientId = NumberUtil.byteArray2HEXString(newid);
        setClientShared(clientId, context);
        return clientId;
    }

    private static void setClientShared(String clientId, Context context) {
        SharedPreferences sharedPreferences = getPreference(context);
        Editor edit = sharedPreferences.edit();
        edit.putString(CLIENTID, clientId);
        edit.commit();
    }

    public static void setMark(Context context) {
        SharedPreferences sharedPreferences = getPreference(context);
        Editor edit = sharedPreferences.edit();
        edit.putInt("mark", 3);
        edit.commit();
    }

    public static int getMark(Context context) {
        return getPreference(context).getInt("mark", 0);
    }

    public static void setFirstOnline(Context context) {
        SharedPreferences sharedPreferences = getPreference(context);
        Editor edit = sharedPreferences.edit();
        edit.putBoolean(FIRSTONLINE, false);
        edit.apply();
    }

    public static boolean getFirstOnline(Context context) {
        return getPreference(context).getBoolean(FIRSTONLINE, true);
    }

    /**
     * 设置升级判断
     *
     * @param context
     */
    public static void setVersionUrl(String versionurl, Context context) {
        SharedPreferences sharedPreferences = getPreference(context);
        Editor edit = sharedPreferences.edit();
        edit.putString(VERSIONEND, versionurl);
        edit.commit();
    }

    /**
     * 判断已经得到升级
     *
     * @param context
     * @return
     */
    public static String getVersionUrl(Context context) {
        return getPreference(context).getString(VERSIONEND,"");
    }

    /**
     * 设置不强制升级判断
     *
     * @param context
     */
    public static void setVersionCount(int versionCount, Context context) {
        SharedPreferences sharedPreferences = getPreference(context);
        Editor edit = sharedPreferences.edit();
        edit.putInt(VERSIONCOUNT, versionCount);
        edit.apply();
    }

    /**
     * 判断不强制已经得到升级
     *
     * @param context
     * @return
     */
    public static int getVersionCount(Context context) {
        return getPreference(context).getInt(VERSIONCOUNT, -1);
    }

    /**
     * 保存List
     *
     * @param tag
     * @param datalist
     */
    public static void setDataList(String tag, List<String> datalist, Context context) {
        if (null == datalist || datalist.size() < 0)
            return;
        //转换成json数据，再保存
        String strJson = JSON.toJSONString(datalist);
        SharedPreferences sharedPreferences = getPreference(context);
        Editor editor = sharedPreferences.edit();
        editor.putString(tag, strJson);
        editor.commit();

    }

    /**
     * 获取List
     *
     * @param tag
     * @return
     */
    public static List<String> getDataList(Context context, String tag) {
        List<String> datalist = new ArrayList<String>();
        String strJson = getPreference(context).getString(tag, null);
        if (null == strJson) {
            return datalist;
        }
        datalist = JSON.parseArray(strJson, String.class);
        return datalist;
    }
}
