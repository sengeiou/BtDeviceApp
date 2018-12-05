package com.cmtech.android.bledevice.ecgmonitor.model.ecgcalibrator;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgcalibrator.IEcgCalibrator;

/**
 * EcgCalibrator: 心电信号定标器，用于定标（归一化）信号值
 * Created by bme on 2018/12/06.
 */

public class EcgCalibrator implements IEcgCalibrator {
    private final static int DEFAULT_CALIBRATEVALUEAFTER = 65536;

    protected int calibrateValueBefore;
    private int calibrateValueAfter = DEFAULT_CALIBRATEVALUEAFTER;

    public EcgCalibrator(int calibrateValueBefore, int calibrateValueAfter) {
        this.calibrateValueBefore = calibrateValueBefore;
        this.calibrateValueAfter = calibrateValueAfter;
    }

    public EcgCalibrator(int calibrateValueBefore) {
        this(calibrateValueBefore, DEFAULT_CALIBRATEVALUEAFTER);
    }

    @Override
    public int process(int data) {
        return data*calibrateValueAfter/calibrateValueBefore;
    }
}
