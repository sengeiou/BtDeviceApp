package com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess;

import com.cmtech.android.bledevice.ecgmonitor.model.EcgMonitorDevice;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.calibrator.EcgCalibrator65536;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.calibrator.IEcgCalibrator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.filter.EcgPreFilterWith35HzNotch;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.filter.IEcgFilter;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor.HrAbnormalProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor.HrStatisticProcessor;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.hrprocessor.IHrProcessor;
import com.cmtech.msp.qrsdetbyhamilton.QrsDetector;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.cmtech.android.bledevice.ecgmonitor.model.ecgdataprocess.ecgsignalprocess.calibrator.IEcgCalibrator.STANDARD_VALUE_1MV_AFTER_CALIBRATION;


/**
  *
  * ClassName:      EcgSignalProcessor
  * Description:    心电信号处理器，包含心电信号的标定，预滤波，基于QRS波检测的心率计算，以及心率记录和统计分析，心率异常报警
  * Author:         chenm
  * CreateDate:     2018-12-23 08:00
  * UpdateUser:     chenm
  * UpdateDate:     2019-06-15 08:00
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class EcgSignalProcessor {
    public static final short INVALID_HR = 0; // 无效心率值
    public static final int HR_FILTER_SECOND = 10;
    public static final int HR_HISTOGRAM_BAR_NUM = 5;
    private static final String HR_ABNORMAL_PROCESSOR_KEY = "hr_abnormal_processor"; // 心率异常处理器的String key
    private static final String HR_STATISTICS_PROCESSOR_KEY = "hr_statistics_processor"; // 心率统计处理器的String key

    private final EcgMonitorDevice device; // 设备
    private final IEcgCalibrator ecgCalibrator; // 标定处理器
    private final IEcgFilter ecgFilter; // 滤波器
    private QrsDetector qrsDetector; // QRS波检测器，可求心率值
    private final Map<String, IHrProcessor> hrProcessorMap; // 心率处理相关操作Map

    public EcgSignalProcessor(EcgMonitorDevice device) {
        if(device == null) {
            throw new IllegalArgumentException("EcgMonitorDevice is null");
        }

        this.device = device;
        ecgCalibrator = new EcgCalibrator65536(device.getValue1mV());
        ecgFilter = new EcgPreFilterWith35HzNotch(device.getSampleRate());
        qrsDetector = new QrsDetector(device.getSampleRate(), STANDARD_VALUE_1MV_AFTER_CALIBRATION);
        hrProcessorMap = new ConcurrentHashMap<>();
        HrStatisticProcessor hrStatisticProcessor = new HrStatisticProcessor(HR_FILTER_SECOND, device);
        hrProcessorMap.put(HR_STATISTICS_PROCESSOR_KEY, hrStatisticProcessor);
        if(device.getConfig().isWarnWhenHrAbnormal()) {
            HrAbnormalProcessor hrAbnormalProcessor = new HrAbnormalProcessor(device);
            hrProcessorMap.put(HR_ABNORMAL_PROCESSOR_KEY, hrAbnormalProcessor);
        }
    }

    // 重置，不会重置心率统计处理器
    public void reset() {
        ecgCalibrator.reset(device.getValue1mV(), STANDARD_VALUE_1MV_AFTER_CALIBRATION);
        ecgFilter.reset(device.getSampleRate());
        qrsDetector = new QrsDetector(device.getSampleRate(), STANDARD_VALUE_1MV_AFTER_CALIBRATION);
        resetHrAbnormalProcessor();
    }

    public void resetHrAbnormalProcessor() {
        HrAbnormalProcessor hrAbnormalProcessor = (HrAbnormalProcessor) hrProcessorMap.get(HR_ABNORMAL_PROCESSOR_KEY);

        if (device.getConfig().isWarnWhenHrAbnormal()) {
            if (hrAbnormalProcessor != null) {
                hrAbnormalProcessor.reset();
            } else {
                hrAbnormalProcessor = new HrAbnormalProcessor(device);
                hrProcessorMap.put(HR_ABNORMAL_PROCESSOR_KEY, hrAbnormalProcessor);
            }
        } else {
            hrProcessorMap.remove(HR_ABNORMAL_PROCESSOR_KEY);
        }
    }

    // 重置心率统计处理器，当设备关闭时调用
    public void resetHrStatisticProcessor() {
        HrStatisticProcessor hrStatisticProcessor = (HrStatisticProcessor) hrProcessorMap.get(HR_STATISTICS_PROCESSOR_KEY);

        if(hrStatisticProcessor != null) {
            hrStatisticProcessor.reset();
        }
    }

    // 处理Ecg信号
    public void process(int ecgSignal) {
        ecgSignal = (int) ecgFilter.filter(ecgCalibrator.calibrate(ecgSignal)); // 标定,滤波
        device.updateSignalValue(ecgSignal); // 更新信号
        short currentHr = (short) qrsDetector.outputHR(ecgSignal); // 检测Qrs波，获取心率
        // 心率值更新处理
        if(currentHr != INVALID_HR) {
            device.updateHrValue(currentHr);

            for (IHrProcessor operator : hrProcessorMap.values()) {
                operator.process(currentHr);
            }
        }
    }

    public void updateHrStatisticInfo() {
        HrStatisticProcessor hrStatisticProcessor = (HrStatisticProcessor) hrProcessorMap.get(HR_STATISTICS_PROCESSOR_KEY);

        if(hrStatisticProcessor != null) {
            hrStatisticProcessor.updateHrStatisticInfo();
        }
    }

    // 获取心率值列表
    public List<Short> getHrList() {
        HrStatisticProcessor hrStatisticProcessor = (HrStatisticProcessor) hrProcessorMap.get(HR_STATISTICS_PROCESSOR_KEY);
        if(hrStatisticProcessor != null) {
            return hrStatisticProcessor.getHrList();
        }
        return null;
    }
}
