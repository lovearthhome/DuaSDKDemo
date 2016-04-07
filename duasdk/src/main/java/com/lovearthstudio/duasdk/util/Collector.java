package com.lovearthstudio.duasdk.util;


import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Collector {
    public Context context;
    public PackageManager pm;
    public PackageInfo pkg;
    public ApplicationInfo app;
    public ConnectivityManager cm;
    public TelephonyManager tm;
    public LocationManager lm;
    public Location curLocation;
    public boolean locationListened;

    public Collector(Context context){
        this.context = context;
        this.pm = context.getPackageManager();
        this.cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        this.lm= (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        this.locationListened=registerLocationListener();
        try {
            this.pkg = pm.getPackageInfo(getPackageName(), 0);
            this.app = pm.getApplicationInfo(getPackageName(), 0);
        }catch (Exception e){

        }

    }

    public String getPackageName() {
        return context.getPackageName();
    }

    public String getAppName() {
        return (String) pm.getApplicationLabel(app);
    }

    public String getVersionNumber() {
        return pkg.versionName;
    }

    public int getVersionCode() {
        return pkg.versionCode;
    }

    public String getUuid() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getOSVersion() {
        return Build.VERSION.RELEASE;
    }

    public String getModel() {
        return Build.MODEL;
    }

    public String getProductName() {
        return Build.PRODUCT;
    }

    public String getManufacturer() {
        return Build.MANUFACTURER;
    }

    public String getSerialNumber() {
        return Build.SERIAL;
    }

    public String getPlatform() {
        if (Build.MANUFACTURER.equals("Amazon")) {
            return "Amazon-fireos";
        } else {
            return "Android";
        }
    }

    public JSONArray getAppLists() throws JSONException {
        List<PackageInfo> ps = pm.getInstalledPackages(0);
        JSONArray ja = new JSONArray();
        for (PackageInfo p : ps) {
            JSONObject jo = new JSONObject();
            jo.put("aname", p.applicationInfo.loadLabel(pm).toString());
            jo.put("pname", p.packageName);
            jo.put("version", p.versionName == null ? "na" : p.versionName);
            jo.put("code", p.versionCode);
            jo.put("system", ((p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) ? 1 : 0);
//            Bitmap bitmap=((BitmapDrawable)p.applicationInfo.loadIcon(pm)).getBitmap();
//            jo.put("icon",encodeToBase64(bitmap));
            ja.put(jo);
        }
        return ja;
    }

    public List<JSONObject> getAppStats(long startTime, long endTime, int type) throws JSONException {
        List<JSONObject> jl = new ArrayList<JSONObject>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // UsageStatsManager usm = getUsageStatsManager(context);
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            int interval;
            switch (type) {
                case 1:
                    interval = UsageStatsManager.INTERVAL_DAILY;
                    break;
                case 2:
                    interval = UsageStatsManager.INTERVAL_WEEKLY;
                    break;
                case 3:
                    interval = UsageStatsManager.INTERVAL_MONTHLY;
                    break;
                case 4:
                    interval = UsageStatsManager.INTERVAL_YEARLY;
                    break;
                default:
                    interval = UsageStatsManager.INTERVAL_DAILY;
                    break;
            }
            List<UsageStats> usageStatsList = usm.queryUsageStats(interval, startTime, endTime);
            for (UsageStats app : usageStatsList) {
                JSONObject jo = new JSONObject();
                jo.put("pname", app.getPackageName());
                jo.put("fts", app.getFirstTimeStamp());
                jo.put("lts", app.getLastTimeStamp());
                jo.put("ltu", app.getLastTimeUsed());
                jo.put("ttf", app.getTotalTimeInForeground());
                jo.put("cnt", -1);
                jo.put("type", type);
                jl.add(jo);
            }
        }
        return jl;
    }

    public List<JSONObject> getNCI() throws Exception {
        List<JSONObject> nci = new ArrayList<JSONObject>();
        if (Build.VERSION.SDK_INT >= 18) {
            String[] permissions = new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION};
            if (!checkPermissionAPI(permissions,"and", 119)) {
                return nci;
            }
            int type = tm.getNetworkType();
            //之所以分成4类，是因为安卓api中cellinfo分为四个子类
            String networkType = "gsm";/*gsm,cdma,wcdma,lte,unknown*/
            switch (type) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    networkType = "gsm";
                    break;
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    networkType = "cdma";
                    break;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    networkType = "lte";
                    break;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    networkType = "unknown";
                    break;
            }
            List<CellInfo> list = tm.getAllCellInfo();
            if(tm!=null&&list.size()>0)  {
                for (int i = 0; i < list.size(); i++) {
                    int mcc = 0;
                    int mnc = 0;
                    int lac = 0;
                    int cid = 0;
                    int dbm = 0;
                    int reg = list.get(i).isRegistered() ? 1 : 0;//这个网络是否在这个SIM卡上注册：如果注册，说明这个网络是附近最好的.isRegistered(): True if this cell is registered to the mobile network
                    if (networkType == "gsm") {
                        CellInfoGsm info = (CellInfoGsm) list.get(i);
                        mcc = info.getCellIdentity().getMcc();
                        mnc = info.getCellIdentity().getMnc();
                        lac = info.getCellIdentity().getLac();
                        cid = info.getCellIdentity().getCid();//CID Either 16-bit GSM Cell Identity described in TS 27.007, 0..65535, Integer.MAX_VALUE if unknown
                        dbm = info.getCellSignalStrength().getDbm();//getDbm() Get the signal strength as dBm
                    } else if (networkType == "cmda") {
                        CellInfoCdma info = (CellInfoCdma) list.get(i);
                        mcc = Integer.parseInt(tm.getNetworkOperator().substring(0, 3));//从网上得知cdma需要如此得到mcc: http://blog.csdn.net/lancees/article/details/7616735
                        mnc = Integer.parseInt(tm.getNetworkOperator().substring(3, 5));
                        lac = info.getCellIdentity().getNetworkId();
                        cid = info.getCellIdentity().getBasestationId();//Base Station Id 0..65535, Integer.MAX_VALUE if unknown
                        dbm = info.getCellSignalStrength().getDbm();//getDbm() Get the signal strength as dBm
                    } else if (networkType == "lte") {
                        CellInfoLte info = (CellInfoLte) list.get(i);
                        mcc = info.getCellIdentity().getMcc(); //返回整形：3-digit Mobile Country Code, 0..999, Integer.MAX_VALUE if unknown
                        mnc = info.getCellIdentity().getMnc(); //返回整形：2 or 3-digit Mobile Network Code, 0..999, Integer.MAX_VALUE if unknown
                        lac = info.getCellIdentity().getTac(); //我们不清楚是不是lac,文档中标明：16-bit Tracking Area Code, Integer.MAX_VALUE if unknown
                        cid = info.getCellIdentity().getCi(); //我们也不清楚ci是否就是cid:lte中的ci竟然是28bit的：28-bit Cell Identity, Integer.MAX_VALUE if unknown
                        dbm = info.getCellSignalStrength().getDbm();//getDbm() Get the signal strength as dBm
                    } else if (networkType == "wcdma") {
                        CellInfoWcdma info = (CellInfoWcdma) list.get(i);
                        mcc = info.getCellIdentity().getMcc(); //返回整形：3-digit Mobile Country Code, 0..999, Integer.MAX_VALUE if unknown
                        mnc = info.getCellIdentity().getMnc(); //返回整形：2 or 3-digit Mobile Network Code, 0..999, Integer.MAX_VALUE if unknown
                        lac = info.getCellIdentity().getLac(); //16-bit Location Area Code, 0..65535, Integer.MAX_VALUE if unknown
                        cid = info.getCellIdentity().getCid(); //竟然是28bit:CID 28-bit UMTS Cell Identity described in TS 25.331, 0..268435455, Integer.MAX_VALUE if unknown
                        dbm = info.getCellSignalStrength().getDbm();//getDbm() Get the signal strength as dBm
                    }
                    JSONObject jo = new JSONObject();
//            jo.put("typ",networkType);
//            jo.put("reg",reg);
//            jo.put("mcc",mcc);
//            jo.put("mnc",mnc);
//            jo.put("lac",lac);
//            jo.put("cid",cid);
//            jo.put("dbm",dbm);
                    jo.put("type", "T");
                    jo.put("id", mcc + ":" + mnc + ":" + lac + ":" + cid);
                    jo.put("name", "N/A");
                    jo.put("dbm", dbm);
                    jo.put("reg", reg);      //此基站当前是否连接
                    nci.add(jo);
                }
            }
        }
        return nci;
    }

    public List<JSONObject> getWifiList(int... numLevels) throws Exception {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> scanResults = wifiManager.getScanResults();
        List<JSONObject> wifis = new ArrayList<JSONObject>();
        for (ScanResult scan : scanResults) {
            int defaultLevel = 5; //default value;
            int level;
            if (numLevels.length == 1) {
                if (numLevels[0] == 0) {  //no need calculate
                    level = scan.level;
                } else {
                    level = wifiManager.calculateSignalLevel(scan.level, numLevels[0]);
                }
            } else {
                level = wifiManager.calculateSignalLevel(scan.level, defaultLevel);
            }
            JSONObject jo = new JSONObject();
//            jo.put("level", level);
//            jo.put("SSID", scan.SSID);
//            jo.put("BSSID", scan.BSSID);
//            jo.put("frequency", scan.frequency);
//            jo.put("capabilities", scan.capabilities);
//            jo.put("timestamp", scan.timestamp);
            jo.put("type", "F");
            jo.put("id", scan.BSSID);
            jo.put("name", scan.SSID);
            jo.put("dbm", level);
            jo.put("reg", 0);      //此wifi是否已连接
            wifis.add(jo);
        }
        return wifis;
    }

    public List<JSONObject> getBlueTeeth() throws Exception {
        List<JSONObject> bt = new ArrayList<JSONObject>();
        return bt;
    }

    public class MyLocationListener implements LocationListener{
        @Override
        public void onLocationChanged(Location location) {
            if(isBetterLocation(curLocation,location)){
                curLocation=location;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            if(!locationListened){
                locationListened=registerLocationListener();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            lm.removeUpdates(this);
        }
    };
    public boolean registerLocationListener(){
        if(lm!=null){
            String[] permissions = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
            if(checkPermissionAPI(permissions,"or",110)) {
                boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if(isNetworkEnabled){
                    MyLocationListener networkListner=new MyLocationListener();
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0, networkListner);
                }
                if(isGPSEnabled){
                    MyLocationListener gpsListener=new MyLocationListener();
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, gpsListener);
                }
                return isNetworkEnabled||isGPSEnabled;
            }
        }
        return false;
    }
    public Location getCurrentLocation() {
        if(tm==null) return null;
        if(locationListened){
            return curLocation;
        }else{
            Criteria criteria = new Criteria();
            return lm.getLastKnownLocation(lm.getBestProvider(criteria, false));
        }
    }

    public boolean isBetterLocation(Location currentBestLocation,Location location){
        final int CHECK_INTERVAL = 1000 * 30;
        if (currentBestLocation == null) {
            return true;
        }
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > CHECK_INTERVAL;
        boolean isSignificantlyOlder = timeDelta < -CHECK_INTERVAL;
        boolean isNewer = timeDelta > 0;
        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;
        boolean isFromSameProvider = location.getProvider().equals(currentBestLocation.getProvider());
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate
                && isFromSameProvider) {
            return true;
        }
        return false;
    }

    public String getNetWorkTye() {
        switch (tm.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";

            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";

            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";

            default:
                return "unknown";
        }
    }

    public String getNetWorkStatus() {
        String netWorkType = "offline";
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            int type = networkInfo.getType();
            if (type == ConnectivityManager.TYPE_WIFI) {
                netWorkType = "wifi";
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                netWorkType = getNetWorkTye();
            }
        }
        return netWorkType;
    }

    public boolean checkPermissionAPI(String[] permissions,String type,int trigger){
        if(Build.VERSION.SDK_INT>=23){
            if(type.equals("or")){
                boolean getOne=false;
                for(String permission:permissions){
                    if(ContextCompat.checkSelfPermission( context, permission ) == PackageManager.PERMISSION_GRANTED){
                        getOne=true;
                        break;
                    }
                }
                if(!getOne){
//                ActivityCompat.requestPermissions( activity,permissions,trigger);
                    return false;
                }
            }else{
                boolean allGot=true;
                for(String permission:permissions){
                    if(ContextCompat.checkSelfPermission( context, permission ) != PackageManager.PERMISSION_GRANTED){
                        allGot=false;
                    }
                }
                if(!allGot){
//                ActivityCompat.requestPermissions( activity,permissions,trigger);
                    return false;
                }
            }
        }
        return true;
    }
}
