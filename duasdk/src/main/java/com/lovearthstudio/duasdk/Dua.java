package com.lovearthstudio.duasdk;


import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;
import com.lovearthstudio.duasdk.util.Collector;
import com.lovearthstudio.duasdk.util.HttpUtil;
import com.lovearthstudio.duasdk.util.StorageUtil;
import com.lovearthstudio.duasdk.util.TimeUtil;
import com.lovearthstudio.duasdk.util.security.Des3;
import com.lovearthstudio.duasdk.util.security.MD5;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Dua {
    public Dua(Context context){
        this.context=context;
        this.collector=new Collector(context);
    }
    private  String serverUrl="http://api.xdua.org";
    private  Context context;
    private  String DUA_LOCAL_STORAGE="duaLocalStorage_0d82839cf42a298708e70c1f5a9f5872";
    private  long APP_EVENT_TIME_GAP=500;
    private  int MAX_APP_EVENT_LENGTH=2;
    private  String NETWORK_OFFLINE="network is offline";
    private  DuaLocalStorage duaLocalStorage;
    private  Collector collector;
    private  Gson gson = new Gson();

    public  void duaSleep(){
        long now= TimeUtil.getCurrentTimeStamp();
        duaLocalStorage_load();
        duaLocalStorage.curEventDuration +=now-duaLocalStorage.curEventLastStart;
        duaLocalStorage.curEventCount +=1;
        duaLocalStorage.curEventLastPause=now;
        duaLocalStorage_save();
    }
    public  void duaAwake(){
        final long t,d,c;
        try {
            duaLocalStorage_load(); //初始化duaLocalStorage对象
            long now=TimeUtil.getCurrentTimeStamp();
//            Log.d("DuaLocalStorage",gson.toJson(duaLocalStorage));
            if(now-duaLocalStorage.curEventLastPause>APP_EVENT_TIME_GAP){
                t=duaLocalStorage.curEventStart;
                d=duaLocalStorage.curEventDuration;
                c=duaLocalStorage.curEventCount;

                duaLocalStorage.curEventStart=now;
                duaLocalStorage.curEventDuration=0;
                duaLocalStorage.curEventCount=0;
                duaLocalStorage.curEventLastPause=0;
                duaLocalStorage.curEventLastStart=now;
            }else{
                t=0;
                d=0;
                c=0;
                duaLocalStorage.curEventLastStart=now;
            }
            duaLocalStorage_save();
            MyCallBack mcb=new MyCallBack() {
                @Override
                public void onSuccess(String str){
                    long dua_id=Long.parseLong(str);
                    saveAppEvent(dua_id, t, d, c);
                    if (!collector.getNetWorkStatus().equals("offline")){
                        uploadAppEvents(dua_id);
                        uploadAppLists(dua_id);
                        uploadAppStats(dua_id);
                        uploadWirelessDevices(dua_id);
                    }
                }
                @Override
                public void onError(String str) {
                    Log.e("getCurrentDuaId",str);
                }
            };
            getCurrentDuaId(mcb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public long getCurrentDuaId(){
        duaLocalStorage_load();
        if(duaLocalStorage.currentDuaId!=-1){
            return duaLocalStorage.currentDuaId;
        }else if(duaLocalStorage.anonymousDuaId!=-1){
            return duaLocalStorage.anonymousDuaId;
        }else{
            applyDuaId(null);
            return 0;
        }
    }
    public  void getCurrentDuaId(MyCallBack mcb){
        duaLocalStorage_load();
        if(duaLocalStorage.currentDuaId!=-1){
            callbackOnSuccess(mcb,duaLocalStorage.currentDuaId+"");
        }else if(duaLocalStorage.anonymousDuaId!=-1){
            callbackOnSuccess(mcb,duaLocalStorage.anonymousDuaId+"");
        }else{
            applyDuaId(mcb);
        }
    }
    public String getNetworkStatus(){
        return collector.getNetWorkStatus();
    }
    public void login(final String ustr, final String pwd, final String role,final MyCallBack mcb){
        if(!getNetworkStatus().equals("offline")){
            MyCallBack myCallBack=new MyCallBack() {
                @Override
                public void onSuccess(String str) {
                    try {
                        JSONObject jo=new JSONObject();
                        jo.put("ustr",ustr);
                        jo.put("pwd", MD5.md5(pwd));
                        jo.put("dua_id",Long.parseLong(str));
                        jo.put("action","login");
                        jo.put("role", role);

                        String jstr=jo.toString();
                        Log.d("Login",jstr);
                        Callback cb=new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                callbackOnError(mcb,e.toString());
                            }
                            @Override
                            public void onResponse(Call call, Response response){
                                try {
                                    String str=response.body().string();
                                    JSONObject result=new JSONObject(str);
                                    if(result.getString("status").equals("0")){
                                        duaLocalStorage.currentDuaId=result.getLong("result");
                                        callbackOnSuccess(mcb, result.getString("result"));
                                    }else{
                                        callbackOnError(mcb, result.getString("reason"));
                                    }
                                } catch (Exception e) {
                                    callbackOnError(mcb,e.toString());
                                }
                            }
                        };
                        HttpUtil.asyncPost(serverUrl + "/users", jstr, cb);
                    }catch (Exception e){
                        callbackOnError(mcb,e.toString());
                    }
                }
                @Override
                public void onError(String str) {
                    callbackOnError(mcb,str);
                }
            };
            getCurrentDuaId(myCallBack);
        }else{
            callbackOnError(mcb, NETWORK_OFFLINE);
        }
    }
    public void register(final String ustr, final String pwd, final String role, final String vfcode, final String incode, final String type, final String sex, final String bday,final MyCallBack mcb){
        if(!getNetworkStatus().equals("offline")){
            MyCallBack myCallBack=new MyCallBack() {
                @Override
                public void onSuccess(String str) {
                    try {
                        JSONObject jo=new JSONObject();
                        jo.put("ustr",ustr);
                        jo.put("pwd", MD5.md5(pwd));
                        jo.put("dua_id",Long.parseLong(str));
                        jo.put("action","register");
                        jo.put("type",type);
                        jo.put("role", role);
                        jo.put("vfcode",vfcode);
                        jo.put("incode",incode);
                        jo.put("sex",sex);
                        jo.put("bday", bday);
                        String jstr=jo.toString();
                        Log.d("Register", jstr);
                        Callback cb=new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                callbackOnError(mcb, e.toString());
                            }
                            @Override
                            public void onResponse(Call call, Response response){
                                try {
                                    String str=response.body().string();
                                    JSONObject result=new JSONObject(str);
                                    if(result.getString("status").equals("0")){
                                        duaLocalStorage.currentDuaId=result.getLong("result");
                                        callbackOnSuccess(mcb, result.getString("result"));
                                    }else{
                                        callbackOnError(mcb, result.getString("reason"));
                                    }
                                } catch (Exception e) {
                                    callbackOnError(mcb, e.toString());
                                }
                            }
                        };
                        HttpUtil.asyncPost(serverUrl + "/users", jstr, cb);
                    }catch (Exception e){
                        callbackOnError(mcb, e.toString());
                    }
                }
                @Override
                public void onError(String str) {
                    callbackOnError(mcb, str);
                }
            };
            getCurrentDuaId(myCallBack);
        }else{
            callbackOnError(mcb, NETWORK_OFFLINE);
        }
    }
    public void getVfCode(final String ustr,final MyCallBack mcb){
        if(!getNetworkStatus().equals("offline")){
            MyCallBack myCallBack=new MyCallBack() {
                @Override
                public void onSuccess(String str) {
                    Callback cb=new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            callbackOnError(mcb, e.toString());
                        }
                        @Override
                        public void onResponse(Call call, Response response){
                            try {
                                String str=response.body().string();
                                JSONObject result=new JSONObject(str);
                                if(result.getString("status").equals("0")){
                                    callbackOnSuccess(mcb, result.getString("result"));
                                }else{
                                    callbackOnError(mcb, result.getString("reason"));
                                }
                            } catch (Exception e) {
                                callbackOnError(mcb, e.toString());
                            }

                        }
                    };
                    try {
                        JSONObject jo=new JSONObject();
                        jo.put("ustr",ustr);
                        jo.put("dua_id",Long.parseLong(str));
                        jo.put("action", "get_vfcode");

                        String jstr=jo.toString();
                        Log.d("GetVerifyCode",jstr);
                        HttpUtil.asyncPost(serverUrl + "/auth",jstr , cb);
                    }catch (Exception e){
                        callbackOnError(mcb, e.toString());
                    }
                }

                @Override
                public void onError(String str) {
                    callbackOnError(mcb,str);
                }
            };
            getCurrentDuaId(myCallBack);
        }else{
            callbackOnError(mcb,NETWORK_OFFLINE);
        }
    }
    public void auth(final MyCallBack mcb){
        if(!getNetworkStatus().equals("offline")){
            MyCallBack myCallBack=new MyCallBack() {
                @Override
                public void onSuccess(String str) {
                    Callback cb=new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            callbackOnError(mcb,e.toString());
                        }
                        @Override
                        public void onResponse(Call call, Response response){
                            try {
                                String str=response.body().string();
                                JSONObject result=new JSONObject(str);
                                if(result.getString("status").equals("0")){
                                    callbackOnSuccess(mcb, result.getString("result"));
                                }else{
                                    callbackOnError(mcb, result.getString("reason"));
                                }
                            } catch (Exception e) {
                                callbackOnError(mcb, e.toString());
                            }
                        }
                    };
                    try{
                        JSONObject jo=new JSONObject();
                        jo.put("rule", "");
                        jo.put("dua_id",Long.parseLong(str));
                        jo.put("action","auth");

                        String jstr=jo.toString();
                        Log.d("Auth",jstr);
                        HttpUtil.asyncPost(serverUrl + "/auth",jstr , cb);
                    }catch (Exception e){
                        callbackOnError(mcb, e.toString());
                    }
                }

                @Override
                public void onError(String str) {
                    callbackOnError(mcb,str);
                }
            };
            getCurrentDuaId(myCallBack);
        }else{
            callbackOnError(mcb,NETWORK_OFFLINE);
        }
    }

    private  void uploadWirelessDevices(long dua_id){
        try{
            List<JSONObject> jl=new ArrayList<JSONObject>();
            jl.addAll(collector.getNCI());
            jl.addAll(collector.getWifiList());
            jl.addAll(collector.getBlueTeeth());

            JSONObject jo=new JSONObject();
            jo.put("dua_id",dua_id);
            jo.put("action","add_wlds");
            jo.put("wlds",jl);
            jo.put("time", 0);

            Location location=collector.getCurrentLocation();
            if(location==null){
                Log.e("WirelessInfo", "cant get current location");
                return;
            }else{
                jo.put("gps",1);
                jo.put("lat",location.getLatitude());
                jo.put("lon",location.getLongitude());
                jo.put("acc",location.getAccuracy());
            }

            Callback cb=new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("uploadWirelessDevices", e.toString());
                }
                @Override
                public void onResponse(Call call, Response response){

                    try {
                        String str=response.body().string();
                        Log.i("uploadWirelessDevices", str);

//                    JSONObject result=new JSONObject(str);
//                    if(result.getString("status").equals("0")){
//                    }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            };
            String str=jo.toString();
            Log.d("WirelessInfo", str);
            HttpUtil.asyncPost(serverUrl + "/wlds", str, cb);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private  void uploadAppStats(long dua_id){
        try {
            final long curTime=TimeUtil.getCurrentTimeStamp();
            long difTime=(curTime-duaLocalStorage.lastAppStatUploadTime)/1000;
            List<JSONObject> jl=new ArrayList<JSONObject>();
            if(difTime>3600*24*7){
                long startTime=TimeUtil.getYearsAgo(-3);
                jl.addAll(collector.getAppStats(startTime, curTime, 1));
                jl.addAll(collector.getAppStats(startTime, curTime, 2));
                jl.addAll(collector.getAppStats(startTime, curTime, 3));
                jl.addAll(collector.getAppStats(startTime, curTime, 4));
            }else if(difTime >= 3600 * 24 * 1 && difTime <= 3600 * 24 * 7){
                long startTime=TimeUtil.getDaysAgo(-7);
                jl.addAll(collector.getAppStats(startTime, curTime, 1));
                jl.addAll(collector.getAppStats(startTime, curTime, 2));
            }else{
                Log.d("uploadAppStats", "last upload time " + TimeUtil.toTimeString(duaLocalStorage.lastAppStatUploadTime));
                return;
            }
            JSONArray ja=new JSONArray();
            for(JSONObject stat:jl){
                long ttf=stat.getLong("ttf");
                if(ttf>0){
                    stat.put("ttf",ttf/1000);
                    long fts=stat.getLong("fts");
                    long lts=stat.getLong("lts");
                    switch (stat.getInt("type")){
                        case 1: {
                            stat.put("date",TimeUtil.toTimeString(fts,"yyyyMMdd"));
                            break;
                        }
                        case 2: {
                            int[] index=TimeUtil.rangeGetIndex(fts,lts,2);
                            stat.put("date",""+(index[2] * 10000 + index[1] * 100 + index[0]));
                            break;
                        }
                        case 3: {
                            int[] index=TimeUtil.rangeGetIndex(fts,lts, 3);
                            stat.put("date",""+(index[1] * 100 + index[0]));
                            break;
                        }
                        case 4: {
                            stat.put("date",""+TimeUtil.rangeGetIndex(fts,lts,4)[0]);
                            break;
                        }
                    }
                    ja.put(stat);
                }
            }
            if(ja.length()!=0){
                JSONObject jo=new JSONObject();
                jo.put("dua_id",dua_id);
                jo.put("action","dev_stat");
                jo.put("data", ja);

                Callback cb=new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("uploadAppStats",e.toString());
                    }
                    @Override
                    public void onResponse(Call call, Response response){

                        try {
                            String str=response.body().string();
                            Log.i("uploadAppStats", str);

                            JSONObject result=new JSONObject(str);
                            if(result.getString("status").equals("0")){
                                duaLocalStorage.lastAppStatUploadTime = curTime;
                                duaLocalStorage_save();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                };
                String str=jo.toString();
                Log.d("AppStats", str);
                HttpUtil.asyncPost(serverUrl + "/duas", str, cb);
            }else{
                Log.e("uploadAppStats", "got no app usage stats");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private  void uploadAppLists(long dua_id){
        final long curTime=TimeUtil.getCurrentTimeStamp();
        if(curTime-duaLocalStorage.lastAppListUploadTime>=3600*24*7){
            try {
                JSONObject jo=new JSONObject();
                jo.put("dua_id",dua_id);
                jo.put("action", "add_rats");
                jo.put("data", collector.getAppLists());

                Callback cb=new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("uploadAppLists",e.toString());
                    }
                    @Override
                    public void onResponse(Call call, Response response){

                        try {
                            String str=response.body().string();
                            Log.i("uploadAppLists", str);

                            JSONObject result=new JSONObject(str);
                            if(result.getString("status").equals("0")){
                                duaLocalStorage.lastAppListUploadTime = curTime;
                                duaLocalStorage_save();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                };
                String str=jo.toString();
                Log.d("AppLists", str);
                HttpUtil.asyncPost(serverUrl + "/apps", str, cb);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            Log.d("uploadAppLists", "last upload time " + TimeUtil.toTimeString(duaLocalStorage.lastAppListUploadTime));
        }
    }
    private  void uploadAppEvents(long dua_id){
        if(duaLocalStorage.needUploadAppEvent==1){
            JSONArray events=getAppEvents();
            if(events!=null&&events.length()>=MAX_APP_EVENT_LENGTH){//此处判断可无
                try {
                    JSONObject jo=new JSONObject();
                    jo.put("dua_id",dua_id);
                    jo.put("action","dua_active");
                    jo.put("channel","Debug");
                    jo.put("model",collector.getModel());
                    jo.put("os", collector.getPlatform() + " " + collector.getOSVersion());
                    jo.put("version", collector.getVersionNumber() + " " + collector.getVersionCode());
                    jo.put("event", events);

                    Callback cb=new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("uploadAppEvents", e.toString());
                        }
                        @Override
                        public void onResponse(Call call, Response response){

                            try {
                                String str=response.body().string();
                                Log.i("uploadAppEvents", str);

                                JSONObject result=new JSONObject(str);
                                if(result.getString("status").equals("0")){
                                    duaLocalStorage.needUploadAppEvent=0;
                                    duaLocalStorage.lastAppEventUploadTime=TimeUtil.getCurrentTimeStamp();
                                    duaLocalStorage_save();
                                    StorageUtil.prefSetKey(context, DUA_LOCAL_STORAGE, "AppEvent", new JSONArray().toString());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    };
                    String str=jo.toString();
                    Log.d("AppEventInfo",str);
                    HttpUtil.asyncPost(serverUrl + "/duas", str, cb);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }else{
            if(duaLocalStorage.lastAppEventUploadTime!=0){
                Log.d("AppEventInfo","last upload time " + TimeUtil.toTimeString(duaLocalStorage.lastAppEventUploadTime));
            }
        }
    }
    private  void saveAppEvent(long dua_id,long timestamp,long duration,long count){
        if(dua_id==0||(timestamp==0&&duration==0&&count==0)) return; //如果dua_id为0或第一次打开不保存
        JSONArray events=getAppEvents();
        JSONObject event=new JSONObject();
        try {
            event.put("dua_id",dua_id);
            event.put("t",timestamp);
            event.put("d",duration);
            event.put("c", count);
        }catch (Exception e){
            e.printStackTrace();
        }
        events.put(event);

        if(events.length()>=MAX_APP_EVENT_LENGTH){
            duaLocalStorage.needUploadAppEvent=1;
        }
        StorageUtil.prefSetKey(context, DUA_LOCAL_STORAGE, "AppEvent", events.toString());
    }
    private  JSONArray getAppEvents(){
        JSONArray events;
        String objStr= StorageUtil.prefGetKey(context, DUA_LOCAL_STORAGE, "AppEvent", null);
        if(objStr==null){
            events=new JSONArray();
        }else{
            try {
                events=new JSONArray(objStr);
            }catch (Exception e){
                events=null;
                e.printStackTrace();
            }
        }
        return events;
    }

    private  class BornInfo{
        public long initime;
        public long lastime;
        public String avn;
        public int avc;
        public String aname;
        public String pname;
        public String dsn;
        public String model;
        public String os;
        public String man;
        public String channel="Debug";
        public String action="dua_born";
        public String key="797b75b683162604191d22dba892dfa2";
    }
    private  BornInfo getBornInfo(){
        BornInfo bornInfo=new BornInfo();
        bornInfo.avn=collector.getVersionNumber();
        bornInfo.avc=collector.getVersionCode();
        bornInfo.aname=collector.getAppName();
        bornInfo.pname=collector.getPackageName();
        bornInfo.dsn=collector.getUuid();
        bornInfo.model=collector.getModel();
        bornInfo.os=collector.getPlatform()+" "+collector.getOSVersion();
        bornInfo.man=collector.getManufacturer();
        bornInfo.initime=(TimeUtil.getCurrentTimeStamp()-duaLocalStorage.firtBornTime)/1000;
        bornInfo.lastime=(TimeUtil.getCurrentTimeStamp()-duaLocalStorage.lastBornTime)/1000;
        if (bornInfo.model == null)
            bornInfo.model = "Unknown";
        if (bornInfo.avn == null)
            bornInfo.avn = "1.1.1";
        if (bornInfo.avc == 0)
            bornInfo.avc = 110;
        if (bornInfo.aname == null)
            bornInfo.aname = "unknown";
        if (bornInfo.pname == null)
            bornInfo.pname = "com.lovearthstudio.unknown";
        if (bornInfo.model == null)
            bornInfo.model = "Unknown";
        return bornInfo;
    }
    private  void applyDuaId(final MyCallBack mcb){
        if(!collector.getNetWorkStatus().equals("offline")){
            final long now=TimeUtil.getCurrentTimeStamp();
            Callback cb=new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callbackOnError(mcb, e.toString());
                }
                @Override
                public void onResponse(Call call, Response response){
                    try {
                        String str=response.body().string();
                        JSONObject result=new JSONObject(str);
                        if(result.getString("status").equals("0")){
                            long dua_id=result.getLong("result");
                            duaLocalStorage.anonymousDuaId=dua_id;
                            duaLocalStorage.lastBornTime=now;
                            if(duaLocalStorage.firtBornTime==0){
                                duaLocalStorage.firtBornTime=now;
                            }
                            duaLocalStorage_save();

                            callbackOnSuccess(mcb,dua_id+"");
                        }else{
                            callbackOnError(mcb,result.getString("reason"));
                        }
                    } catch (Exception e) {
                        callbackOnError(mcb,e.toString());
                    }
                }
            };
            String str=gson.toJson(getBornInfo());
            Log.d("BornInfo", str);
            try {
                HttpUtil.asyncPost(serverUrl + "/duas",str , cb);
            }catch (Exception e){
                callbackOnError(mcb,e.toString());
            }
        } else {
            callbackOnError(mcb,NETWORK_OFFLINE);
        }
    }

    private void callbackOnSuccess(MyCallBack mcb,String str){
        if(mcb!=null){
            mcb.onSuccess(str);
        }
    }
    private void callbackOnError(MyCallBack mcb,String str){
        if(mcb!=null){
            mcb.onError(str);
        }
    }



    private  class DuaLocalStorage{
        public long curEventStart=0;
        public long curEventDuration=0;
        public long curEventCount=0;
        public long curEventLastStart=0;
        public long curEventLastPause=0;

        public int needUploadAppEvent=0;

        public long lastAppListUploadTime=0;
        public long lastAppStatUploadTime=0;
        public long lastAppEventUploadTime=0;
        public long anonymousDuaId=-1;
        public long currentDuaId=-1;

        public long lastBornTime=0;
        public long firtBornTime=0;
    }
    private  DuaLocalStorage duaLocalStorage_load(){
        if(duaLocalStorage==null){
            String objStr= StorageUtil.prefGetKey(context, DUA_LOCAL_STORAGE, "DuaLocalStorage", null);
            if (objStr != null) {
                try {
                    duaLocalStorage=gson.fromJson(Des3.decode(objStr), DuaLocalStorage.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                duaLocalStorage=new DuaLocalStorage();
                duaLocalStorage_save();
            }
        }
        return duaLocalStorage;
    }
    private  void duaLocalStorage_save(){
        try{
            StorageUtil.prefSetKey(context, DUA_LOCAL_STORAGE, "DuaLocalStorage", Des3.encode(gson.toJson(duaLocalStorage)));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
