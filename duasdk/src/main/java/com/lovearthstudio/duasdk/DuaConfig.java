package com.lovearthstudio.duasdk;

import android.content.Intent;
import android.os.Bundle;

import com.lovearthstudio.duasdk.util.IntentUtil;

/**
 * Author：Mingyu Yi on 2016/5/12 17:05
 * Email：461072496@qq.com
 */
public class DuaConfig {
    public static final String DUA_LAUNCH_MODE_REGISTER="Register";
    public static final String DUA_LAUNCH_MODE_RESET_PWD="ResetPassword";
    public static final String DUA_LAUNCH_MODE_MODIFY_PROFILE="ModifyProfile";
    public static boolean keepLogon=true;
    public static boolean keepLoginHistory=true;

    public static String callbackPackage;
    public static String loginCallbackActivity;
    public static String registerCallbackActivity;

    public static void loadConfig(Intent intent){
        Bundle extra=intent.getExtras();
        if(extra!=null){
            callbackPackage=extra.getString("callbackPackage");
            loginCallbackActivity=extra.getString("loginCallbackActivity");
            registerCallbackActivity=extra.getString("registerCallbackActivity");
        }
    }

    public static Intent getLoginCallbackIntent(){
        return IntentUtil.makeCrossAppIntent(callbackPackage,loginCallbackActivity);
    }
    public static Intent getRegisterCallbackIntent(){
        Intent intent=IntentUtil.makeCrossAppIntent(callbackPackage,registerCallbackActivity);
        if(intent==null){
            intent=IntentUtil.makeCrossAppIntent(callbackPackage,loginCallbackActivity);
        }
        return intent;
    }
}
