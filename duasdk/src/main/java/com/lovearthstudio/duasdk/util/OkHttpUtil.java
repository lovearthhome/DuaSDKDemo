package com.lovearthstudio.duasdk.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.protobuf.DescriptorProtos;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class OkHttpUtil {
    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
    public static final MediaType MEDIA_TYPE_JSON=MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

    private static final OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
    public static final  Callback defaultCallback=new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response!=null) {
                if(response.isSuccessful()){//502网关错误时body不能string
                    try {
                        Log.i("OkHttpUtilLog",response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    LogUtil.e("OkHttpUtilErr",response.message());
                }
            }
        }
    };

    private static Request buildGet(String url){
        return new Request.Builder().url(url).build();
    }
    private static Request buildPost(String url,RequestBody body){
        return new Request.Builder().url(url).post(body).build();
    }
//    private static Request buildHeader(String headerKey,String headerValue,String url,RequestBody body){
//        return new Request.Builder().header(headerKey,headerValue).url(url).post(body).build();
//    }
//    private static Request buildHeaders(Headers headers,String url,RequestBody body){
//        return new Request.Builder().headers(headers).url(url).post(body).build();
//    }
    public static Response execute(Request request){
        try {
            return mOkHttpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static InputStream handlerStream(Request request){
        Response response = execute(request);
        if (response!=null&&response.isSuccessful()) {
                return response.body().byteStream();
        }
        return null;
    }
    public static InputStream getStream(String url){
        return handlerStream(buildGet(url));
    }
    private static void enqueue(Request request, Callback responseCallback){
        if(responseCallback==null){
            responseCallback=defaultCallback;
        }
        mOkHttpClient.newCall(request).enqueue(responseCallback);
    }

    /**
     *
     * @param url
     * @param json
     * @param callback
     * Media-type json utf-8
     */
    public static void asyncPost(String url,String json,Callback callback){
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, json);
        asyncPost(url,body,callback);
    }
    public static void asyncPost(String url,RequestBody body,Callback callback){
        enqueue(buildPost(url, body),callback);
    }

    public static void updateAvatar(String url, String imgKey, String imgName, String imgFullPath, Callback callback, Map<String,?> map){
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(imgKey, imgName, RequestBody.create(MEDIA_TYPE_PNG, new File(imgFullPath)));
        if(map!=null&&map.size()>0){
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                builder.addFormDataPart(entry.getKey(),(String) entry.getValue());
            }
        }
        asyncPost(url,builder.build(),callback);
    }
}