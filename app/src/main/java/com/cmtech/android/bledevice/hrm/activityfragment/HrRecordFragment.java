package com.cmtech.android.bledevice.hrm.activityfragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.view.MyLineChart;
import com.cmtech.android.bledeviceapp.R;

import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledevice.hrm.model.HrmDevice.INVALID_HEART_RATE;
import static com.cmtech.android.bledeviceapp.data.record.BleHrRecord.HR_MA_FILTER_SPAN;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.ecgmonitor.activity
 * ClassName:      HrSequenceFragment
 * Description:    Ecg心率统计Fragment
 * Author:         chenm
 * CreateDate:     2019/4/15 上午5:40
 * UpdateUser:     更新者
 * UpdateDate:     2019/4/15 上午5:40
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class HrRecordFragment extends Fragment {
    public static final int TITLE_ID = R.string.hr_record;
    private TextView tvHrAve; // average heart rate value
    private TextView tvHrMax; // max heart rate value
    private MyLineChart lineChart; // heart rate line chart
    private ImageButton ibStart, ibStop;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_record_hrm_hr, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvHrMax = view.findViewById(R.id.tv_hr_max_value);
        tvHrAve = view.findViewById(R.id.tv_hr_ave_value);
        lineChart = view.findViewById(R.id.line_chart);
        lineChart.setXAxisValueFormatter(HR_MA_FILTER_SPAN);
        updateHrInfo(new ArrayList<Short>(), INVALID_HEART_RATE, INVALID_HEART_RATE);

        TextView tvYUnit = view.findViewById(R.id.line_chart_y_unit);
        tvYUnit.setText(R.string.BPM);

        ibStart = view.findViewById(R.id.ib_record_start);
        ibStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert getParentFragment() != null;
                ((HrmFragment)getParentFragment()).setHrRecord(true);
            }
        });
        ibStop = view.findViewById(R.id.ib_record_stop);
        ibStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert getParentFragment() != null;
                ((HrmFragment)getParentFragment()).setHrRecord(false);
            }
        });
    }

    public void updateHrInfo(List<Short> hrList, short hrMax, short hrAve) {
        if(hrMax <= 0)
            tvHrMax.setText(R.string.ellipsis);
        else
            tvHrMax.setText(String.valueOf(hrMax));
        if(hrAve <= 0)
            tvHrAve.setText(R.string.ellipsis);
        else
            tvHrAve.setText(String.valueOf(hrAve));
        lineChart.showShortLineChart(hrList, getResources().getString(R.string.hr_linechart), Color.BLUE);
    }

    public void updateHrRecordStatus(boolean isRecord) {
        if(isRecord) {
            ibStart.setVisibility(View.INVISIBLE);
            ibStop.setVisibility(View.VISIBLE);
        } else {
            ibStart.setVisibility(View.VISIBLE);
            ibStop.setVisibility(View.INVISIBLE);
        }
    }
}
