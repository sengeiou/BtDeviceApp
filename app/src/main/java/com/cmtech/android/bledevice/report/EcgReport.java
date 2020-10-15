package com.cmtech.android.bledevice.report;

import android.support.annotation.NonNull;

import com.cmtech.android.bledeviceapp.interfac.IJsonable;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.LitePalSupport;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EcgReport extends LitePalSupport implements IJsonable {
    public static final String DEFAULT_VER = "1.0";
    public static final long INVALID_TIME = -1;
    public static final String DEFAULT_REPORT_CONTENT = "无";
    public static final int DONE = 0;
    public static final int REQUEST = 1;
    public static final int PROCESS = 2;

    private int id;
    private String ver = DEFAULT_VER;
    private long reportTime = INVALID_TIME;
    private String content = DEFAULT_REPORT_CONTENT;
    private int status = DONE;

    public EcgReport() {
    }

    public int getId() {
        return id;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public long getReportTime() {
        return reportTime;
    }

    public void setReportTime(long reportTime) {
        this.reportTime = reportTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException{
        //ver = json.getString("ver");
        reportTime = json.getLong("reportTime");
        content = json.getString("content");
        status = json.getInt("status");
    }

    @Override
    public JSONObject toJson() throws JSONException{
        JSONObject json = new JSONObject();
        json.put("ver", ver);
        json.put("reportTime", reportTime);
        json.put("content", content);
        json.put("status", status);
        return json;
    }

    @NonNull
    @Override
    public String toString() {
        DateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String statusStr = "未知";
        switch (status) {
            case DONE:
                statusStr = "已处理";
                break;
            case REQUEST:
                statusStr = "等待处理";
                break;
            case PROCESS:
                statusStr = "正在处理";
                break;
            default:
                break;
        }
        return "时间：" + dateFmt.format(new Date(reportTime))
                + "\n内容：" + content
                + "\n状态：" + statusStr;
    }
}
