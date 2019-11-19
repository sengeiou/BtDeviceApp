package com.cmtech.android.bledeviceapp.model;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.bledeviceapp.activity.DeviceFragment;
import com.vise.log.ViseLog;

import java.util.List;

/**
 *
 * ClassName:      DeviceFragTabManager
 * Description:    设备的Fragment和TabLayout管理器
 * Author:         chenm
 * CreateDate:     2018-12-08 07:02
 * UpdateUser:     chenm
 * UpdateDate:     2018-12-08 07:02
 * UpdateRemark:   更新说明
 * Version:        1.0
 */

public class DeviceFragTabManager extends FragTabManager {

    public DeviceFragTabManager(FragmentManager fragmentManager, TabLayout tabLayout, int containerId) {
        this(fragmentManager, tabLayout, containerId, false);
    }

    public DeviceFragTabManager(FragmentManager fragmentManager, TabLayout tabLayout, int containerId, boolean isShowTabText) {
        super(fragmentManager, tabLayout, containerId, isShowTabText);
    }

    // 寻找设备的Fragment
    public DeviceFragment findFragment(IDevice device) {
        if(device != null) {
            List<Fragment> fragments = getFragmentList();
            for (Fragment fragment : fragments) {
                /*if (device.equals(((DeviceFragment) fragment).getDevice())) {
                    return (DeviceFragment) fragment;
                }*/
                if(device.getAddress().equals(((DeviceFragment) fragment).getDevice().getAddress())) {
                    return (DeviceFragment) fragment;
                }
            }
        }
        return null;
    }

    // 设备的Fragment是否打开
    public boolean isFragmentOpened(IDevice device) {
        return (findFragment(device) != null);
    }

    // 设备的Fragment是否被选中
    public boolean isFragmentSelected(IDevice device) {
        Fragment fragment = findFragment(device);
        ViseLog.e(fragment);
        return (fragment != null && fragment == getCurrentFragment());
    }
}
