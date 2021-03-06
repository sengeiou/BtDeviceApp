package com.cmtech.android.bledeviceapp.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.activity.MainActivity;
import com.cmtech.android.bledeviceapp.adapter.WebDeviceAdapter;
import com.cmtech.android.bledeviceapp.global.MyApplication;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledeviceapp.activity
 * ClassName:      WebDevicesFragment
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2019-11-26 02:38
 * UpdateUser:     更新者
 * UpdateDate:     2019-11-26 02:38
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class WebDevicesFragment extends Fragment {
    private static final int MSG_UPDATE_WEB_DEVICES = 0;
    private WebDeviceAdapter webDeviceAdapter;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_WEB_DEVICES:
                    update();
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_device_web, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化已注册设备列表
        RecyclerView rvDevices = view.findViewById(R.id.rv_local_device);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvDevices.setLayoutManager(layoutManager);
        if(getContext() != null)
            rvDevices.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        webDeviceAdapter = new WebDeviceAdapter((MainActivity) getActivity());
        rvDevices.setAdapter(webDeviceAdapter);

        Button btnUpdate = view.findViewById(R.id.bt_update_web_devices);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateWebDeviceList();
            }
        });

        updateWebDeviceList();
    }

    private void updateWebDeviceList() {
        MyApplication.getDeviceManager().updateWebDevices(getContext());
        //DeviceManager.addCommonListenerForAllDevices(((MainActivity)getActivity()).getNotificationService());
        handler.sendEmptyMessage(MSG_UPDATE_WEB_DEVICES);
    }

    public void update() {
        webDeviceAdapter.update();
    }
}
