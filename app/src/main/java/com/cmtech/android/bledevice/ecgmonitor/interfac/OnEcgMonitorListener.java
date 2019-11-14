package com.cmtech.android.bledevice.ecgmonitor.interfac;

import com.cmtech.android.bledevice.ecgmonitor.enumeration.EcgLeadType;
import com.cmtech.android.bledevice.ecgmonitor.enumeration.EcgMonitorState;
import com.cmtech.android.bledevice.ecgmonitor.process.hr.HrStatisticsInfo;

public interface OnEcgMonitorListener {
    void onStateUpdated(EcgMonitorState state); // 状态更新
    void onSampleRateUpdated(int sampleRate); // 采样率更新
    void onLeadTypeUpdated(EcgLeadType leadType); // 导联类型更新
    void onValue1mVUpdated(int value1mV, int value1mVAfterCalibration);  // 1mV值更新
    void onRecordStateUpdated(boolean isRecord); // 记录状态更新
    void onShowSetupUpdated(int sampleRate, int value1mV, double zeroLocation); // 信号显示设置更新
    void onEcgSignalUpdated(int ecgSignal); // 信号显示
    void onEcgSignalShowStarted(int sampleRate); // 信号显示启动
    void onEcgSignalShowStopped(); // 信号显示停止
    void onRecordSecondUpdated(int second); // 信号记录秒数更新
    void onHrUpdated(int hr); // 心率值更新，单位bpm
    void onHrStaticsInfoUpdated(HrStatisticsInfo hrStaticsInfoAnalyzer); // 心率统计信息更新
    void onHrAbnormalNotified(); // 心率值异常通知
    void onBatteryUpdated(int bat); // 电池电量更新
}
