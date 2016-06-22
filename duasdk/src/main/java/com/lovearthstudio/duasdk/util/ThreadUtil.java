package com.lovearthstudio.duasdk.util;

import android.os.Handler;
import android.os.Looper;

/**
 * Author：Mingyu Yi on 2016/5/13 20:37
 * Email：461072496@qq.com
 */
public class ThreadUtil {
    public static Handler mainHandler=new Handler(Looper.getMainLooper());
    public static void runOnUiThread(Runnable runnable){
        mainHandler.post(runnable);
    }
}
