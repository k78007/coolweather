package com.example.lenovo.coolweather.util;

import android.text.TextUtils;

import com.example.lenovo.coolweather.db.CoolWeatherDB;
import com.example.lenovo.coolweather.model.City;
import com.example.lenovo.coolweather.model.County;
import com.example.lenovo.coolweather.model.Province;

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
}
