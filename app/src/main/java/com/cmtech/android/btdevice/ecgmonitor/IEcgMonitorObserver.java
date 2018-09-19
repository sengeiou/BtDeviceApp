package com.cmtech.android.btdevice.ecgmonitor;

import com.cmtech.android.btdevice.ecgmonitor.ecgmonitorstate.IEcgMonitorState;

public interface IEcgMonitorObserver {
    void updateState(IEcgMonitorState state);
    void updateSampleRate(int sampleRate);
    void updateLeadType(EcgLeadType leadType);
    void updateCalibrationValue(int calibrationValue);
    void updateEcgView(int xRes, float yRes, int viewGridWidth);
    void updateRecordCheckBox(boolean isCheck, boolean clickable);
    void updateFilterCheckBox(boolean isCheck, boolean clickable);
    void updateEcgData(int ecgData);
}