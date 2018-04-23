package com.tttrtcgame.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by liujing on 2016/11/09.
 */

public class SharedPreferencesUtil {
    private static SharedPreferences sp;

    private static SharedPreferences getSp(Context context) {
        if (sp == null)
            sp = context.getSharedPreferences("login", Context.MODE_PRIVATE);
        return sp;
    }

    /**
     * 保存信息的操作
     * 2016-11-09
     */
    public static void saveString(Context context, String key, String value){
        getSp(context).edit().putString(key, value).commit();
    }

    /**
     * 获取信息的操作
     * 2016-11-09
     */
    public static String getString(Context context, String key, String defValue){
        return getSp(context).getString(key, defValue);
    }


    /**
     * 保存信息的操作
     * 2016-11-09
     */
    public static void saveInt(Context context, String key, int value){
        getSp(context).edit().putInt(key, value).commit();
    }

    /**
     * 获取信息的操作
     * 2016-11-09
     */
    public static int getInt(Context context, String key, int defValue){
        return getSp(context).getInt(key, defValue);
    }

    /**
     * 保存信息的操作
     * 2016-11-09
     */
    public static void saveLong(Context context, String key, long value){
        getSp(context).edit().putLong(key, value).commit();
    }

    /**
     * 获取信息的操作
     * 2016-11-09
     */
    public static long getLong(Context context, String key, long defValue){
        return getSp(context).getLong(key, defValue);
    }

   /**
    * 保存boolean信息的操作
    * 2016-11-10
    */
    public static void saveBoolean(Context context, String key , boolean value){
        getSp(context).edit().putBoolean(key,value).commit();
    }
    /**
     * 获取Boolean信息的操作
     * 2016-11-10
     */
    public static boolean getBoolean(Context context, String key, boolean defValue){
        return getSp(context).getBoolean(key,defValue);
    }
    /**
     *  创建删除方法
     */


    public static void clearData() {
        sp.edit().clear().commit();
    }

}

