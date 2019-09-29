package com.cmtech.android.bledevice.thermo.model;

import android.content.Context;

import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.cmtech.android.bledeviceapp.model.BleDeviceFactory;
import com.cmtech.android.ble.core.BleDevice;
import com.cmtech.android.bledeviceapp.activity.BleDeviceFragment;
import com.cmtech.android.ble.core.BleDeviceType;
import com.cmtech.android.bledevice.thermo.view.ThermoFragment;
import com.cmtech.android.bledeviceapp.R;

public class ThermoDeviceFactory extends BleDeviceFactory {
    private static final String THERMOMETER_UUID = "aa30"; // 体温计
    private static final String THERMOMETER_NAME = "体温计";
    private static final int THERMOMETER_IMAGE = R.drawable.ic_thermo_defaultimage;
    private static final String THERMOMETER_FACTORY = "com.cmtech.android.bledevice.thermo.model.ThermoDeviceFactory";

    public static final BleDeviceType THERMO_DEVICE_TYPE = new BleDeviceType(THERMOMETER_UUID, THERMOMETER_IMAGE, THERMOMETER_NAME, THERMOMETER_FACTORY);

    private ThermoDeviceFactory(BleDeviceRegisterInfo registerInfo) {
        super(registerInfo);
    }

    @Override
    public BleDevice createDevice(Context context) {
        return new ThermoDevice(context, registerInfo);
    }

    @Override
    public BleDeviceFragment createFragment() {
        return BleDeviceFragment.create(registerInfo.getMacAddress(), ThermoFragment.class);
    }
}
