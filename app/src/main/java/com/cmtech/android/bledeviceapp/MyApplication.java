package com.cmtech.android.bledeviceapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.cmtech.android.ble.ViseBle;
import com.cmtech.android.bledevice.core.BleDeviceConfig;
import com.mob.MobSDK;
import com.vise.log.ViseLog;
import com.vise.log.inner.LogcatTree;

import org.litepal.LitePal;

import static com.cmtech.android.bledevice.core.BleDeviceConstant.CONNECT_TIMEOUT;
import static com.cmtech.android.bledevice.core.BleDeviceConstant.SCAN_TIMEOUT;

/**
 * MyApplication
 * Created by bme on 2018/2/19.
 */

public class MyApplication extends Application {
    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        // ViseBle包的配置，不要修改
        BleDeviceConfig.setScanTimeout(SCAN_TIMEOUT);

        BleDeviceConfig.setConnectTimeout(CONNECT_TIMEOUT);

        BleDeviceConfig.setConnectRetryCount(0);

        BleDeviceConfig.setOpDataRetryCount(0);

        Context context = getApplicationContext();

        ViseBle.getInstance().init(context);

        // 初始化LitePal
        LitePal.initialize(context);

        LitePal.getDatabase();

        // 初始化MobSDK
        MobSDK.init(context, "2865551f849a2", "4e4d54b3cba5472505b5f251419ba502");

        // 初始化ViseLog
        ViseLog.getLogConfig()
                .configAllowLog(true)           //是否输出日志
                .configShowBorders(false)        //是否排版显示
                .configTagPrefix("BleDeviceApp")     //设置标签前缀
                //.configFormatTag("%d{HH:mm:ss:SSS} %t %c{-5}")//个性化设置标签，默认显示包名
                .configLevel(Log.VERBOSE);      //设置日志最小输出级别，默认Log.VERBOSE

        ViseLog.plant(new LogcatTree());        //添加打印日志信息到Logcat的树
    }

    // 获取Application Context
    public static Context getContext() {
        return instance.getApplicationContext();
    }
}
