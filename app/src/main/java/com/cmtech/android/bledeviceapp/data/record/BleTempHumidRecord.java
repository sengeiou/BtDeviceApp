package com.cmtech.android.bledeviceapp.data.record;

import com.cmtech.android.bledeviceapp.model.Account;

import org.json.JSONException;
import org.json.JSONObject;

import static com.cmtech.android.bledeviceapp.data.record.RecordType.TH;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.thm.model
 * ClassName:      BleTempHumidRecord10
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/4/4 下午3:11
 * UpdateUser:     更新者
 * UpdateDate:     2020/4/4 下午3:11
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class BleTempHumidRecord extends BasicRecord {
    private float temperature = 0.0f;
    private float humid = 0.0f;
    private float heatIndex = 0.0f;
    private String location = "室内";

    private BleTempHumidRecord(String ver, long createTime, String devAddress, Account creator) {
        super(TH, ver, createTime, devAddress, creator);
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException{
        super.fromJson(json);
    }

    @Override
    public JSONObject toJson() throws JSONException {
        return null;
    }

    @Override
    public boolean noSignal() {
        return true;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getHumid() {
        return humid;
    }

    public void setHumid(float humid) {
        this.humid = humid;
    }

    public float getHeatIndex() {
        return heatIndex;
    }

    public void setHeatIndex(float heatIndex) {
        this.heatIndex = heatIndex;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
