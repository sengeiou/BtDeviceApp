package com.cmtech.android.bledeviceapp.model;

import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * ClassName:      FragTabManager
 * Description:    Fragment和TabLayout管理器
 * Author:         chenm
 * CreateDate:     2018-03-16 07:02
 * UpdateUser:     chenm
 * UpdateDate:     2018-03-16 07:02
 * UpdateRemark:   更新说明
 * Version:        1.0
 */

public class TabFragManager {
    private final List<Fragment> fragList = new ArrayList<>(); // fragment list
    private final InnerFragmentManager innerFragManager; // Fragment内部管理器实例
    private final TabLayout tabLayout; // TabLayout实例
    private final boolean isShowTabText; // is tab text showed
    private int curPos = -1; // 当前显示的Fragment和Tab的位置
    private OnFragmentUpdatedListener listener = null; // fragment更新监听器

    // fragment更新监听器接口
    public interface OnFragmentUpdatedListener {
        void onFragmentUpdated();
    }

    /**
     * 构造器
     * @param fragManager : fragment管理器
     * @param tabLayout：tabLayout
     * @param containerId: fragment容器ID
     */
    TabFragManager(FragmentManager fragManager, TabLayout tabLayout, int containerId, boolean isShowTabText) {
        innerFragManager = new InnerFragmentManager(fragManager, containerId);
        this.tabLayout = tabLayout;
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                if(pos < 0) return;
                // 隐藏当前的Fragment
                if(curPos != pos) innerFragManager.hideFragment(getCurrentFragment());
                innerFragManager.showFragment(getFragment(pos)); // 显示选中的Fragment
                curPos = pos;
                if(listener != null) {
                    listener.onFragmentUpdated();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        this.isShowTabText = isShowTabText;
    }

    // Fragment数量
    public int size() {
        return fragList.size();
    }

    // 设置fragment更新监听器
    public void setOnFragmentUpdatedListener(OnFragmentUpdatedListener listener) {
        this.listener = listener;
    }

    /**
     * 打开Fragment
     * @param fragment: fragment
     * @param drawable: tablayout上的图标drawable
     * @param tabText: tablayout上的文字
     */
    public void openFragment(Fragment fragment, Drawable drawable, String tabText) {
        if(fragment == null || fragList.contains(fragment)) return;

        innerFragManager.addFragment(fragment, "");

        View view = LayoutInflater.from(MyApplication.getContext()).inflate(R.layout.tab_view, null);
        ((ImageView)view.findViewById(R.id.iv_tab_image)).setImageDrawable(drawable);
        ((TextView)view.findViewById(R.id.tv_tab_text)).setText((isShowTabText) ? tabText : "");

        TabLayout.Tab tab = tabLayout.newTab();
        tab.setCustomView(view);
        tabLayout.addTab(tab, true);
    }

    private Fragment getFragment(int pos) {
        return (pos >= 0 && pos < fragList.size()) ? fragList.get(pos) : null;
    }

    // 获取当前fragment
    public Fragment getCurrentFragment() {
        return getFragment(curPos);
    }

    // 更新Fragment的tab信息
    public void updateTabInfo(Fragment fragment, Drawable drawable, String tabText) {
        if(fragment == null || !fragList.contains(fragment)) return;

        TabLayout.Tab tab = tabLayout.getTabAt(fragList.indexOf(fragment));
        if(tab != null) {
            View view = tab.getCustomView();
            if(view != null) {
                if(isShowTabText) {
                    TextView tv = view.findViewById(R.id.tv_tab_text);
                    tv.setText(tabText);
                }
                ImageView imageView = view.findViewById(R.id.iv_tab_image);
                imageView.setImageDrawable(drawable);
                tab.setCustomView(view);
            }
        }
    }

    // 显示Fragment
    public void showFragment(Fragment fragment) {
        if(fragment == null || !fragList.contains(fragment)) return;

        int index = fragList.indexOf(fragment);
        TabLayout.Tab tab = tabLayout.getTabAt(index);
        if(tab != null)
            tab.select();
    }

    // 删除Fragment
    public void removeFragment(Fragment fragment) {
        if(fragment == null || !fragList.contains(fragment)) return;

        int index = fragList.indexOf(fragment);
        innerFragManager.removeFragment(fragment);
        TabLayout.Tab tab = tabLayout.getTabAt(index);
        if(tab != null)
            tabLayout.removeTab(tab);

        if(size() == 0 && listener != null) {
            listener.onFragmentUpdated();
        }
    }

    // 获取Fragment列表
    public List<Fragment> getFragList() {
        return fragList;
    }

    private class InnerFragmentManager {
        private final FragmentManager fragManager;
        private final int containerId;

        InnerFragmentManager(FragmentManager fragManager, int containerId) {
            this.fragManager = fragManager;
            this.containerId = containerId;
        }

        void addFragment(Fragment fragment, String tag) {
            if(fragment != null) {
                fragList.add(fragment);
                FragmentTransaction fTransaction = fragManager.beginTransaction();
                fTransaction.add(containerId, fragment, tag);
                fTransaction.commit();
            }
        }

        void removeFragment(Fragment fragment) {
            if (fragment != null) {
                fragList.remove(fragment);
                FragmentTransaction transaction = fragManager.beginTransaction();
                transaction.remove(fragment);
                transaction.commit();
            }
        }

        void hideFragment(Fragment fragment) {
            if (fragment != null) {
                FragmentTransaction transaction = fragManager.beginTransaction();
                transaction.hide(fragment);
                transaction.commit();
            }
        }

        void showFragment(Fragment fragment) {
            if (fragment != null) {
                FragmentTransaction transaction = fragManager.beginTransaction();
                transaction.show(fragment);
                transaction.commit();
            }
        }
    }
}
