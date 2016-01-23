package com.example.lenovo.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.lenovo.coolweather.R;
import com.example.lenovo.coolweather.util.HttpCallBackInterface;
import com.example.lenovo.coolweather.util.HttpUtil;
import com.example.lenovo.coolweather.util.Utility;

/**
 * Created by lenovo on 2016/1/23.
 */
public class WeatherActivity extends Activity implements View.OnClickListener{
    private LinearLayout weatherInfoLayout;
    private TextView cityNameText;
    private TextView publishText;
    private TextView weatherDateText;
    private TextView weatherDescriptionText;
    private TextView temp1Text;
    private TextView temp2Text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        weatherInfoLayout=(LinearLayout)findViewById(R.id.weather_info_layout);
        cityNameText=(TextView)findViewById(R.id.city_name);
        publishText=(TextView)findViewById(R.id.publish_text);
        weatherDateText=(TextView)findViewById(R.id.weather_date);
        weatherDescriptionText=(TextView)findViewById(R.id.weather_description);
        temp1Text=(TextView)findViewById(R.id.temp1);
        temp2Text=(TextView)findViewById(R.id.temp2);
        Button switch_city=(Button)findViewById(R.id.switch_city);
        switch_city.setOnClickListener(this);
        Button refresh=(Button)findViewById(R.id.refresh);
        refresh.setOnClickListener(this);
        String countyCode=getIntent().getStringExtra("county_code");
        if(!TextUtils.isEmpty(countyCode)){
            publishText.setText("正在同步...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        }else {
            showWeather();
        }

    }
    private void queryWeatherCode(String countyCode){
        String address="http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
        queryWeatherFromServer(address,"countyCode");
    }
    private void queryWeatherInfo(String weatherCode){
        String address="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
        queryWeatherFromServer(address, "weatherCode");
    }
    private void queryWeatherFromServer(String adress, final String type){
        HttpUtil.sendHttpRequest(adress, new HttpCallBackInterface() {
            @Override
            public void onFinish(String data) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(data)){
                        String[] codes = data.split("\\|");
                        if (codes!=null&&codes.length == 2) {
                            String weatherCode = codes[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }

                } else if ("weatherCode".equals(type)) {
                    Utility.handleWeatherResponseFromJSON(WeatherActivity.this, data);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }
    private void showWeather(){
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        String cityName=sharedPreferences.getString("cityName", "");
        String temp1=sharedPreferences.getString("temp1","");
        String temp2=sharedPreferences.getString("temp2","");
        String weatherDecription=sharedPreferences.getString("weatherDecription","");
        String publishTime=sharedPreferences.getString("publishTime","");
        String weatherDate=sharedPreferences.getString("weatherDate","");
        cityNameText.setText(cityName);
        temp1Text.setText(temp1);
        temp2Text.setText(temp2);
        weatherDateText.setText(weatherDate);
        weatherDescriptionText.setText(weatherDecription);
        publishText.setText("今天"+publishTime+"发布");
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.switch_city:{
                Intent intent=new Intent(WeatherActivity.this,ChooseAreaActivity.class);
                intent.putExtra("isFromWeatherActivity",true);
                startActivity(intent);
                finish();
                break;
            }
            case R.id.refresh:{
                publishText.setText("正在更新...");
                SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences
                        (WeatherActivity.this);
                String weatherCode=sharedPreferences.getString("weatherCode","");
                if(!TextUtils.isEmpty(weatherCode)){
                    queryWeatherInfo(weatherCode);
                }
                break;
            }
            default:
                break;
        }
    }
}
