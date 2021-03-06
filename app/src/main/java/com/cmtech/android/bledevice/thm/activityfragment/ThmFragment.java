package com.cmtech.android.bledevice.thm.activityfragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.bledevice.thm.model.BleTempHumidData;
import com.cmtech.android.bledevice.thm.model.OnThmListener;
import com.cmtech.android.bledevice.thm.model.ThmDevice;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.fragment.DeviceFragment;


/**
 * Created by bme on 2018/2/27.
 */

public class ThmFragment extends DeviceFragment implements OnThmListener {
    private TextView tvTempData;
    private TextView tvHumidData;
    private TextView tvHeadIndex;
    private EditText etInterval;
    private Button btnSetInterval;
    private ImageButton ibSave;
    private EditText etLoc;

    private ThmDevice device;

    public ThmFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        device = (ThmDevice) getDevice();

        return inflater.inflate(R.layout.fragment_device_thm, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTempData = view.findViewById(R.id.tv_temp_data);
        tvHumidData = view.findViewById(R.id.tv_humid_data);
        tvHeadIndex = view.findViewById(R.id.tv_heat_index);
        etInterval = view.findViewById(R.id.et_measure_interval);
        etInterval.setText(String.valueOf(device.getInterval()));
        btnSetInterval = view.findViewById(R.id.btn_set_interval);

        btnSetInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                short interval = Short.valueOf(etInterval.getText().toString());
                device.setInterval(interval);
            }
        });

        etLoc = view.findViewById(R.id.et_sens_loc);

        ibSave = view.findViewById(R.id.ib_record);
        ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(device != null)
                    device.save(etLoc.getText().toString());
            }
        });

        device.registerListener(this);

        // 打开设备
        device.open();
    }

    @Override
    public void openConfigureActivity() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(device != null)
            device.removeListener(this);
    }

    @Override
    public void onTempHumidDataUpdated(final BleTempHumidData tempHumidData) {
        if(tempHumidData != null && getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvHumidData.setText(String.valueOf((int)(Math.round(tempHumidData.getHumid()/100.0))));
                    tvTempData.setText(String.valueOf((int)(Math.round(tempHumidData.getTemp()/100.0))));
                    float heatindex = tempHumidData.calculateHeatIndex();
                    tvHeadIndex.setText(String.valueOf((int)heatindex));
                }
            });
        }
    }

}
