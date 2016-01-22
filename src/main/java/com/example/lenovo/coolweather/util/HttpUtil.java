package com.example.lenovo.coolweather.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lenovo on 2016/1/19.
 */
public class HttpUtil {
    public static void sendHttpRequest(final String address, final HttpCallBackInterface httpCallBackInterface){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection httpURLConnection=null;
                try {
                    URL url=new URL(address);
                    httpURLConnection=(HttpURLConnection)url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setConnectTimeout(8000);
                    httpURLConnection.setReadTimeout(8000);
                    InputStream inputStream=httpURLConnection.getInputStream();
                    BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));
                    String str=null;
                    StringBuilder stringBuilder = new StringBuilder();
                    while((str=br.readLine())!=null){
                        stringBuilder.append(str);
                    }
                    if(httpCallBackInterface!=null){
                        httpCallBackInterface.onFinish(stringBuilder.toString());
                    }

                } catch (IOException e) {
                    if(httpCallBackInterface!=null){
                        httpCallBackInterface.onError(e);
                    }
                    e.printStackTrace();
                }finally {
                    if(httpURLConnection!=null){
                        httpURLConnection.disconnect();
                    }
                }
            }
        }).start();
    }
}
