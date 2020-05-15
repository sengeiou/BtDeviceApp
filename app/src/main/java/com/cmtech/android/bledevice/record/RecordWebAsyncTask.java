package com.cmtech.android.bledevice.record;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.KMWebService;
import com.vise.log.ViseLog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.record
 * ClassName:      RecordWebAsyncTask
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/4/20 上午6:44
 * UpdateUser:     更新者
 * UpdateDate:     2020/4/20 上午6:44
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class RecordWebAsyncTask extends AsyncTask<IRecord, Void, Void> {
    public static final int RECORD_UPLOAD_CMD = 1;
    public static final int RECORD_UPDATE_NOTE_CMD = 2;
    public static final int RECORD_QUERY_CMD = 3;
    public static final int RECORD_DELETE_CMD = 4;
    public static final int RECORD_INFO_DOWNLOAD_CMD = 5;
    public static final int RECORD_DOWNLOAD_CMD = 6;

    public static final int DOWNLOAD_NUM_PER_TIME = 10;

    private ProgressDialog progressDialog;
    private final int cmd;
    private final boolean isShowProgress;

    private boolean finish = false;

    private int code;
    private String errStr;
    private Object rlt;

    public interface RecordWebCallback {
        void onFinish(Object[] objs);
    }

    private final RecordWebCallback callback;

    public RecordWebAsyncTask(Context context, int cmd, RecordWebCallback callback) {
        this(context, cmd, true, callback);
    }

    public RecordWebAsyncTask(Context context, int cmd, boolean isShowProgress, RecordWebCallback callback) {
        this.cmd = cmd;

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("请稍等...");
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        this.callback = callback;

        this.isShowProgress = isShowProgress;
    }

    @Override
    protected void onPreExecute() {
        if(isShowProgress)
            progressDialog.show();
    }

    @Override
    protected Void doInBackground(IRecord... records) {
        if(records == null) return null;

        IRecord record = records[0];

        switch (cmd) {
            // QUERY
            case RECORD_QUERY_CMD:
                KMWebService.queryRecord(record.getTypeCode(), record.getCreateTime(), record.getDevAddress(), new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        code = 1;
                        errStr = "网络错误";
                        rlt = null;
                        ViseLog.e(code+errStr);
                        finish = true;
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if(response.body() == null) return;
                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            int id = json.getInt("id");
                            ViseLog.e("find ecg record id = " + id);
                            code = 0;
                            errStr = "查询成功";
                            rlt = (Integer)id;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            code = 1;
                            errStr = "网络错误";
                            rlt = null;
                        } finally {
                            finish = true;
                        }
                    }
                });
                break;

            // UPLOAD
            case RECORD_UPLOAD_CMD:
                KMWebService.uploadRecord(AccountManager.getAccount().getPlatName(), AccountManager.getAccount().getPlatId(), record, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        code = 1;
                        errStr = "网络错误";
                        rlt = null;
                        ViseLog.e(code+errStr);
                        finish = true;
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String respBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(respBody);
                            code = json.getInt("code");
                            errStr = json.getString("errStr");
                            ViseLog.e(code+errStr);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            code = 1;
                            errStr = "网络错误";
                            rlt = null;
                        } finally {
                            finish = true;
                        }
                    }
                });
                break;

            // UPDATE NOTE
            case RECORD_UPDATE_NOTE_CMD:
                KMWebService.updateRecordNote(AccountManager.getAccount().getPlatName(), AccountManager.getAccount().getPlatId(), record, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        code = 1;
                        errStr = "网络错误";
                        rlt = null;
                        ViseLog.e(code+errStr);
                        finish = true;
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String respBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(respBody);
                            code = json.getInt("code");
                            errStr = json.getString("errStr");
                            ViseLog.e(code+errStr);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            code = 1;
                            errStr = "网络错误";
                            rlt = null;
                        } finally {
                            finish = true;
                        }
                    }
                });
                break;

            // DELETE
            case RECORD_DELETE_CMD:
                KMWebService.deleteRecord(AccountManager.getAccount().getPlatName(), AccountManager.getAccount().getPlatId(), record, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        code = 1;
                        errStr = "网络错误";
                        rlt = null;
                        ViseLog.e(code+errStr);
                        finish = true;
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String respBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(respBody);
                            code = json.getInt("code");
                            errStr = json.getString("errStr");
                            ViseLog.e(code+errStr);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            code = 1;
                            errStr = "网络错误";
                            rlt = null;
                        } finally {
                            finish = true;
                        }
                    }
                });
                break;

            // DOWNLOAD INFO
            case RECORD_INFO_DOWNLOAD_CMD:
                KMWebService.downloadRecordInfo(AccountManager.getAccount().getPlatName(), AccountManager.getAccount().getPlatId(), record, DOWNLOAD_NUM_PER_TIME, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        code = 1;
                        errStr = "网络错误";
                        rlt = null;
                        ViseLog.e(code+errStr);
                        finish = true;
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String respBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(respBody);
                            code = json.getInt("code");
                            errStr = json.getString("errStr");
                            rlt = json.get("records");
                            ViseLog.e(json.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            code = 1;
                            errStr = "网络错误";
                            rlt = null;
                        } finally {
                            finish = true;
                        }
                    }
                });
                break;

            // DOWNLOAD
            case RECORD_DOWNLOAD_CMD:
                KMWebService.downloadRecord(AccountManager.getAccount().getPlatName(), AccountManager.getAccount().getPlatId(), record, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        code = 1;
                        errStr = "网络错误";
                        rlt = null;
                        ViseLog.e(code+errStr);
                        finish = true;
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String respBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(respBody);
                            ViseLog.e(json.toString());
                            code = json.getInt("code");
                            errStr = json.getString("errStr");
                            rlt = json.get("record");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            code = 1;
                            errStr = "网络错误";
                            rlt = null;
                        } finally {
                            finish = true;
                        }
                    }
                });
                break;

            default:
                code = 1;
                errStr = "无效命令";
                rlt = null;
                finish = true;
                break;
        }

        while (!finish) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        callback.onFinish(new Object[]{code, errStr, rlt});
        progressDialog.dismiss();
    }
}