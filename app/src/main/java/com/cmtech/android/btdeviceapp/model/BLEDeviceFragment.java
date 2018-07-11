package com.cmtech.android.btdeviceapp.model;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cmtech.android.btdeviceapp.R;
import com.cmtech.android.btdeviceapp.activity.MainActivity;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceConnectStateObserver;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceControllerInterface;
import com.cmtech.android.btdeviceapp.interfa.IBLEDeviceInterface;

/**
 * Created by bme on 2018/2/27.
 */

public abstract class BLEDeviceFragment extends Fragment implements IBLEDeviceConnectStateObserver {
    // MainActivity
    protected MainActivity activity;

    // 对应的控制器接口
    protected IBLEDeviceControllerInterface controller;

    // 对应的设备接口
    protected IBLEDeviceInterface device;

    // 连接状态tv
    protected TextView tvConnectState;

    protected ImageButton btnSwitchConnectState;

    protected ImageButton btnClose;

    public BLEDeviceFragment() {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvConnectState = view.findViewById(R.id.device_connect_state_tv);

        btnSwitchConnectState = view.findViewById(R.id.device_connectswitch_btn);

        btnClose = view.findViewById(R.id.device_close_btn);

        btnSwitchConnectState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchDeviceConnectState();
            }
        });


        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(BLEDeviceFragment.this.getClass().getSimpleName(), "is closed.");
                closeDevice();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(!(context instanceof MainActivity)) {
            throw new IllegalStateException("context不是MainActivity");
        }

        // 获得Activity
        activity = (MainActivity) context;

        // 获取controller
        controller = activity.getController(this);

        // 获取device
        if(controller != null) {
            device = controller.getDevice();
        }

        if(device == null || controller == null) {
            throw new IllegalStateException();
        }

        device.registerConnectStateObserver(this);

        activity.updateToolBarUsingBLEDevice((BLEDevice) device);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 更新连接状态
        updateConnectState();
    }

    @Override
    public void onStart() {
        super.onStart();

        // 连接设备
        connectDevice();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        controller.closeDevice();

        if(device != null)
            device.removeConnectStateObserver(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public IBLEDeviceInterface getDevice() {
        return device;
    }

    public void connectDevice() {
        controller.connectDevice();
    }

    public void disconnectDevice() {
        controller.disconnectDevice();
    }

    public void closeDevice() {
        activity.closeDevice(device);
    }

    public void switchDeviceConnectState() {
        controller.switchDeviceConnectState();
    }


    /////////////// IBLEDeviceConnectStateObserver接口函数//////////////////////
    @Override
    public void updateConnectState(final BLEDevice device) {
        if(device == this.device) {
            // isAdded()用来判断Fragment是否与Activity关联，如果关联了，才能更新状态信息
            if(isAdded()) updateConnectState();
        }
    }

    private void updateConnectState() {
        tvConnectState.setText(device.getDeviceConnectState().getDescription());
        switch (device.getDeviceConnectState()) {
            case CONNECT_SUCCESS:
                setImageButton(btnSwitchConnectState, R.mipmap.ic_connect_32px, true);
                break;

            case CONNECT_DISCONNECTING:
            case CONNECT_CONNECTING:
                setImageButton(btnSwitchConnectState, R.mipmap.ic_connecting_32px, false);
                break;

            default:
                setImageButton(btnSwitchConnectState, R.mipmap.ic_disconnect_32px, true);
                break;
        }
    }

    private void setImageButton(ImageButton btn, int imageId, boolean enable) {
        btn.setImageDrawable(getResources().getDrawable(imageId));
        btn.setEnabled(enable);
    }
    //////////////////////////////////////////////////////////////////////////

}
