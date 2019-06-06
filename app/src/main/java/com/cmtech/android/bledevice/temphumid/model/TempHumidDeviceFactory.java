package com.cmtech.android.bledevice.temphumid.model;

import android.content.Context;

import com.cmtech.android.ble.extend.AbstractBleDeviceFactory;
import com.cmtech.android.ble.extend.BleDevice;
import com.cmtech.android.ble.extend.BleDeviceFragment;
import com.cmtech.android.ble.extend.BleDeviceType;
import com.cmtech.android.bledevice.temphumid.activity.TempHumidFragment;
import com.cmtech.android.bledeviceapp.R;

// 会根据设备类型BleDeviceType，通过反射创建工厂类实例
public class TempHumidDeviceFactory extends AbstractBleDeviceFactory {
    private static final String UUID_TEMPHUMID                  = "aa60";       // 温湿度计
    private static final String NAME_TEMPHUMID                   = "温湿度计";
    private static final int IMAGE_TEMPHUMID                   = R.drawable.ic_temphumid_defaultimage;
    private static final String tempHumidDeviceFactory = "com.cmtech.android.bledevice.temphumid.model.TempHumidDeviceFactory";

    public static void addDeviceType() {
        BleDeviceType deviceType = new BleDeviceType(UUID_TEMPHUMID, IMAGE_TEMPHUMID, NAME_TEMPHUMID, tempHumidDeviceFactory);
        BleDeviceType.addSupportedType(deviceType);
    }

    @Override
    public BleDevice createDevice(Context context) {
        return new TempHumidDevice(context, basicInfo);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return BleDeviceFragment.create(basicInfo.getMacAddress(), TempHumidFragment.class);
    }


}
