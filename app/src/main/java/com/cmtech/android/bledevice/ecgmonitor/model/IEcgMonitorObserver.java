package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.state.IEcgMonitorState;

public interface IEcgMonitorObserver {
    void updateState(IEcgMonitorState state);
    void updateSampleRate(int sampleRate);
    void updateLeadType(EcgLeadType leadType);
    void updateCalibrationValue(int calibrationValue);
    void updateRecordStatus(boolean isRecord);
    void updateEcgView(int xRes, float yRes, int viewGridWidth);
    void updateEcgData(int ecgData);
    void updateEcgHr(int hr);
}