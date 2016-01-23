package com.example.lenovo.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.example.lenovo.coolweather.db.CoolWeatherDB;
import com.example.lenovo.coolweather.model.City;
import com.example.lenovo.coolweather.model.County;
import com.example.lenovo.coolweather.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    public static void handleWeatherResponseFromJSON(Context context,String response){
        try {
            JSONObject jsonObject=new JSONObject(response);
            JSONObject weatherObjece=jsonObject.getJSONObject("weatherinfo");
            String cityName=weatherObjece.getString("city");
            String weatherCode=weatherObjece.getString("cityid");
            String temp1=weatherObjece.getString("temp1");
            String temp2=weatherObjece.getString("temp2");
            String weatherDecription=weatherObjece.getString("weather");
            String publishTime=weatherObjece.getString("ptime");
            saveWeatherInfo(context,cityName,weatherCode,temp1,temp2,weatherDecription,publishTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public static void saveWeatherInfo(Context context,String cityName,String weatherCode,String temp1,
                                       String temp2, String weatherDecription,String publishTime){
        SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(context).edit();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
        editor.putBoolean("cityIsSelected",true);
        editor.putString("cityName", cityName);
        editor.putString("weatherCode",weatherCode);
        editor.putString("temp1",temp1);
        editor.putString("temp2",temp2);
        editor.putString("weatherDecription",weatherDecription);
        editor.putString("publishTime",publishTime);
        editor.putString("weatherDate",simpleDateFormat.format(new Date()));
        editor.commit();
    }

}
