package com.cmtech.android.bledevice.ecgmonitor.model.ecgprocess.ecgcalibrator;

/**
 * EcgCalibrator65536: 心电信号定标器，定标后的基准值为65536，可以利用这一点简化运算
 * Created by bme on 2018/12/06.
 */

public class EcgCalibrator65536 extends EcgCalibrator {

    public EcgCalibrator65536(int calibrateValueBefore) {
        super(calibrateValueBefore);
    }

    // 乘以65536可以用右移16位实现
    @Override
    public int process(int data) {
        return (data<<16)/calibrateValueBefore;
    }
}