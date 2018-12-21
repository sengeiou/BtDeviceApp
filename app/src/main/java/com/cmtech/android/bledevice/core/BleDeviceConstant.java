package com.cmtech.android.bledevice.core;

import com.cmtech.android.bledeviceapp.MyApplication;

import java.io.File;

/**
 * BleDeviceConstant: 设备相关的常数
 * Created by bme on 2018/3/1.
 */

public class BleDeviceConstant {

    public static final String MY_BASE_UUID = "0a20XXXX-cce5-4025-a156-38ea833f6ef8"; // 基础UUID
    public static final String BT_BASE_UUID = "0000XXXX-0000-1000-8000-00805F9B34FB"; // 蓝牙标准UUID
    public static final String CCCUUID = "00002902-0000-1000-8000-00805f9b34fb"; // CCC UUID

    // 下面四个参数是ViseBle包中需要设置的
    public final static int SCAN_TIMEOUT = 12000; // 扫描超时时间
    public final static int CONNECT_TIMEOUT = 35000; // 连接超时时间
    public final static int CONNECT_RETRY_COUNT = 0; // 重连次数，不要改
    public final static int OPDATA_RETRY_COUNT = 0; // 数据操作重试次数，不要改


    public static final String SCAN_DEVICE_NAME = "CM1.0"; // 扫描时过滤设备：只获取广播数据包中设备名为该名称的设备
    public static final File IMAGEDIR = MyApplication.getContext().getExternalFilesDir("images"); // 图像文件DIR
    public static final File CACHEDIR = MyApplication.getContext().getExternalCacheDir(); // 文件缓存cache目录

    // 下面五个参数用于登记设备基本信息
    public static final String DEFAULT_DEVICE_NICKNAME = ""; // 缺省设备名
    public static final String DEFAULT_DEVICE_IMAGEPATH = ""; // 缺省设备图标路径名
    public static final boolean DEFAULT_DEVICE_AUTOCONNECT = true; // 设备打开时是否自动连接
    public static final int DEFAULT_DEVICE_RECONNECT_TIMES = 3; // 连接错误后的重连次数
    public static final boolean DEFAULT_WARN_AFTER_RECONNECT_FAILURE = true; // 重连失败后是否报警

    // 重连时间间隔，注意：反复多次重连会导致系统禁用蓝牙，Android规定30秒内不能超过5次
    public final static int RECONNECT_INTERVAL = 8000;

}