package com.cmtech.android.bledeviceapp;

import android.os.Environment;

import com.cmtech.android.ble.utils.UuidUtil;
import com.cmtech.android.bledevice.ecg.device.EcgFactory;
import com.cmtech.android.bledevice.ecg.webecg.WebEcgFactory;
import com.cmtech.android.bledevice.hrm.model.HrmFactory;
import com.cmtech.android.bledevice.thm.model.ThmFactory;
import com.cmtech.android.bledevice.thermo.model.ThermoFactory;
import com.cmtech.android.bledeviceapp.model.DeviceType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AppConstant: App constant
 * Created by bme on 2018/3/1.
 */

public class AppConstant {
    public static final String MY_BASE_UUID = "0a20XXXX-cce5-4025-a156-38ea833f6ef8"; // my base UUID
    public static final String STANDARD_BLE_UUID = "0000XXXX-0000-1000-8000-00805F9B34FB"; // standard BLE UUID
    public static final UUID CCC_UUID = UuidUtil.stringToUUUID("2902", STANDARD_BLE_UUID); // client characteristic config UUID
    public static final int RECONNECT_INTERVAL = 6000; // reconnect interval, unit: millisecond
    public static final int SCAN_DURATION = 20000; // scan duration, unit: millisecond
    public static final File DIR_IMAGE = MyApplication.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES); // image file directory
    public static final File DIR_CACHE = MyApplication.getContext().getExternalCacheDir(); // file cache directory
    public static final int SPLASH_ACTIVITY_COUNT_DOWN_SECOND = 3; // count down second in splash activity
    public static final String KM_STORE_URI = "https://decathlon.tmall.com/shop/view_shop.htm?spm=a21bo.2017.201863-1.d2.6dd211d9AzJgBt&user_number_id=352469034&pvid=067004f4-d493-413a-a4f7-003e62637549&pos=2&brandId=44506&acm=03014.1003.1.765824&scm=1007.13143.56636.100200300000000";
    public static final List<DeviceType> SUPPORT_DEVICE_TYPES = new ArrayList<DeviceType>(){
        {
            add(HrmFactory.HRM_DEVICE_TYPE);
            add(ThermoFactory.THERMO_DEVICE_TYPE);
            add(ThmFactory.THM_DEVICE_TYPE);
            add(EcgFactory.ECG_DEVICE_TYPE);
            add(WebEcgFactory.ECGWEBMONITOR_DEVICE_TYPE);
        }
    }; // supported device type
}
