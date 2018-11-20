package com.cmtech.android.bledevice.ecgmonitor.model.state;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice;
import com.vise.log.ViseLog;

public class EcgMonitorCalibratedState implements IEcgMonitorState {
    private EcgMonitorDevice device;

    public EcgMonitorCalibratedState(EcgMonitorDevice device) {
        this.device = device;
    }

    @Override
    public void start() {
        device.stopSampleData();
        device.startSampleEcg();
    }

    @Override
    public void stop() {
        ViseLog.i("ecgmonitor state action wrong");
    }

    @Override
    public void switchState() {
        start();
    }

    @Override
    public void onCalibrateSuccess() {
        ViseLog.i("ecgmonitor state action wrong");
    }

    @Override
    public void onCalibrateFailure() {
        ViseLog.i("ecgmonitor state action wrong");
    }

    @Override
    public void onProcessData(byte[] data) {
        ViseLog.i("ecgmonitor state action wrong");
    }

    @Override
    public boolean canStart() {
        return true;
    }

    @Override
    public boolean canStop() {
        return false;
    }
}