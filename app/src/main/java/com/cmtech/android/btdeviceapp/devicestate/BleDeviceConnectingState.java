package com.cmtech.android.btdeviceapp.devicestate;

import com.cmtech.android.ble.core.DeviceMirror;
import com.cmtech.android.btdeviceapp.model.BleDevice;
import com.vise.log.ViseLog;

public class BleDeviceConnectingState implements IBleDeviceState {
    BleDevice device;

    public BleDeviceConnectingState(BleDevice device) {
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
        device.processConnectSuccess(mirror);

        device.setState(device.getConnectedState());
    }

    @Override
    public void onDeviceConnectFailure() {
        device.processConnectFailure();

        device.setState(device.getOpenState());
    }

    @Override
    public void onDeviceConnectTimeout() {
        device.processConnectFailure();

        device.setState(device.getOpenState());
    }

    @Override
    public void onDeviceDisconnect() {
        ViseLog.i("callback wrong");
    }

    @Override
    public String getStateDescription() {
        return "连接中";
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
