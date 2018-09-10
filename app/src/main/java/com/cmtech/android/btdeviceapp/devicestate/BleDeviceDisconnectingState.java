package com.cmtech.android.btdeviceapp.devicestate;

import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.btdeviceapp.model.BleDevice;
import com.vise.log.ViseLog;

public class BleDeviceDisconnectingState implements IBleDeviceState {
    BleDevice device;

    public BleDeviceDisconnectingState(BleDevice device) {
        this.device = device;
    }

    @Override
    public void open() {
        ViseLog.i("action wrong");
    }

    @Override
    public void close() {
        ViseLog.i("action wrong");
    }

    @Override
    public void scan() {
        ViseLog.i("action wrong");
    }

    @Override
    public void disconnect() {
        ViseLog.i("action wrong");
    }

    @Override
    public void switchState() {
        ViseLog.i("action wrong");
    }

    @Override
    public void onDeviceScanSuccess() {
        ViseLog.i("callback wrong");
    }

    @Override
    public void onDeviceScanFailure() {
        ViseLog.i("callback wrong");
    }

    @Override
    public void onDeviceConnectSuccess(DeviceMirror mirror) {
        ViseLog.i("callback wrong");
    }

    @Override
    public void onDeviceConnectFailure() {
        ViseLog.i("callback wrong");
    }

    @Override
    public void onDeviceConnectTimeout() {
        ViseLog.i("callback wrong");
    }

    @Override
    public void onDeviceDisconnect() {
        device.getHandler().removeCallbacksAndMessages(null);
        device.processDisconnect();
        device.setState(device.getOpenState());
    }

    @Override
    public String getStateDescription() {
        return "断开中";
    }

    @Override
    public boolean canConnect() {
        return false;
    }

    @Override
    public boolean canDisconnect() {
        return false;
    }

    @Override
    public boolean canClose() {
        return false;
    }
}
