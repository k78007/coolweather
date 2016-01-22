package com.example.lenovo.coolweather.util;

/**
 * Created by lenovo on 2016/1/19.
 */
public interface HttpCallBackInterface {
    public abstract void onFinish(String data);
    public abstract void onError(Exception e);
}
