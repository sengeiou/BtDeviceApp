package com.cmtech.android.bledeviceapp.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.cmtech.android.bledevice.hrm.model.BleEcgRecord10;
import com.cmtech.android.bledevice.hrm.model.BleHrRecord10;
import com.cmtech.android.bledevice.hrm.model.RecordWebAsyncTask;
import com.cmtech.android.bledevice.thermo.model.BleThermoRecord10;
import com.cmtech.android.bledevice.thermo.view.ThermoRecordActivity;
import com.cmtech.android.bledevice.thm.model.BleTempHumidRecord10;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.adapter.RecordListAdapter;
import com.cmtech.android.bledevice.hrm.view.EcgRecordActivity;
import com.cmtech.android.bledevice.hrm.view.HrRecordActivity;
import com.cmtech.android.bledevice.interf.IRecord;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.KMWebService;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
  *
  * ClassName:      HrRecordExplorerActivity
  * Description:    Ecg记录浏览Activity
  * Author:         chenm
  * CreateDate:     2018/11/10 下午5:34
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/12 下午5:34
  * UpdateRemark:   制作类图，优化代码
  * Version:        1.0
 */

public class RecordExplorerActivity extends AppCompatActivity {
    private static final String TAG = "RecordExplorerActivity";

    private List<IRecord> allRecords = new ArrayList<>(); // all records
    private RecordListAdapter adapter; // Adapter
    private RecyclerView view; // RecycleView
    private TextView tvPromptInfo; // prompt info

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_explorer);

        // 创建ToolBar
        Toolbar toolbar = findViewById(R.id.tb_record_explorer);
        setSupportActionBar(toolbar);

        view = findViewById(R.id.rv_record_list);
        LinearLayoutManager fileLayoutManager = new LinearLayoutManager(this);
        fileLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        view.setLayoutManager(fileLayoutManager);
        view.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter = new RecordListAdapter(this, allRecords);
        view.setAdapter(adapter);

        tvPromptInfo = findViewById(R.id.tv_prompt_info);
        tvPromptInfo.setText("无记录");

        List<BleHrRecord10> hrRecords = LitePal.select("createTime, devAddress, creatorPlat, creatorId, recordSecond").find(BleHrRecord10.class);
        allRecords.addAll(hrRecords);
        List<BleEcgRecord10> ecgRecords = LitePal.select("createTime, devAddress, creatorPlat, creatorId, recordSecond").find(BleEcgRecord10.class);
        allRecords.addAll(ecgRecords);
        List<BleThermoRecord10> thermoRecords = LitePal.select("createTime, devAddress, creatorPlat, creatorId, highestTemp").find(BleThermoRecord10.class);
        allRecords.addAll(thermoRecords);
        List<BleTempHumidRecord10> thmRecords = LitePal.select("createTime, devAddress, creatorPlat, creatorId, temperature, humid, heatIndex, location").find(BleTempHumidRecord10.class);
        allRecords.addAll(thmRecords);
        Collections.sort(allRecords, new Comparator<IRecord>() {
            @Override
            public int compare(IRecord o1, IRecord o2) {
                long time1 = o1.getCreateTime();
                long time2 = o2.getCreateTime();
                if(time1 == time2) return 0;
                return (time2 > time1) ? 1 : -1;
            }
        });
        updateRecordList();
    }

    public void selectRecord(final IRecord record) {
        Intent intent = null;
        if(record instanceof BleHrRecord10) {
            intent = new Intent(this, HrRecordActivity.class);
        } else if(record instanceof BleEcgRecord10) {
            intent = new Intent(this, EcgRecordActivity.class);
        } else if(record instanceof BleThermoRecord10) {
            intent = new Intent(this, ThermoRecordActivity.class);
        }
        if(intent != null) {
            intent.putExtra("record_id", record.getId());
            startActivity(intent);
        }
    }

    public void deleteRecord(final IRecord record) {
        if(record != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("删除记录").setMessage("确定删除该记录吗？");

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(allRecords.remove(record)) {
                        updateRecordList();
                    }
                    if(record instanceof BleHrRecord10) {
                        LitePal.delete(BleHrRecord10.class, record.getId());
                    } else if(record instanceof BleEcgRecord10) {
                        LitePal.delete(BleEcgRecord10.class, record.getId());
                        new RecordWebAsyncTask(RecordExplorerActivity.this, RecordWebAsyncTask.RECORD_DELETE_CMD, new RecordWebAsyncTask.RecordWebCallback() {
                            @Override
                            public void onFinish(Object[] objs) {
                                MyApplication.showMessageUsingShortToast((Integer)objs[0]+(String)objs[1]);
                            }
                        }).execute((BleEcgRecord10)record);
                    } else if(record instanceof BleThermoRecord10) {
                        LitePal.delete(BleThermoRecord10.class, record.getId());
                    } else if(record instanceof BleTempHumidRecord10) {
                        LitePal.delete(BleTempHumidRecord10.class, record.getId());
                    }
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).show();
        }
    }

    private void updateRecordList() {
        if(allRecords.isEmpty()) {
            view.setVisibility(View.INVISIBLE);
            tvPromptInfo.setVisibility(View.VISIBLE);
        }else {
            view.setVisibility(View.VISIBLE);
            tvPromptInfo.setVisibility(View.INVISIBLE);
        }
        adapter.updateRecordList();
    }
}
