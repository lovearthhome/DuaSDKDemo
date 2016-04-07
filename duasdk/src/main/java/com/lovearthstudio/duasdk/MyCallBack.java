package com.lovearthstudio.duasdk;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public interface MyCallBack{
    void onSuccess(String str);
    void onError(String str);
}
