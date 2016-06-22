/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package com.lovearthstudio.duasdk.util;

import android.util.Log;

public class LogUtil {

    private static final String TAG = "DuaLog";

    public static void d(String message) {
        d(TAG,message);
    }
    public static void d(String tag,String message) {
        Log.d(tag, buildMessage(message));
    }
    public static void e(String message) {
        e(TAG,message);
    }
    public static void e(String tag,String message) {
        Log.e(tag, buildMessage(message));
    }
    public static void i(String message) {
        i(TAG,message);
    }
    public static void i(String tag,String message) {
        Log.i(tag, buildMessage(message));
    }
    public static void v(String message) {
        v(TAG,message);
    }
    public static void v(String tag,String message) {
        Log.v(tag, buildMessage(message));
    }
    public static void w(String message) {
        w(TAG,message);
    }
    public static void w(String tag,String message) {
        Log.w(tag, buildMessage(message));
    }
    public static void wtf(String message) {
        wtf(TAG,message);
    }
    public static void wtf(String tag,String message) {
        Log.wtf(tag, buildMessage(message));
    }
    private static String buildMessage(String rawMessage) {
        StackTraceElement caller = new Throwable().getStackTrace()[2];
        String fullClassName = caller.getClassName();
//        if(fullClassName.equals(LogUtil.class.getName())){
//            return rawMessage;
//        }else {
//            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
//            return className + "." + caller.getMethodName() + "(): " + rawMessage;
//        }
        return rawMessage;
    }
}
