package com.lovearthstudio.duasdk.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author：Mingyu Yi on 2016/5/15 21:46
 * Email：461072496@qq.com
 */
public class JsonUtil {
    public static JSONObject tryJsonObject(String str){
        JSONObject jo=null;
        try {
            jo=new JSONObject(str);
        }catch (JSONException e1){
            String s=str;
            while (jo==null&&s.startsWith("\"")&&s.endsWith("\"")){
                s= simpleNoQuotes(s);
                try {
                    jo=new JSONObject(s);
                } catch (JSONException e2) {
                }
            }
        }
        return jo;
    }

    public static JSONArray tryJsonArray(String str){
        JSONArray ja=null;
        try {
            ja=new JSONArray(str);
        }catch (JSONException e1){
            String s=str;
            while (ja==null&&s.startsWith("\"")&&s.endsWith("\"")){
                s= simpleNoQuotes(s);
                try {
                    ja=new JSONArray(s);
                } catch (JSONException e2) {
                }
            }
        }
        return ja;
    }

    public static String deepNoQuotes(String str){
        String tmp=str;
        while (tmp.startsWith("\"")&&tmp.endsWith("\"")){
            tmp=tmp.substring(1,tmp.length()-1);
            String regex = "\\\\";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(tmp);

            int lastIndex=-1;
            StringBuffer sb = new StringBuffer();
            while (matcher.find()){
                if(matcher.start()>lastIndex){
                    matcher.appendReplacement(sb, "");
                    lastIndex=matcher.end();
                }
            }
            matcher.appendTail(sb);
            tmp=sb.toString();
        }
        return tmp;
    }

    public static String simpleNoQuotes(String str){
        if (!str.startsWith("\"")||!str.endsWith("\"")){
            return str;
        }
        String tmp=str.substring(1,str.length()-1);
        String regex = "\\\\";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(tmp);

        int lastIndex=-1;
        StringBuffer sb = new StringBuffer();
        while (matcher.find()){
            if(matcher.start()>lastIndex){
                matcher.appendReplacement(sb, "");
                lastIndex=matcher.end();
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static JSONObject toJsonObject(String json){
        JSONObject obj= tryJsonObject(json);
        if(obj!=null) {
            return jsonDecode(obj);
        }else {
            return obj;      //null
        }
    }

    public static JSONArray toJsonArray(String json){
        JSONArray array= tryJsonArray(json);
        if(array!=null) {
            return jsonDecode(array);
        }else {
            return array;      //null
        }
    }

    public static JSONObject jsonDecode(JSONObject obj){
        JSONObject jsonObject=null;
        if (obj!=null){
            jsonObject=new JSONObject();
            if (obj.length()>0){
                Iterator<?> keys = obj.keys();
                while( keys.hasNext() ) {
                    String key = (String)keys.next();
                    Object value=null;
                    try {
                        value = obj.get(key);
                    } catch (JSONException e) {
                    }
                    key= deepNoQuotes(key);
                    if(value!=null){
                        if ( value instanceof JSONObject) {
                            try {
                                jsonObject.put(key,jsonDecode((JSONObject)value));
                            } catch (JSONException e) {
                            }
                        } else if (value instanceof JSONArray){
                            try {
                                jsonObject.put(key,jsonDecode((JSONArray) value));
                            } catch (JSONException e) {
                            }
                        } else if(value instanceof String){
                            String str=(String)value;
                            JSONArray ja=toJsonArray(str);
                            if(ja==null){
                                JSONObject jo=toJsonObject(str);
                                if(jo==null){
                                    try {
                                        jsonObject.put(key, deepNoQuotes(str));
                                    } catch (JSONException e) {
                                    }
                                }else {
                                    try {
                                        jsonObject.put(key,jo);
                                    } catch (JSONException e) {
                                    }
                                }
                            }else {
                                try {
                                    jsonObject.put(key,ja);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            try {
                                jsonObject.put(key,value);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }else {
                        try {
                            jsonObject.put(key,value);  //null
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return jsonObject;
    }

    public static JSONArray jsonDecode(JSONArray array){
        JSONArray jsonArray=null;

        if (array!=null){
            jsonArray=new JSONArray();
            if(array.length()>0){
                for (int i = 0; i <array.length() ; i++) {
                    Object obj=null;
                    try {
                        obj=array.get(i);
                    } catch (JSONException e) {
                    }
                    if(obj!=null){
                        if(obj instanceof JSONObject){
                            jsonArray.put(jsonDecode((JSONObject)obj));
                        }else if (obj instanceof JSONArray){
                            jsonArray.put(jsonDecode((JSONArray)obj));
                        }else if (obj instanceof String){
                            String str=(String)obj;
                            JSONArray ja=toJsonArray(str);
                            if(ja==null){
                                JSONObject jo=toJsonObject(str);
                                if(jo==null){
                                    jsonArray.put(deepNoQuotes(str)); //jsonArray put String ????
                                }else {
                                    jsonArray.put(jo);
                                }
                            }else {
                                jsonArray.put(ja);
                            }
                        }else {
                            jsonArray.put(obj);  //jsonArray put 整型？？？？
                        }
                    }else {
                        jsonArray.put(obj);  // 好像多余
                    }
                }
            }
        }
        return jsonArray;
    }

}
