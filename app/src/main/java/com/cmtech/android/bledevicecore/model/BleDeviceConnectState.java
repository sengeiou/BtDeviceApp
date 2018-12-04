package com.cmtech.android.bledevicecore.model;

import com.cmtech.android.bledeviceapp.R;

/**
 * BleDeviceConnectState: BleDevice连接状态类型，在BLE包基础上增加了扫描和关闭两个状态
 * Created by bme on 2018/4/21.
 */

public enum BleDeviceConnectState {
    CONNECT_INIT(-1, "连接初始化", true, true, R.mipmap.ic_connect_disconnect),
    CONNECT_PROCESS(0x00, "连接中", false, false, R.drawable.connectingdrawable),
    CONNECT_SUCCESS(0x01, "连接成功", true, true, R.mipmap.ic_connect_connected),
    CONNECT_FAILURE(0x02, "连接失败", true, true, R.mipmap.ic_connect_disconnect),
    CONNECT_TIMEOUT(0x03, "连接超时", true, true, R.mipmap.ic_connect_disconnect),
    CONNECT_DISCONNECT(0x04, "连接断开", true, true, R.mipmap.ic_connect_disconnect),

    CONNECT_SCAN(0x05, "扫描中", false, false, R.drawable.connectingdrawable),
    CONNECT_CLOSED(0x06, "连接关闭", true, true, R.mipmap.ic_connect_disconnect);

    BleDeviceConnectState(int code, String description, boolean enableSwitch, boolean enableClose, int icon) {
        this.code = code;
        this.description = description;
        this.enableSwitch = enableSwitch;
        this.enableClose = enableClose;
        this.icon = icon;
    }

    private int code;
    private String description;
    boolean enableSwitch;
    boolean enableClose;
    int icon;

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnableSwitch() {
        return enableSwitch;
    }

    public boolean isEnableClose() {
        return enableClose;
    }

    public int getIcon() {
        return icon;
    }

    public static String getDescriptionFromCode(int code) {
        for(BleDeviceConnectState ele : BleDeviceConnectState.values()) {
            if(ele.code == code) {
                return ele.description;
            }
        }
        return "";
    }

    public static BleDeviceConnectState getFromCode(int code) {
        for(BleDeviceConnectState ele : BleDeviceConnectState.values()) {
            if(ele.code == code) {
                return ele;
            }
        }
        return null;
    }
}