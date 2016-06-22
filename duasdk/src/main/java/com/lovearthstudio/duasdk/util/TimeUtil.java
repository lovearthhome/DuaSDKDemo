package com.lovearthstudio.duasdk.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class TimeUtil {
    public static String getCurrentTimeString(){
        return toTimeString(getCurrentTimeStamp());
    }
    public static String getCurrentTimeString(String fmt){
        return toTimeString(getCurrentTimeStamp(),fmt);
    }
    public static long getCurrentTimeStamp(){
        return Calendar.getInstance().getTimeInMillis();
    }
    public static long getYearsAgo(int ys){
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.YEAR, ys);
        return calendar.getTimeInMillis();
    }
    public static long getDaysAgo(int ds){
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DATE, ds);
        return calendar.getTimeInMillis();
    }
    public static String toTimeString(long ts){
        return toTimeString(ts,null);
    }
    public static String toTimeString(long ts,String fmt){
        if(fmt==null||fmt.equals("")) fmt="yyyy-MM-dd HH:mm:ss:SS";
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(fmt);
            Calendar calendar=Calendar.getInstance();
            calendar.setTimeInMillis(ts);
            String timeStr = dateFormat.format(calendar.getTime());
            return timeStr;
        } catch (Exception e) {
            e.printStackTrace();
            return "Invalid timestamp or format";
        }
    }
    public static int[] getWeekOfMonthInfo(long ts){
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(ts);

        int day=calendar.get(Calendar.DAY_OF_WEEK);  //1-7
        int wn=calendar.get(Calendar.WEEK_OF_MONTH); //0-6
        int month=calendar.get(Calendar.MONTH);      //0-11
        int year=calendar.get(Calendar.YEAR);
        return new int[]{day,wn,month,year};
    }
    public static int[] getMonthOfYearInfo(long ts){
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(ts);
        int dm=calendar.get(Calendar.DAY_OF_MONTH);   //1-31
        int month=calendar.get(Calendar.MONTH);
        int year=calendar.get(Calendar.YEAR);
        return new int[]{dm,month,year};
    }
    public static int[] getYearRangeInfo(long ts){
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(ts);
        int dn=calendar.get(Calendar.DAY_OF_YEAR);   //1-366
        int year=calendar.get(Calendar.YEAR);
        return new int[]{dn,year};
    }
    public static int[] rangeGetIndex(long ts1,long ts2,int type){
        int[] index={};
        switch (type){
            case 2 :{    //七天大部分处于哪一月哪一周
                int[] p1=getWeekOfMonthInfo(ts1);
                int[] p2=getWeekOfMonthInfo(ts2);
                int wn,month,year;
                if(p1[0]+p2[0]<7){
                    wn=p1[1]+1;
                    month=p1[2]+1;
                    year=p1[3];
                }else{
                    wn=p2[1]+1;
                    month=p2[2]+1;
                    year=p2[3];
                }
                index=new int[]{wn,month,year};
                break;
            }
            case 3 : {       //30天大部分处于哪一年哪一月
                int[] p1=getMonthOfYearInfo(ts1);
                int[] p2=getMonthOfYearInfo(ts2);
                int month,year;
                if(p1[0]+p2[0]<30){
                    month=p1[1]+1;
                    year=p1[2];
                }else{
                    month=p2[1]+1;
                    year=p2[2];
                }
                index=new int[]{month,year};
                break;
            }
            case 4 : {      //365天大部分处于哪一年
                int[] p1=getYearRangeInfo(ts1);
                int[] p2=getYearRangeInfo(ts2);
                int year=p1[0]+p2[0]<365?p1[1]:p2[1];
                index=new int[]{year};
            }
        }
        return index;
    }
}
