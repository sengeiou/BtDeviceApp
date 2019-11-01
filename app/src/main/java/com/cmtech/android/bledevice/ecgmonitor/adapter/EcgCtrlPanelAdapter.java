package com.cmtech.android.bledevice.ecgmonitor.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.ecgmonitor.adapter
 * ClassName:      EcgCtrlPanelAdapter
 * Description:    Ecg控制Adapter
 * Author:         chenm
 * CreateDate:     2019/4/15 上午5:44
 * UpdateUser:     更新者
 * UpdateDate:     2019/4/15 上午5:44
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class EcgCtrlPanelAdapter extends FragmentPagerAdapter {
    private final List<Fragment> fragmentList;
    private final List<String> titleList;

    public EcgCtrlPanelAdapter(FragmentManager fm, List<Fragment> fragmentList, List<String> titleList) {
        super(fm);
        this.fragmentList = fragmentList;
        this.titleList = titleList;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return titleList.size();
    }

    /**
     * //此方法用来显示tab上的名字
     *
     * @param position
     * @return
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return titleList.get(position);
    }
}