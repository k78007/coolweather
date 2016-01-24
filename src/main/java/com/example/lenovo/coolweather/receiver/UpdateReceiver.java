package com.example.lenovo.coolweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.lenovo.coolweather.service.AutoUpdateService;

/**
 * Created by lenovo on 2016/1/24.
 */
public class UpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context,AutoUpdateService.class);
        context.startService(intent1);
    }
}
