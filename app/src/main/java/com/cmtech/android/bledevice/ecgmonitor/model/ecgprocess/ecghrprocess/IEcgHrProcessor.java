package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecghrprocess;

/**
 * IEcgHrProcessor: 心率处理器接口
 * Created by Chenm, 2018-12-07
 */

public interface IEcgHrProcessor{
    void process(int hr); // 处理心率值
}
