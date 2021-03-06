package com.example.lenovo.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.coolweather.R;
import com.example.lenovo.coolweather.db.CoolWeatherDB;
import com.example.lenovo.coolweather.model.City;
import com.example.lenovo.coolweather.model.County;
import com.example.lenovo.coolweather.model.Province;
import com.example.lenovo.coolweather.util.HttpCallBackInterface;
import com.example.lenovo.coolweather.util.HttpUtil;
import com.example.lenovo.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2016/1/22.
 */
public class ChooseAreaActivity extends Activity {
    public final static int LEVEL_PROVINCE = 0;
    public final static int LEVEL_CITY = 1;
    public final static int LEVEL_COUNTY = 2;
    private int currentLevel;
    private TextView titleView;
    private ListView listView;
    private ProgressDialog progressDialog;
    private ArrayAdapter<String> arrayAdapter;
    private CoolWeatherDB coolWeatherDB;
    private List<String> datalist = new ArrayList<String>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        boolean cityIsSelected=sharedPreferences.getBoolean("cityIsSelected", false);
        boolean isFromWeatherActivity=getIntent().getBooleanExtra("isFromWeatherActivity",false);
        //如果市县被选定，且当前活动不是由天气信息活动启动的，则直接启动天气信息活动显示上次选定的市县天气。
        if(cityIsSelected&&!isFromWeatherActivity){
            Intent intent=new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
            //不加return?? 可以
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        titleView = (TextView) findViewById(R.id.text_title);
        listView = (ListView) findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, datalist);
        listView.setAdapter(arrayAdapter);
        coolWeatherDB = CoolWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if(currentLevel==LEVEL_COUNTY){
                    String countyCode=countyList.get(position).getCountyCode();
                    Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
                    intent.putExtra("county_code",countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvinces();

    }
    //优先从数据库查询省数据，无数据再从服务器查询。
    private void queryProvinces() {
        provinceList=coolWeatherDB.loadProvinces();
        if(provinceList.size()>0){
            datalist.clear();
            for(Province province:provinceList){
                datalist.add(province.getProvinceName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleView.setText("中国");
            currentLevel=LEVEL_PROVINCE;
        }
        else{
            queryListViewContentFromServer(null,"province");
        }
    }
    //优先从数据库查询市数据，无数据再从服务器查询。
    private void queryCities() {
        cityList=coolWeatherDB.loadCities(selectedProvince.getId());
        if(cityList.size()>0){
            datalist.clear();
            for (City city:cityList){
                datalist.add(city.getCityName());
            }
            arrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleView.setText(selectedProvince.getProvinceName());
            currentLevel=LEVEL_CITY;
        }
        else {
            queryListViewContentFromServer(selectedProvince.getProvinceCode(),"city");
        }
    }
    //优先从数据库查询县数据，无数据再从服务器查询。
    private void queryCounties() {
        countyList=coolWeatherDB.loadCounties(selectedCity.getId());
        if(countyList.size()>0){
            datalist.clear();
            for(County county:countyList){
                datalist.add(county.getCountyName());
            }
            arrayAdapter.notifyDataSetChanged();
            titleView.setText(selectedCity.getCityName());
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }
        else {
            queryListViewContentFromServer(selectedCity.getCityCode(),"county");
        }

    }
    //从服务器查询数据。此处使用的中国气象网的API仅用于显示城市列表和查询城市的ID
    private void queryListViewContentFromServer(final String code, final String type){
        String address;
        if(!TextUtils.isEmpty(code)){
            address="http://www.weather.com.cn/data/list3/city"+code+".xml";
        }
        else {
            address="http://www.weather.com.cn/data/list3/city.xml";
        }

        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallBackInterface() {
            @Override
            public void onFinish(String data) {
                boolean result=false;
                if("province".equals(type)){
                    result=Utility.handleProvincesResponse(coolWeatherDB,data);
                }
                else if("city".equals(type)){
                    result=Utility.handleCitiesResponse(coolWeatherDB,data,selectedProvince.getId());
                }
                else if("county".equals(type)){
                    result=Utility.handleCountiesResponse(coolWeatherDB,data,selectedCity.getId());
                }
                if(result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }
                            else if("city".equals(type)){
                                queryCities();
                            }
                            else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,"加载失败",Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    //显示ProgressDialog控件
    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(this);
            progressDialog.setMessage("Loading....");
           progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();

    }
    //关闭ProgressDialog控件
    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }

    }
    //重写返回按钮
    @Override
    public void onBackPressed() {
        if(currentLevel==LEVEL_CITY){
            queryProvinces();
        }
        else if(currentLevel==LEVEL_COUNTY){
            queryCities();
        }else if(getIntent().getBooleanExtra("isFromWeatherActivity",false)){
            Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            finish();
        }
    }
}
