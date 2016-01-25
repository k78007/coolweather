package com.example.lenovo.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.lenovo.coolweather.db.CoolWeatherDB;
import com.example.lenovo.coolweather.model.City;
import com.example.lenovo.coolweather.model.County;
import com.example.lenovo.coolweather.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by lenovo on 2016/1/22.
 */
public class Utility {
    //为何加锁？？
    public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,String response){
        if(!TextUtils.isEmpty(response)){
            String[] codeAndName=response.split(",");
            if(codeAndName!=null&&codeAndName.length>0){
                for(String string:codeAndName){
                    String[] data=string.split("\\|");
                    Province province=new Province();
                    province.setProvinceCode(data[0]);
                    province.setProvinceName(data[1]);
                    coolWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }
    public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            String[] codeAndName=response.split(",");
            if(codeAndName!=null&&codeAndName.length>0){
                for(String string:codeAndName){
                    String[] data=string.split("\\|");
                    City city=new City();
                    city.setCityCode(data[0]);
                    city.setCityName(data[1]);
                    city.setProvinceId(provinceId);
                    coolWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }
    public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB,String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            String[] codeAndName=response.split(",");
            if(codeAndName!=null&&codeAndName.length>0){
                for(String string:codeAndName){
                    String[] data=string.split("\\|");
                    County county=new County();
                    county.setCountyCode(data[0]);
                    county.setCountyName(data[1]);
                    county.setCityId(cityId);
                    coolWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }

    //用此方法解析的API已无法更新，因此此方法暂时废弃
    public static void handleWeatherResponseFromJSONOld(Context context,String response){
        try {
            JSONObject jsonObject=new JSONObject(response);
            JSONObject weatherObjece=jsonObject.getJSONObject("weatherinfo");
            String cityName=weatherObjece.getString("city");
            String weatherCode=weatherObjece.getString("cityid");
            String temp1=weatherObjece.getString("temp1");
            String temp2=weatherObjece.getString("temp2");
            String weatherDecription=weatherObjece.getString("weather");
            String publishTime=weatherObjece.getString("ptime");
            saveWeatherInfo(context,cityName,weatherCode,temp1,temp2,weatherDecription,publishTime,null,null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static void handleWeatherResponseFromJSON(Context context,String response){
        try {
            JSONObject jsonObject=new JSONObject(response);
            JSONObject weatherObjece=jsonObject.getJSONObject("retData");
            String cityName=weatherObjece.getString("city");
            String weatherCode=weatherObjece.getString("cityid");
            JSONObject todayObject=weatherObjece.getJSONObject("today");
            String temp1=todayObject.getString("hightemp");
            String temp2=todayObject.getString("lowtemp");
            String weatherDecription=todayObject.getString("type");
            String weatherDate=todayObject.getString("date")+"  "+todayObject.getString("week");
            String currentTemp=todayObject.getString("curTemp");

            //需要更改：
            String publishTime=getPublishTime();

            saveWeatherInfo(context,cityName,weatherCode,temp1,temp2,weatherDecription,weatherDate,publishTime,currentTemp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private static void saveWeatherInfo(Context context,String cityName,String weatherCode,String temp1,
                                       String temp2, String weatherDecription,String weatherDate,
                                        String publishTime,String currentTemp){
        SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("cityIsSelected",true);
        editor.putString("cityName", cityName);
        editor.putString("weatherCode",weatherCode);
        editor.putString("temp1",temp1);
        editor.putString("temp2",temp2);
        editor.putString("weatherDecription",weatherDecription);
        editor.putString("publishTime",publishTime);
        editor.putString("weatherDate",weatherDate);
        editor.putString("currentTemp",currentTemp);
        editor.commit();
    }
    //获取发布时间方法
    // 由于使用的API不提供发布时间参数，需要另行获取。
    //API说明上显示更新天气为三个时段：每日08时、11时、18时；
    //因此通过判断请求获取服务器数据的时间来得到天气发布时间。
    private  static String getPublishTime(){

        TimeZone tz =TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone.setDefault(tz);

        //为何此句不起作用？
//        Calendar calendar1=Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"),Locale.CHINA);

        Calendar calendarNow=Calendar.getInstance(Locale.CHINA);
        Calendar calendar1=Calendar.getInstance(Locale.CHINA);
        Calendar calendar2=Calendar.getInstance(Locale.CHINA);
        Calendar calendar3=Calendar.getInstance(Locale.CHINA);
        calendar1.set(Calendar.HOUR_OF_DAY,8);
        calendar1.set(Calendar.MINUTE,0);
        calendar1.set(Calendar.SECOND,5);
        calendar2.set(Calendar.HOUR_OF_DAY,11);
        calendar2.set(Calendar.MINUTE,0);
        calendar2.set(Calendar.SECOND,5);
        calendar3.set(Calendar.HOUR_OF_DAY,18);
        calendar3.set(Calendar.MINUTE, 0);
        calendar3.set(Calendar.SECOND,5);
        String publishTime=null;
        Log.d("TAG", "calendar1-----------"+calendar1.getTime().toString());
        Log.d("TAG", "calendar2-----------"+calendar2.getTime().toString());
        Log.d("TAG", "calendar3-----------"+calendar3.getTime().toString());
        Log.d("TAG", "calendarNow-----------"+calendarNow.getTime().toString());
        if(calendarNow.after(calendar1)&&calendarNow.before(calendar2)){
            publishTime="8:00";
        }else if(calendarNow.after(calendar2)&&calendarNow.before(calendar3)){
            publishTime="11:00";
        }else {
            publishTime="18:00";
        }
        return publishTime;
    }

}
