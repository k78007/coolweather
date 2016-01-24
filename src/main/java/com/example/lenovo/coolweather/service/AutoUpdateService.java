package com.example.lenovo.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.example.lenovo.coolweather.receiver.UpdateReceiver;
import com.example.lenovo.coolweather.util.HttpCallBackInterface;
import com.example.lenovo.coolweather.util.HttpUtil;
import com.example.lenovo.coolweather.util.Utility;

/**
 * Created by lenovo on 2016/1/24.
 */
public class AutoUpdateService extends Service{
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();
        AlarmManager alarmManager=(AlarmManager)getSystemService(ALARM_SERVICE);
        long triggerAtMillis= SystemClock.elapsedRealtime()+5*60*1000;
        Intent intent1=new Intent(this, UpdateReceiver.class);
        //第四个参数是0？
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,intent1,PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis,pendingIntent);
//        此句不用加？
//        alarmManager.notify();
        return super.onStartCommand(intent, flags, startId);
    }
    //只讲更新的数据储存到SharedPreferences，不更新UI！
    private void updateWeather(){
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherCode=sharedPreferences.getString("weatherCode","");
        if(!TextUtils.isEmpty(weatherCode)){
            String address="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
            HttpUtil.sendHttpRequest(address, new HttpCallBackInterface() {
                @Override
                public void onFinish(String data) {
                    Utility.handleWeatherResponseFromJSON(AutoUpdateService.this, data);
                    Log.d("TAG", "----auto update weather ");
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

}
