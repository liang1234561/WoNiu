package com.data.operation;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;



public class BasePreferenceHelper {
    private static final String PREFERENCE = "preference"; //使用SharedPreferences保存数据 文件名

    /**
     * get
     **/
    public static int getInt(String key, Context context) {
        return getPreference(context).getInt(key, 0);
    }

    public static SharedPreferences getPreference(Context context) {
        if (context != null)
            return context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        else {
            return null;
        }
    }

    public static int getInt(String key, int defValue, Context context) {
        return getPreference(context).getInt(key, defValue);
    }

    public static float getFloat(String key, Context context) {
        return getPreference(context).getFloat(key, 0);
    }

    public static float getFloat(String key, float defValue, Context context) {
        return getPreference(context).getFloat(key, defValue);
    }

    public static long getLong(String key, Context context) {
        return getPreference(context).getLong(key, 0);
    }

    public static long getLong(String key, long defValue, Context context) {
        return getPreference(context).getLong(key, defValue);
    }

    public static String getString(String key, Context context) {
        return getPreference(context).getString(key, null);
    }

    public static String getString(String key, String defValue, Context context) {
        return getPreference(context).getString(key, defValue);
    }

    public static boolean getBoolean(String key, Context context) {
        return getPreference(context).getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defValue, Context context) {
        return getPreference(context).getBoolean(key, defValue);
    }

    /**
     * set
     **/
    public static void setInt(String key, int value, Context context) {
        Editor editor = getPreference(context).edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void setFloat(String key, float value, Context context) {
        Editor editor = getPreference(context).edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public static void setLong(String key, long value, Context context) {
        Editor editor = getPreference(context).edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static void setString(String key, String value, Context context) {
        Editor editor = getPreference(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void setBoolean(String key, boolean value, Context context) {
        Editor editor = getPreference(context).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void setSetString(String key, Set<String> values, Context context) {
        Editor editor = getPreference(context).edit();
        editor.putStringSet(key, values);
        editor.apply();
    }

    public static void remove(String key, Context context) {
        Editor editor = getPreference(context).edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param context
     * @param key
     */
    public static void remove(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE,
                Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.remove(key);
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 清除所有数据
     *
     * @param context
     */
    public static void clear(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFERENCE,
                Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.clear();
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 创建一个解决SharedPreferencesCompat.apply方法的一个兼容类
     *
     * @author editor
     */
    private static class SharedPreferencesCompat {
        private static final Method sApplyMethod = findApplyMethod();

        /**
         * 反射查找apply的方法
         *
         * @return
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Method findApplyMethod() {
            try {
                Class clz = Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException e) {
            }

            return null;
        }

        /**
         * 如果找到则使用apply执行，否则使用commit
         *
         * @param editor
         */
        public static void apply(Editor editor) {
            try {
                if (sApplyMethod != null) {
                    sApplyMethod.invoke(editor);
                    return;
                }
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
            editor.commit();
        }
    }
}
