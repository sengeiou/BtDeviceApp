package com.cmtech.android.bledevice.ecg.process.signal.filter;

import com.cmtech.dsp.filter.IDigitalFilter;
import com.cmtech.dsp.filter.design.DCBlockDesigner;
import com.cmtech.dsp.filter.design.NotchDesigner;
import com.cmtech.dsp.filter.structure.StructType;


/**
  *
  * ClassName:      EcgPreFilter
  * Description:    心电信号预滤波器，包含一个基线漂移滤除的隔直滤波器和一个工频干扰滤除的陷波器
  * Author:         chenm
  * CreateDate:     2018-12-06 07:38
  * UpdateUser:     chenm
  * UpdateDate:     2019-07-03 07:38
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class EcgPreFilter implements IEcgFilter {
    protected static final double NOTCH_BANDWIDTH_3DB = 0.5; // 陷波器的3dB带宽
    private static final double DEFAULT_BASELINE_FREQ = 0.5; // 缺省基线漂移滤波器截止频率
    private static final int DEFAULT_POWERLINE_FREQ = 50; // 缺省工频

    private final double baselineFreq; // 基线漂移截止频率
    private final int powerlineFreq; // 工频
    private IDigitalFilter dcBlock;
    private IDigitalFilter notch50Hz;

    public EcgPreFilter(int sampleRate) {
        this(sampleRate, DEFAULT_BASELINE_FREQ, DEFAULT_POWERLINE_FREQ);
    }

    public EcgPreFilter(int sampleRate, double baselineFreq, int powerlineFreq) {
        this.baselineFreq = baselineFreq;
        this.powerlineFreq = powerlineFreq;

        // 准备0.5Hz基线漂移滤波器
        dcBlock = DCBlockDesigner.design(baselineFreq, sampleRate); // 设计隔直滤波器
        dcBlock.createStructure(StructType.IIR_DCBLOCK); // 创建隔直滤波器专用结构

        // 准备50Hz陷波器
        notch50Hz = NotchDesigner.design(powerlineFreq, NOTCH_BANDWIDTH_3DB, sampleRate);  // 设计陷波器
        notch50Hz.createStructure(StructType.IIR_NOTCH); // 创建陷波器专用结构
    }

    @Override
    public void reset(int sampleRate) {
        // 准备0.5Hz基线漂移滤波器
        dcBlock = DCBlockDesigner.design(baselineFreq, sampleRate); // 设计隔直滤波器
        dcBlock.createStructure(StructType.IIR_DCBLOCK); // 创建隔直滤波器专用结构

        // 准备50Hz陷波器
        notch50Hz = NotchDesigner.design(powerlineFreq, NOTCH_BANDWIDTH_3DB, sampleRate);  // 设计陷波器
        notch50Hz.createStructure(StructType.IIR_NOTCH); // 创建陷波器专用结构
    }

    @Override
    public double filter(double ecgSignal) {
        return notch50Hz.filter(dcBlock.filter(ecgSignal));
    }

}
