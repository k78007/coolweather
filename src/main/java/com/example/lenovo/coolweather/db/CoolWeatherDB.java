package com.example.lenovo.coolweather.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.lenovo.coolweather.model.City;
import com.example.lenovo.coolweather.model.County;
import com.example.lenovo.coolweather.model.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2016/1/18.
 */
public class CoolWeatherDB {
    public static final String DB_NAME="cool_weather";
    public static final int DB_VERSION=1;
    private SQLiteDatabase db;
    private static CoolWeatherDB coolWeatherDB;
    private CoolWeatherDB(Context context) {
        CoolWeatherHelpler coolWeatherHelpler=new CoolWeatherHelpler(context,DB_NAME,null,DB_VERSION);
        db=coolWeatherHelpler.getWritableDatabase();
    }
    public synchronized static CoolWeatherDB getInstance(Context context){
        if(coolWeatherDB==null){
            coolWeatherDB=new CoolWeatherDB(context);
        }
        return coolWeatherDB;
    }
    //省份储存到数据库
    public void saveProvince(Province province){
        if(province!=null){
            ContentValues contentValues=new ContentValues();
            contentValues.put("province_name",province.getProvinceName());
            contentValues.put("province_code",province.getProvinceCode());
            db.insert("Province",null,contentValues);
        }
    }
    //从数据库读取所有省份
    public List<Province> loadProvinces(){
        List<Province> list=new ArrayList<Province>();
        Cursor cursor=db.query("Province",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do {
                Province province=new Province();
                province.setId((cursor.getInt(cursor.getColumnIndex("id"))));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                list.add(province);
            }while (cursor.moveToNext());
        }
        if(cursor!=null){
            cursor.close();
        }
        return list;
    }
    //把城市数据存入数据库
    public void saveCity(City city){
        if(city!=null){
            ContentValues contentValues=new ContentValues();
            contentValues.put("city_name",city.getCityName());
            contentValues.put("city_code",city.getCityCode());
            contentValues.put("province_id",city.getProvinceId());
            db.insert("City", null, contentValues);
        }
    }
    //从数据库提取某省的所有城市
    public List<City> loadCities(int provinceId){
        List<City> list=new ArrayList<City>();
        Cursor cursor=db.query("City",null,"province_id=?",new String[]{String.valueOf(provinceId)},null,null,null);
        if(cursor.moveToFirst()){
            do {
                City city=new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));

                //两句达到的结果相同? 是的
                city.setProvinceId(cursor.getInt(cursor.getColumnIndex("province_id")));
//                city.setProvinceId(provinceId);
                list.add(city);
            }while (cursor.moveToNext());
        }
        if(cursor!=null){
            cursor.close();
        }
        return list;
    }
    //把县的信息传入数据库
    public void saveCounty(County county){
        if(county!=null){
            ContentValues contentValues=new ContentValues();
            contentValues.put("county_name",county.getCountyName());
            contentValues.put("county_code",county.getCountyCode());
            contentValues.put("city_id",county.getCityId());
            db.insert("County", null, contentValues);
        }
    }
    public List<County> loadCounties(int cityId){
        List<County> list=new ArrayList<County>();
        Cursor cursor=db.query("County",null,"city_id=?",new String[]{String.valueOf(cityId)},null,null,null);
        if(cursor.moveToFirst()){
            do {
                County county=new County();
                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setCityId(cityId);
                list.add(county);
            }while (cursor.moveToNext());
        }
        if(cursor!=null){
            cursor.close();
        }
        return list;
    }

}
