package com.lovearthstudio.duasdk.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceUtil {

    public static String prefGetKey(Context context,String prefName,String keyName,String defaultValue){
        SharedPreferences settings = context.getSharedPreferences(prefName, 0);
        return settings.getString(keyName,defaultValue);
    }
    public static void prefSetKey(Context context,String prefName,String keyName,String keyValue){
        //1、打开Preferences，名称为prefName，如果存在则打开它，否则创建新的Preferences
        SharedPreferences settings = context.getSharedPreferences(prefName, 0);
        //2、让pref处于编辑状态
        SharedPreferences.Editor editor = settings.edit();
        //3、存放数据
        editor.putString(keyName,keyValue);
        //4、完成提交
        editor.commit();
    }
}
