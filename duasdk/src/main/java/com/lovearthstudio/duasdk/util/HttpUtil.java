package com.lovearthstudio.duasdk.util;

import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;

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

public class HttpUtil {
    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
    public static final MediaType MEDIA_TYPE_JSON=MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

    private static final Gson gson = new Gson();
    private static final OkHttpClient mOkHttpClient = new OkHttpClient();

    public static Request buildGet(String url)throws IOException{
        return new Request.Builder().url(url).build();
    }
    public static Request buildPost(String url,RequestBody body)throws IOException{
        return new Request.Builder().url(url).post(body).build();
    }
    public static Response execute(Request request) throws IOException {
        return mOkHttpClient.newCall(request).execute();
    }
    public static void enqueue(Request request, Callback responseCallback){
        mOkHttpClient.newCall(request).enqueue(responseCallback);
    }
    public static String handlerResponse(Request request)throws IOException{
        Response response = execute(request);
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }
    public static void handlerResponseJson(Request request)throws IOException{
        Response response = execute(request);
        if (response.isSuccessful()) {
//            Gist gist = gson.fromJson(response.body().charStream(), Gist.class);
//            for (Map.Entry<String, GistFile> entry : gist.files.entrySet()) {
//                System.out.println(entry.getKey());
//                System.out.println(entry.getValue().content);
//            }
        } else {
            throw new IOException("Unexpected code " + response);
        }
//        class Gist {
//            Map<String, GistFile> files;
//        }
//        class GistFile {
//            String content;
//        }
    }


    public static String syncGet(String url) throws Exception {
        return handlerResponse(buildGet(url));
    }
    public static void asyncGet(String url,Callback callback) throws Exception {
//        Callback test=new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//            }
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//                Headers responseHeaders = response.headers();
//                for (int i = 0; i < responseHeaders.size(); i++) {
//                    System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
//                }
//                System.out.println(response.body().string());
//            }
//        };
        enqueue(buildGet(url),callback);
    }
    public static String syncPost(String url, String json) throws Exception {
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, json);
        return handlerResponse(buildPost(url, body));
    }
    public static void asyncPost(String url,String json,Callback callback) throws Exception{
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, json);
        enqueue(buildPost(url, body),callback);
    }
    public static String postStream(String url) throws Exception {
        RequestBody body = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MEDIA_TYPE_MARKDOWN;
            }
            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                sink.writeUtf8("Numbers\n");
                sink.writeUtf8("-------\n");
                for (int i = 2; i <= 997; i++) {
                    sink.writeUtf8(String.format(" * %s = %s\n", i, factor(i)));
                }
            }
            private String factor(int n) {
                for (int i = 2; i < n; i++) {
                    int x = n / i;
                    if (x * i == n) return factor(x) + " × " + i;
                }
                return Integer.toString(n);
            }
        };
        return handlerResponse(buildPost(url, body));
    }
    public static String postFile(String url,String fileFullPath) throws Exception {
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, new File(fileFullPath));
        return handlerResponse(buildPost(url, body));
    }
    public static String postForm(String url) throws Exception {
        RequestBody body = new FormBody.Builder()
                .add("search", "Jurassic Park")
                .build();
        return handlerResponse(buildPost(url, body));
    }
    public static String postMultiPart(String url,String title,String img,String imgFullPath) throws Exception {
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title", title)
                .addFormDataPart("image", img, RequestBody.create(MEDIA_TYPE_PNG, new File(imgFullPath)))
                .build();
        Request request = new Request.Builder()
                .header("Authorization", "Client-ID " + "IMGUR_CLIENT_ID")
                .url(url)
                .post(body)
                .build();
        return handlerResponse(request);
    }
}