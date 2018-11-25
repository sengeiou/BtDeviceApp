package com.cmtech.android.bledevicecore.model;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Created by bme on 2018/2/27.
 */

public abstract class BleDeviceFragment extends Fragment{
    // IBleDeviceActivity，包含Fragment的Activity
    private IBleDeviceActivity activity;

    // 对应的devFragPair
    private DeviceFragmentPair devFragPair;

    // 对应的设备
    private BleDevice device;
    public BleDevice getDevice() {
        return device;
    }

    public BleDeviceFragment() {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(!(context instanceof IBleDeviceActivity)) {
            throw new IllegalStateException("context不是IBleDeviceActivity");
        }

        // 获得Activity
        activity = (IBleDeviceActivity) context;

        // 获取controller
        devFragPair = activity.getDevFragPair(this);

        // 获取device
        if(devFragPair != null) {
            device = devFragPair.getDevice();
        }

        /// 这里有时候重启时会导致错误
        if(device == null || devFragPair == null) {
            //throw new IllegalStateException();
            activity.closeDevice(this);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 打开设备
        openDevice();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 更新连接状态
        updateDeviceState();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        device.close();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    // 打开设备
    public void openDevice() {
        device.open();
    }

    // 关闭设备
    // 为什么这里不是调用controller.closeDevice()，而是调用activity.closeDevice(fragment)???
    // 因为关闭一个带Fragment的设备，除了要关闭设备本身以外，还要销毁它的Fragment，并将设备的控制器从控制器列表中删除
    // 这些动作需要调用activity.closeDevice才能完成
    // 关闭设备的动作会在销毁Fragment时触发onDestroy()，那里会调用controller.closeDevice()来关闭设备
    public void closeDevice() {
        activity.closeDevice(this);
    }

    // 切换设备状态，根据设备的当前状态实现状态切换
    public void switchState() {
        device.switchState();
    }

    /*// 断开设备
    public void disconnectDevice() {
        devFragPair.disconnectDevice();
    }*/

    // 更新设备连接状态
    public void updateDeviceState(final BleDevice device) {
        // isAdded()用来判断Fragment是否与Activity关联，如果关联了，才能更新状态信息
        if(device == this.device && isAdded()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    updateDeviceState();
                }
            });
        }
    }

    private void updateDeviceState() {

    }

    //////////////////////////////////////////////////////////////////////////

}
