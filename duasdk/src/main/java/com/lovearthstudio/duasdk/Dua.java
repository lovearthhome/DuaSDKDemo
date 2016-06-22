package com.lovearthstudio.duasdk;


import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lovearthstudio.duasdk.util.JsonUtil;
import com.lovearthstudio.duasdk.util.LogUtil;
import com.lovearthstudio.duasdk.util.OkHttpUtil;
import com.lovearthstudio.duasdk.util.SharedPreferenceUtil;
import com.lovearthstudio.duasdk.util.TimeUtil;
import com.lovearthstudio.duasdk.util.encryption.Des3;
import com.lovearthstudio.duasdk.util.encryption.MD5;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Dua {
    private static Dua instance=null;
    public static Dua getInstance(){
        return instance;
    }
    public static Dua init(Context context){
        if(instance==null){
            instance=new Dua(context);
        }
        return instance;
    }
    private Dua(Context context){
        this.context=context;
        this.duaCollector =new DuaCollector(context);
    }
    private  String serverUrl="http://api.xdua.org";
    private  Context context;
    private  String DUA_LOCAL_STORAGE="duaLocalStorage_0d82839cf42a298708e70c1f5a9f5872";
    private  long APP_EVENT_TIME_GAP=5000;
    private  int MAX_APP_EVENT_LENGTH=2;
    private  String NETWORK_OFFLINE="network is offline";
    private  DuaLocalStorage duaLocalStorage;
    private DuaCollector duaCollector;
    private  Gson gson = new Gson();
    public DuaUser duaUser;


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
        duaLocalStorage_load();
        long now=TimeUtil.getCurrentTimeStamp();
        duaLocalStorage.lastOpenTime=now;
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
        new DuaIdRequest(null){
            @Override
            public void doWithDuaId(long dua_id) {
                LogUtil.e("当前DuaId",dua_id+"");
                saveAppEvent(dua_id, t, d, c);
                if (!duaCollector.getNetWorkStatus().equals("offline")){
                    uploadAppEvents(dua_id);
                    uploadAppLists(dua_id);
                    uploadAppStats(dua_id);
                    uploadWirelessDevices2(dua_id);
                }
            }
        }.doRequest();
    }
    public  void duaExit(){//扫描蓝牙中有一个广播接收器，可能出现内存泄露
        duaCollector.unregisterReciver();
    }
    public long getCurrentDuaId(){
        duaLocalStorage_load();
        if(getCurrentDuaUser().logon&&duaLocalStorage.currentDuaId!=-1){
            return duaLocalStorage.currentDuaId;
        }else if(duaLocalStorage.anonymousDuaId!=-1){
            return duaLocalStorage.anonymousDuaId;
        }else{
            applyDuaId(null);
            return 0;
        }
    }
    public  void getCurrentDuaId(DuaCallback mcb){
        duaLocalStorage_load();
        if(getCurrentDuaUser().logon&&duaLocalStorage.currentDuaId!=-1){
            callbackOnSuccess(mcb,"当前DuaId",duaLocalStorage.currentDuaId+"");
        }else if(duaLocalStorage.anonymousDuaId!=-1){
            callbackOnSuccess(mcb,"当DuaId",duaLocalStorage.anonymousDuaId+"");
        }else{
            applyDuaId(mcb);
        }
    }
    public String getNetworkStatus(){
        return duaCollector.getNetWorkStatus();
    }
    public void logout(){
        getCurrentDuaUser().logon=false;
        if(!DuaConfig.keepLoginHistory){
            duaUser.ustr="";
            duaUser.avatar="";
        }
        duaUser.pwd="";
        duaUser.tel="";
        duaUser.rules=new ArrayList<String>();
    }
    public void login(String ustr, String pwd, String role,DuaCallback mcb){
        cookieLogin(ustr,MD5.md5(pwd),role,mcb);
    }
    public void checkCookie(DuaCallback callback){
        duaLocalStorage_load();
        if(getCurrentDuaUser().logon){
            cookieLogin(duaUser.ustr,duaUser.pwd,duaUser.role,callback);
        }
    }
    private void cookieLogin(final String ustr, final String pwd, final String role,final DuaCallback mcb){
        new DuaIdRequest(mcb){
            @Override
            public void doWithDuaId(long dua_id) {
                try {
                    JSONObject jo = new JSONObject();
                    jo.put("ustr", ustr);
                    jo.put("pwd", pwd);
                    jo.put("dua_id", dua_id);
                    jo.put("action", "login");
                    jo.put("role", role);
                    String jstr = jo.toString();

                    new DuaNetRequest("登录",serverUrl + "/users", jstr, mcb){
                        @Override
                        public void doSuccessExtra(String result) {
                            try {
                                JSONObject resultContent=new JSONObject(result);
                                duaLocalStorage.currentDuaId=resultContent.getLong("dua_id");
                                String rules=resultContent.getString("rules");
                                duaUser.rules=gson.fromJson(rules,new TypeToken<ArrayList<String>>() {}.getType());
                                if(DuaConfig.keepLogon){
                                    duaUser.logon=true;
                                    duaUser.ustr=ustr;
                                    int index=ustr.indexOf("-");
                                    duaUser.zone=ustr.substring(0,index);
                                    duaUser.tel=ustr.substring(index+1);
                                    duaUser.pwd=pwd;
                                    duaUser.role=role;
                                }
                                getUserProfile(null);
                                duaLocalStorage_save();
                                duaUser_save();
                                super.doSuccessExtra(result);
                            }catch (Exception e){
                                doErrorExtra(e.toString());
                            }
                        }

                        @Override
                        public void doErrorExtra(String err) {
                            logout();
                            super.doErrorExtra(err);
                        }
                    }.doRequest();
                }catch (Exception e){
                    callbackOnError(mcb,"登录", e.toString());
                }
            }
        }.doRequest();
    }
    public void register(final String ustr, final String pwd, final String role, final String vfcode, final String incode, final String type, final String sex, final String bday,final DuaCallback mcb){
        new DuaIdRequest(mcb){
            @Override
            public void doWithDuaId(long dua_id) {
                try {
                    JSONObject jo=new JSONObject();
                    jo.put("ustr",ustr);
                    jo.put("pwd", MD5.md5(pwd));
                    jo.put("dua_id",dua_id);
                    jo.put("action","register");
                    jo.put("type",type);
                    jo.put("role", role);
                    jo.put("vfcode",vfcode);
                    jo.put("incode",incode);
                    jo.put("sex",sex);
                    jo.put("bday", bday);
                    String jstr=jo.toString();
                    new DuaNetRequest("注册",serverUrl + "/users", jstr, mcb){
                        @Override
                        protected void doSuccessExtra(String result) {
                            try {
                                duaLocalStorage.currentDuaId=Long.parseLong(result);
                                duaLocalStorage_save();
                                super.doSuccessExtra(result);
                            }catch (Exception e){
                                doErrorExtra(e.toString());
                            }
                        }
                    }.doRequest();
                }catch (Exception e){
                    callbackOnError(mcb,"注册", e.toString());
                }
            }
        }.doRequest();
    }
    public void getVfCode(final String ustr,final DuaCallback mcb){
        new DuaIdRequest(mcb){
            @Override
            public void doWithDuaId(long dua_id) {
                try {
                    JSONObject jo=new JSONObject();
                    jo.put("ustr",ustr);
                    jo.put("dua_id",dua_id);
                    jo.put("action", "get_vfcode");
                    String jstr=jo.toString();

                    new DuaNetRequest("验证码",serverUrl + "/auth", jstr, mcb){}.doRequest();
                }catch (Exception e){
                    callbackOnError(mcb,"验证码", e.toString());
                }
            }
        }.doRequest();
    }
    public void auth(final DuaCallback mcb){
        new DuaIdRequest(mcb){
            @Override
            public void doWithDuaId(long dua_id) {
                try{
                    JSONObject jo=new JSONObject();
                    jo.put("rule", "");
                    jo.put("dua_id",dua_id);
                    jo.put("action","auth");
                    String jstr=jo.toString();
                    new DuaNetRequest("Auth",serverUrl + "/auth",jstr,mcb).doRequest();
                }catch (Exception e){
                    callbackOnError(mcb,"Auth",e.toString());
                }
            }
        }.doRequest();
    }

    public void getUserProfile(final DuaCallback mcb){
        new DuaIdRequest(mcb){
            @Override
            public void doWithDuaId(long dua_id) {
                JSONObject jo=new JSONObject();
                try {
                    jo.put("dua_id",dua_id);
                    jo.put("action","get_profile");
                    new DuaNetRequest("用户资料",serverUrl+"/users",jo.toString(),mcb){
                        @Override
                        protected void doSuccessExtra(String result) {
                            JSONObject user=JsonUtil.toJsonObject(result);
                            try {
                                duaUser.avatar=user.getString("avatar");
                                duaUser.sex=user.getString("sex");
                                duaUser.bday=user.getString("bday");
                                duaUser.name=user.getString("name");
                                duaUser_save();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            super.doSuccessExtra(result);
                        }
                    }.doRequest();
                } catch (JSONException e) {
                    callbackOnError(mcb,"用户资料",e.toString());
                }
            }
        }.doRequest();
    }
    public void setAppPmc(final String event, final int param,final String punit, final long stats, final String sunit){
        final DuaCallback mcb=null;
        new DuaIdRequest(mcb){
            @Override
            public void doWithDuaId(long dua_id) {
                try {
                    JSONObject jo=new JSONObject();
                    jo.put("dua_id",dua_id);
                    jo.put("action", "set_pmc");
                    jo.put("event",event);
                    jo.put("param",param);
                    jo.put("punit",punit);
                    jo.put("stats",stats);
                    jo.put("sunit",sunit);
                    String jstr=jo.toString();

                    new DuaNetRequest("App性能", serverUrl + "/apps",jstr, mcb){}.doRequest();
                }catch (Exception e){
                    callbackOnError(mcb,"App性能", e.toString());
                }
            }
        }.doRequest();
    }

    public void updateAvatar(String imgName, String imgFullName, final DuaCallback callback){
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("dua_id",duaLocalStorage.currentDuaId+"")
                .addFormDataPart("subdir","avatar/")
                .addFormDataPart("files", imgName, RequestBody.create(OkHttpUtil.MEDIA_TYPE_PNG, new File(imgFullName)));
        new DuaNetRequest("图片上传","http://files.xdua.org/index.php",builder.build(),callback){
            @Override
            protected void dealResponse(String response) {
                JSONObject jo=null;
                String err=null;
                try {
                    jo= JsonUtil.toJsonObject(response).getJSONArray("files").getJSONObject(0);
                    err=jo.getString("error");
                } catch (Exception e) {
                }
                if (jo==null||err!=null){
                    callbackOnError(callback,"图片上传",response);
                }else {
                    try {
                        String url=jo.getString("url");
                        duaUser.avatar=url;
                        duaUser_save();

                        JSONObject body=new JSONObject();
                        body.put("dua_id",duaLocalStorage.currentDuaId);
                        body.put("action","set_avatar");
                        body.put("url",url);
                        new DuaNetRequest("头像更新",serverUrl+"/users",body.toString(),callback){
                        }.doRequest();
                    }catch (Exception e){
                        e.printStackTrace();
                        callbackOnError(callback,"头像更新","图片上传成功但是服务器更新头像失败");
                    }

                }
            }
        }.doRequest();
    }

    public void uploadSleepData(final String data, final DuaCallback mcb){
        new DuaIdRequest(mcb){
            @Override
            public void doWithDuaId(long dua_id) {
                try{
                    JSONObject jo=new JSONObject();
                    jo.put("dua_id",dua_id);
                    jo.put("data",data);
                    String jstr=jo.toString();
                    new DuaNetRequest("睡眠数据","http://api.ivita.org/sleep",jstr,mcb){}.doRequest();
                }catch (Exception e){
                    e.printStackTrace();
                    callbackOnError(mcb,"睡眠数据", e.toString());
                }
            }
        }.doRequest();
    }
    public void uploadSleepFeature(final JSONObject jo, final DuaCallback mcb){
        new DuaIdRequest(mcb){
            @Override
            public void doWithDuaId(long dua_id) {
                try{
                    jo.put("dua_id",dua_id);
                    jo.put("timestamp",TimeUtil.getCurrentTimeStamp());
                    String jstr=jo.toString();
                    new DuaNetRequest("睡眠特征","http://api.ivita.org/sleepFeature",jstr,mcb){}.doRequest();
                }catch (Exception e){
                    e.printStackTrace();
                    callbackOnError(mcb,"睡眠特征", e.toString());
                }
            }
        }.doRequest();
    }

    private void uploadWirelessDevices2(final long dua_id){
        final List<DuaCollector.WirelessDevice> jl=new ArrayList<DuaCollector.WirelessDevice>();
        jl.addAll(duaCollector.getNCI());
        jl.addAll(duaCollector.getWifiList());
        DuaCallback mcb=new DuaCallback() {
            @Override
            public void onSuccess(String str) {
                List<DuaCollector.WirelessDevice> bt=gson.fromJson(str,new TypeToken<ArrayList<DuaCollector.WirelessDevice>>() {}.getType());
                jl.addAll(bt);
                try{
                    JSONObject jo=new JSONObject();
                    jo.put("dua_id",dua_id);
                    jo.put("action","add_wlds");
                    jo.put("wlds",gson.toJson(jl));
                    jo.put("time", 0);
                    Location location= duaCollector.getCurrentLocation();
                    if(location!=null){
                        jo.put("gps",1);
                        jo.put("lat",location.getLatitude());
                        jo.put("lon",location.getLongitude());
                        jo.put("acc",location.getAccuracy());
                    }else{
                        jo.put("gps",0);
                        jo.put("lat",0);
                        jo.put("lon",0);
                        jo.put("acc",0);
                        LogUtil.e("WirelessDevices","无法获取gps信息");
                    }
                    String info=jo.toString();
                    LogUtil.e("Wld上传",info);
                    new DuaNetRequest("Wld结果",serverUrl + "/wlds", info, null){}.doRequest();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String str) {
                LogUtil.e("UploadWirelessDevices",str);
            }
        };
        duaCollector.scanBlueTeeth(mcb);
    }
    private  void uploadWirelessDevices(long dua_id){
        try{
            List<DuaCollector.WirelessDevice> jl=new ArrayList<DuaCollector.WirelessDevice>();
            jl.addAll(duaCollector.getNCI());
            jl.addAll(duaCollector.getWifiList());
            jl.addAll(duaCollector.getBoundBlueTeeth());

            JSONObject jo=new JSONObject();
            jo.put("dua_id",dua_id);
            jo.put("action","add_wlds");
            jo.put("wlds",gson.toJson(jl));
            jo.put("time", 0);
            Location location= duaCollector.getCurrentLocation();
            if(location==null){
                Log.e("WirelessInfo", "cant get current location");
                return;
            }else{
                jo.put("gps",1);
                jo.put("lat",location.getLatitude());
                jo.put("lon",location.getLongitude());
                jo.put("acc",location.getAccuracy());
            }
            String str=jo.toString();
            new DuaNetRequest("Wld结果",serverUrl + "/wlds",str, null){}.doRequest();
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
                jl.addAll(duaCollector.getAppStats(startTime, curTime, 1));
                jl.addAll(duaCollector.getAppStats(startTime, curTime, 2));
                jl.addAll(duaCollector.getAppStats(startTime, curTime, 3));
                jl.addAll(duaCollector.getAppStats(startTime, curTime, 4));
            }else if(difTime >= 3600 * 24 * 1 && difTime <= 3600 * 24 * 7){
                long startTime=TimeUtil.getDaysAgo(-7);
                jl.addAll(duaCollector.getAppStats(startTime, curTime, 1));
                jl.addAll(duaCollector.getAppStats(startTime, curTime, 2));
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
                String str=jo.toString();
                LogUtil.e("AppStat上传",str);
                new DuaNetRequest("AppStat结果",serverUrl + "/duas", str, null){
                    @Override
                    protected void doSuccessExtra(String result) {
                        duaLocalStorage.lastAppStatUploadTime = curTime;
                        duaLocalStorage_save();
                        super.doSuccessExtra(result);
                    }
                }.doRequest();
            }else{
                LogUtil.e("uploadAppStats", "got no app usage stats");
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
                jo.put("data", duaCollector.getAppLists());
                String str=jo.toString();
                LogUtil.e("AppList上传",str);
                new DuaNetRequest("AppList结果",serverUrl + "/apps", str, null){
                    @Override
                    protected void doSuccessExtra(String result) {
                        duaLocalStorage.lastAppListUploadTime = curTime;
                        duaLocalStorage_save();
                        super.doSuccessExtra(result);
                    }
                }.doRequest();
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            LogUtil.d("uploadAppLists", "last upload time " + TimeUtil.toTimeString(duaLocalStorage.lastAppListUploadTime));
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
                    jo.put("model", duaCollector.getModel());
                    jo.put("os", duaCollector.getPlatform() + " " + duaCollector.getOSVersion());
                    jo.put("version", duaCollector.getVersionNumber() + " " + duaCollector.getVersionCode());
                    jo.put("event", events);
                    String str=jo.toString();
                    LogUtil.e("AppEvent上传",str);
                    new DuaNetRequest("AppEvent结果",serverUrl + "/duas", str, null){
                        @Override
                        protected void doSuccessExtra(String result) {
                            duaLocalStorage.needUploadAppEvent=0;
                            duaLocalStorage.lastAppEventUploadTime=TimeUtil.getCurrentTimeStamp();
                            duaLocalStorage_save();
                            SharedPreferenceUtil.prefSetKey(context, DUA_LOCAL_STORAGE, "AppEvent", new JSONArray().toString());
                            super.doSuccessExtra(result);
                        }
                    }.doRequest();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }else{
            if(duaLocalStorage.lastAppEventUploadTime!=0){
                LogUtil.d("AppEventInfo","last upload time " + TimeUtil.toTimeString(duaLocalStorage.lastAppEventUploadTime));
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
        SharedPreferenceUtil.prefSetKey(context, DUA_LOCAL_STORAGE, "AppEvent", events.toString());
    }
    private  JSONArray getAppEvents(){
        JSONArray events;
        String objStr= SharedPreferenceUtil.prefGetKey(context, DUA_LOCAL_STORAGE, "AppEvent", null);
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
        bornInfo.avn= duaCollector.getVersionNumber();
        bornInfo.avc= duaCollector.getVersionCode();
        bornInfo.aname= duaCollector.getAppName();
        bornInfo.pname= duaCollector.getPackageName();
        bornInfo.dsn= duaCollector.getUuid();
        bornInfo.model= duaCollector.getModel();
        bornInfo.os= duaCollector.getPlatform()+" "+ duaCollector.getOSVersion();
        bornInfo.man= duaCollector.getManufacturer();
        bornInfo.initime=(TimeUtil.getCurrentTimeStamp()-duaLocalStorage.firtOpenTime)/1000;
        bornInfo.lastime=(TimeUtil.getCurrentTimeStamp()-duaLocalStorage.lastOpenTime)/1000;
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
    private  void applyDuaId(final DuaCallback mcb){
        final long now=TimeUtil.getCurrentTimeStamp();
        String str=gson.toJson(getBornInfo());
        LogUtil.e("Born消息内容",str);
        new DuaNetRequest("Born结果",serverUrl + "/duas",str , mcb){
            @Override
            protected void doSuccessExtra(String result) {
                long dua_id=Long.parseLong(result);
                duaLocalStorage.anonymousDuaId=dua_id;
                duaLocalStorage.lastOpenTime =now;
                if(duaLocalStorage.firtOpenTime ==0){
                    duaLocalStorage.firtOpenTime =now;
                }
                duaLocalStorage_save();
                super.doSuccessExtra(result);
            }
        }.doRequest();
    }

    private void callbackOnSuccess(DuaCallback mcb,String tag,String str){
        if(mcb!=null){
            mcb.onSuccess(str);
        }else{
            LogUtil.e(tag,str);
        }
    }
    private void callbackOnError(DuaCallback mcb,String tag,String str){
        if(mcb!=null){
            mcb.onError(str);
        }else{
            LogUtil.e(tag,str);
        }
    }

    public class DuaUser{
        public boolean logon=false;
        public String zone="+86";
        public String tel;
        public String ustr;
        public String pwd;
        public String avatar;
        public String sex;
        public String bday;
        public String name;
        public String role="member";
        public List<String> rules=new ArrayList<String>();
    }
    public DuaUser getCurrentDuaUser(){
        if(duaUser==null){
            String objStr= SharedPreferenceUtil.prefGetKey(context, DUA_LOCAL_STORAGE, "DuaUser", null);
            if (objStr != null) {
                try {
                    duaUser=gson.fromJson(Des3.decode(objStr), DuaUser.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    duaUser=new DuaUser();
                }
            }else {
                duaUser=new DuaUser();
            }
        }
        return duaUser;
    }
    public void duaUser_save(){
        try{
            SharedPreferenceUtil.prefSetKey(context, DUA_LOCAL_STORAGE, "DuaUser", Des3.encode(gson.toJson(duaUser)));
        }catch (Exception e){
            e.printStackTrace();
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

        public long lastOpenTime =TimeUtil.getCurrentTimeStamp();
        public long firtOpenTime =TimeUtil.getCurrentTimeStamp();
    }
    private  DuaLocalStorage duaLocalStorage_load(){
        if(duaLocalStorage==null){
            String objStr= SharedPreferenceUtil.prefGetKey(context, DUA_LOCAL_STORAGE, "DuaLocalStorage", null);
            if (objStr != null) {
                try {
                    //LogUtil.e(Des3.decode(objStr));
                    duaLocalStorage=gson.fromJson(Des3.decode(objStr), DuaLocalStorage.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    duaLocalStorage=new DuaLocalStorage();
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
            SharedPreferenceUtil.prefSetKey(context, DUA_LOCAL_STORAGE, "DuaLocalStorage", Des3.encode(gson.toJson(duaLocalStorage)));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public class DuaNetRequest {
        private String tag;
        private String url;
        private RequestBody body;
        private DuaCallback mcb;
        public DuaNetRequest(String tag,String url,String jstr,DuaCallback duaCallback){
            this.tag=tag;
            this.url=url;
            this.body=RequestBody.create(OkHttpUtil.MEDIA_TYPE_JSON, jstr);;
            this.mcb=duaCallback;
        }
        public DuaNetRequest(String tag,String url,RequestBody body,DuaCallback duaCallback){
            this.tag=tag;
            this.url=url;
            this.body=body;
            this.mcb=duaCallback;
        }

        protected void doSuccessExtra(String result){
            /*
            此方法存在的意义是请求成功后做一些不让用户知道的操作
            子类如果要重写该方法，可以在最后调用super.doSuccessExtra(result)
            或者自己通知用户请求成功并返回结果
             */
            callbackOnSuccess(mcb, tag,result);
        };
        protected void doErrorExtra(String err){
            /*
            此方法存在的意义是请求失败后做一些不让用户知道的操作
            子类如果要重写该方法，可以在最后调用super.doErrorExtra(err)
            或者自己通知用户请求失败并返回原因
             */
            callbackOnError(mcb,tag,err);
        };
        protected void dealResponse(String response){
            /**
             * 如果重写了这个方法就没有必要重写doSuccessExtra和doErrorExtra方法了
             */
            try {
                JSONObject jo=new JSONObject(response);
                String status=jo.getString("status");
                if(status!=null&&status.equals("0")){
                    String result=jo.getString("result");
                    doSuccessExtra(result);
                }else{
                    doErrorExtra(response);
                }
            }catch (Exception e){
                e.printStackTrace();
                doErrorExtra(e.toString());
            }
        }
        public final void doRequest(){
            if(!getNetworkStatus().equals("offline")){
                Callback callback=new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        doErrorExtra(e.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response!=null) {
                            if(response.isSuccessful()){
                                try {
                                    String str=response.body().string();
                                    if(!str.equals("")){
                                        dealResponse(str);
                                    }else{
                                        doErrorExtra("Response from server is string null");
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    doErrorExtra(e.toString());
                                }
                            }else {
                                doErrorExtra(response.message());
                            }

                        }else {
                            doErrorExtra("There is no response from server");
                        }
                    }
                };
                OkHttpUtil.asyncPost(url,body , callback);
            }else {
                doErrorExtra(NETWORK_OFFLINE);
            }
        }
    }
    public abstract class DuaIdRequest {
        private DuaCallback mcb;
        private DuaIdRequest(DuaCallback duaCallback){
            this.mcb=duaCallback;
        }
        public abstract void doWithDuaId(long dua_id);
        public void doRequest(){
            DuaCallback duaCallback =new DuaCallback(){

                @Override
                public void onSuccess(String str) {
                    doWithDuaId(Long.parseLong(str));
                }

                @Override
                public void onError(String str) {
                    callbackOnError(mcb,"DuaId",str);
                }
            };
            getCurrentDuaId(duaCallback);
        }
    }
}
