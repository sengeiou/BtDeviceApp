package com.cmtech.android.bledevice.ecg.device;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.cmtech.android.bledevice.ecg.EcgConstant.DEFAULT_HR_HIGH_LIMIT;
import static com.cmtech.android.bledevice.ecg.EcgConstant.DEFAULT_HR_LOW_LIMIT;
import static com.cmtech.android.bledevice.ecg.EcgConstant.DEFAULT_WARN_WHEN_HR_ABNORMAL;

/**
 * EcgConfiguration: 心电带配置类
 * Created by bme on 2018/12/20.
 */

public class EcgConfiguration extends LitePalSupport implements Serializable {
    private final static long serialVersionUID = 1L;

    private int id; // id
    private String macAddress = ""; // mac地址
    private boolean warnWhenHrAbnormal = DEFAULT_WARN_WHEN_HR_ABNORMAL; // hr异常时是否报警
    private int hrLowLimit = DEFAULT_HR_LOW_LIMIT; // hr异常的下限
    private int hrHighLimit = DEFAULT_HR_HIGH_LIMIT; // hr异常的上限
    private List<String> markerList = new ArrayList<>(Arrays.asList("标记1", "标记2", "标记3", "标记4")); //标记列表

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getMacAddress() {
        return macAddress;
    }
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
    public boolean warnWhenHrAbnormal() {
        return warnWhenHrAbnormal;
    }
    public void setWarnWhenHrAbnormal(boolean warnWhenHrAbnormal) {
        this.warnWhenHrAbnormal = warnWhenHrAbnormal;
    }
    public int getHrLowLimit() {
        return hrLowLimit;
    }
    public void setHrLowLimit(int hrLowLimit) {
        this.hrLowLimit = hrLowLimit;
    }
    public int getHrHighLimit() {
        return hrHighLimit;
    }
    public void setHrHighLimit(int hrHighLimit) {
        this.hrHighLimit = hrHighLimit;
    }
    public List<String> getMarkerList() {
        return markerList;
    }
    public void setMarkerList(List<String> markerList) {
        Collections.copy(this.markerList, markerList);
    }

    public void copyFrom(EcgConfiguration config) {
        warnWhenHrAbnormal = config.warnWhenHrAbnormal;
        hrLowLimit = config.hrLowLimit;
        hrHighLimit = config.hrHighLimit;
        setMarkerList(config.getMarkerList());
    }
}
