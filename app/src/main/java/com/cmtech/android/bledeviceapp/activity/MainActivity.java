package com.cmtech.android.bledeviceapp.activity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.ble.core.BleDeviceRegisterInfo;
import com.cmtech.android.ble.core.BleScanner;
import com.cmtech.android.ble.core.DeviceRegisterInfo;
import com.cmtech.android.ble.core.IDevice;
import com.cmtech.android.ble.core.WebDeviceRegisterInfo;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.exception.ScanException;
import com.cmtech.android.bledevice.ecg.activity.EcgRecordExplorerActivity;
import com.cmtech.android.bledevice.ecg.adapter.EcgCtrlPanelAdapter;
import com.cmtech.android.bledevice.hrmonitor.view.HrRecordExplorerActivity;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.DeviceFactory;
import com.cmtech.android.bledeviceapp.model.DeviceFragTabManager;
import com.cmtech.android.bledeviceapp.model.DeviceManager;
import com.cmtech.android.bledeviceapp.model.DeviceType;
import com.cmtech.android.bledeviceapp.model.FragTabManager;
import com.cmtech.android.bledeviceapp.model.MainToolbarManager;
import com.cmtech.android.bledeviceapp.model.NotifyService;
import com.cmtech.android.bledeviceapp.util.APKVersionCodeUtils;
import com.vise.log.ViseLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static android.bluetooth.BluetoothAdapter.STATE_OFF;
import static android.bluetooth.BluetoothAdapter.STATE_ON;
import static com.cmtech.android.ble.core.DeviceState.CLOSED;
import static com.cmtech.android.ble.core.IDevice.INVALID_BATTERY;
import static com.cmtech.android.bledevice.ecg.device.EcgFactory.ECGMONITOR_DEVICE_TYPE;
import static com.cmtech.android.bledevice.temphumid.model.TempHumidFactory.TEMPHUMID_DEVICE_TYPE;
import static com.cmtech.android.bledevice.thermo.model.ThermoFactory.THERMO_DEVICE_TYPE;
import static com.cmtech.android.bledeviceapp.MyApplication.showMessageUsingShortToast;
import static com.cmtech.android.bledeviceapp.activity.LoginActivity.SUPPORT_PLATFORM;
import static com.cmtech.android.bledeviceapp.activity.RegisterActivity.DEVICE_REGISTER_INFO;
import static com.cmtech.android.bledeviceapp.activity.ScanActivity.REGISTERED_DEVICE_ADDRESS_LIST;

/**
 *  MainActivity: 主界面
 *  Created by bme on 2018/2/19.
 */

public class MainActivity extends AppCompatActivity implements IDevice.OnDeviceListener, FragTabManager.OnFragmentUpdatedListener {
    private static final String TAG = "MainActivity";
    private final static int RC_REGISTER_DEVICE = 1;     // 注册设备返回码
    private final static int RC_MODIFY_REGISTER_INFO = 2;       // 修改设备注册信息返回码
    private final static int RC_MODIFY_ACCOUNT_INFO = 3;     // 修改账户信息返回码

    private final static SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());

    // 蓝牙状态改变广播接收器
    private final BroadcastReceiver btStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if(state == STATE_ON) {
                    BleScanner.clearInnerError();
                    MyApplication.showMessageUsingShortToast("蓝牙已开启");
                } else if(state == STATE_OFF) {
                    MyApplication.showMessageUsingShortToast("蓝牙已关闭");
                }
            }
        }
    };
    // 绑定状态广播接收器
    private final BroadcastReceiver bondStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Toast.makeText(context, device.getAddress() + "绑定成功。", Toast.LENGTH_SHORT).show();
                } else if(device.getBondState() == BluetoothDevice.BOND_BONDING){
                    Toast.makeText(context, device.getAddress() + "绑定中。", Toast.LENGTH_SHORT).show();
                } else if(device.getBondState() == BluetoothDevice.BOND_NONE) {
                    Toast.makeText(context, device.getAddress() + "绑定失败。", Toast.LENGTH_SHORT).show();
                }
            } else if(BluetoothDevice.ACTION_PAIRING_REQUEST.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    String strPsw = "000000";
                    device.setPin(strPsw.getBytes());
                    abortBroadcast();
                }
            }
        }
    };
    LocalDevicesFragment localDevicesFragment;
    //WebDevicesFragment webDevicesFragment;
    private NotifyService notifyService; // 通知服务,用于初始化BleDeviceManager，并管理后台通知
    private DeviceFragTabManager fragTabManager; // BleFragment和TabLayout管理器
    private MainToolbarManager toolbarManager; // 工具条管理器
    private DrawerLayout drawerLayout; // 侧滑界面
    private LinearLayout noDeviceOpenedLayout; // 无设备打开时的界面
    private RelativeLayout hasDeviceOpenedLayout; // 有设备打开时的界面，即包含设备Fragment和Tablayout的主界面
    private FloatingActionButton fabConnect; // 切换连接状态的FAB
    private TextView tvAccountName; // 账户名称控件
    private ImageView ivAccountImage; // 头像头像控件
    private boolean stopNotifyService = false; // 是否停止通知服务
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            notifyService = ((NotifyService.BleNotifyServiceBinder)iBinder).getService();
            // 成功绑定后初始化，否则请求退出
            if(notifyService != null) {
                initialize();
            } else {
                requestFinish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if(notifyService != null) {
                Intent stopServiceIntent = new Intent(MainActivity.this, NotifyService.class);
                stopService(stopServiceIntent);
                notifyService = null;
            }
            finish();
        }
    };

    public NotifyService getNotifyService() {
        return notifyService;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 确定账户已经登录
        if(!AccountManager.getInstance().isSignIn()) {
            Toast.makeText(this, "account sign in fail.", Toast.LENGTH_SHORT).show();
            finish();
        }
        ViseLog.e(AccountManager.getInstance().getAccount());

        // 启动并绑定通知服务
        Intent serviceIntent = new Intent(this, NotifyService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);

        // 登记蓝牙状态改变广播接收器
        IntentFilter bleStateIntent = new IntentFilter();
        bleStateIntent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(btStateChangedReceiver, bleStateIntent);

        // 登记绑定状态广播接收器
        IntentFilter bondIntent = new IntentFilter();
        bondIntent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        bondIntent.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        registerReceiver(bondStateReceiver, bondIntent);

        if(BleScanner.isBleDisabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
        }
    }

    // 主界面初始化
    private void initialize() {
        // 初始化工具条管理器
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tvDeviceBattery = findViewById(R.id.tv_device_battery);
        toolbarManager = new MainToolbarManager(this, toolbar, tvDeviceBattery);
        toolbarManager.setNavigationIcon(AccountManager.getInstance().getAccount().getImagePath());

        ViewPager pager = findViewById(R.id.vp_device_panel);
        TabLayout layout = findViewById(R.id.tl_device_panel);
        localDevicesFragment = new LocalDevicesFragment();
        //webDevicesFragment = new WebDevicesFragment();
        List<Fragment> fragmentList = new ArrayList<>();fragmentList.add(localDevicesFragment);
        List<String> titleList = new ArrayList<>();titleList.add("本地设备");
        EcgCtrlPanelAdapter fragAdapter = new EcgCtrlPanelAdapter(getSupportFragmentManager(), fragmentList, titleList);
        pager.setAdapter(fragAdapter);
        pager.setOffscreenPageLimit(1);
        layout.setupWithViewPager(pager);

        // 初始化导航视图
        initNavigation();
        updateNavigation();

        // 设置FAB，FloatingActionButton
        fabConnect = findViewById(R.id.fab_connect);
        fabConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeviceFragment fragment = (DeviceFragment) fragTabManager.getCurrentFragment();
                if(fragment != null) {
                    fragment.switchState();
                }
            }
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        noDeviceOpenedLayout = findViewById(R.id.layout_when_nodeivce_opened);
        hasDeviceOpenedLayout = findViewById(R.id.layout_when_device_opened);

        // 初始化BleFragTabManager
        TabLayout tabLayout = findViewById(R.id.tablayout_device);
        fragTabManager = new DeviceFragTabManager(getSupportFragmentManager(), tabLayout, R.id.layout_main_fragment);
        fragTabManager.setOnFragmentUpdatedListener(this);

        // 初始化主界面
        initMainLayout();

        // 为已经打开的设备创建并打开Fragment
        List<IDevice> openedDevices = DeviceManager.getOpenedDevice();
        for(IDevice device : openedDevices) {
            if(device.getState() != CLOSED) {
                createAndOpenFragment(device);
            }
        }
    }

    private void initNavigation() {
        NavigationView navView = findViewById(R.id.nav_view);
        View headerView = navView.getHeaderView(0);
        tvAccountName = headerView.findViewById(R.id.tv_account_name);
        ivAccountImage = headerView.findViewById(R.id.iv_account_image);

        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                //startActivityForResult(intent, RC_MODIFY_ACCOUNT_INFO);
            }
        });

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_scan_device: // 扫描设备
                        List<String> registeredMacList = DeviceManager.getDeviceMacList();
                        Intent scanIntent = new Intent(MainActivity.this, ScanActivity.class);
                        scanIntent.putExtra(REGISTERED_DEVICE_ADDRESS_LIST, (Serializable) registeredMacList);
                        startActivityForResult(scanIntent, RC_REGISTER_DEVICE);
                        return true;
                    case R.id.nav_query_record: // 查阅记录
                        PopupMenu popupMenu = new PopupMenu(MainActivity.this, item.getActionView());
                        popupMenu.inflate(R.menu.menu_query_record);
                        List<DeviceType> types = DeviceType.getSupportedDeviceTypes();
                        popupMenu.getMenu().findItem(R.id.nav_hr_record).setVisible(true);
                        popupMenu.getMenu().findItem(R.id.nav_ecg_record).setVisible(types.contains(ECGMONITOR_DEVICE_TYPE));
                        popupMenu.getMenu().findItem(R.id.nav_temphumid_record).setVisible(types.contains(TEMPHUMID_DEVICE_TYPE));
                        popupMenu.getMenu().findItem(R.id.nav_thermo_record).setVisible(types.contains(THERMO_DEVICE_TYPE));
                        @SuppressLint("RestrictedApi")
                        MenuPopupHelper popupHelper = new MenuPopupHelper(MainActivity.this, (MenuBuilder) popupMenu.getMenu(), item.getActionView());
                        popupHelper.setForceShowIcon(true);
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                switch (menuItem.getItemId()) {
                                    case R.id.nav_hr_record:
                                      Intent hrIntent = new Intent(MainActivity.this, HrRecordExplorerActivity.class);
                                        startActivity(hrIntent);
                                        return true;
                                    case R.id.nav_ecg_record:
                                        Intent recordIntent = new Intent(MainActivity.this, EcgRecordExplorerActivity.class);
                                        startActivity(recordIntent);
                                        return true;
                                    case R.id.nav_temphumid_record:
                                    case R.id.nav_thermo_record:
                                        Toast.makeText(MainActivity.this, "当前无记录。", Toast.LENGTH_SHORT).show();
                                        return true;
                                }
                                return false;
                            }
                        });
                        popupHelper.show();
                        return true;
                    case R.id.nav_open_store: // open KM store
                        String storeAddress = "https://decathlon.tmall.com/shop/view_shop.htm?spm=a21bo.2017.201863-1.d2.6dd211d9AzJgBt&user_number_id=352469034&pvid=067004f4-d493-413a-a4f7-003e62637549&pos=2&brandId=44506&acm=03014.1003.1.765824&scm=1007.13143.56636.100200300000000";
                        Intent storeIntent = new Intent(Intent.ACTION_VIEW);
                        storeIntent.setData(Uri.parse(storeAddress));
                        startActivity(storeIntent);
                        //Intent newsIntent = new Intent(MainActivity.this, NewsActivity.class);
                        //startActivity(newsIntent);
                        return true;
                    case R.id.nav_exit: // exit
                        requestFinish();
                        return true;
                }
                return false;
            }
        });
    }

    private void updateMainLayout(IDevice device) {
        if(device == null) {
            toolbarManager.setTitle(getString(R.string.app_name), getString(R.string.no_device_opened));
            toolbarManager.setBattery(INVALID_BATTERY);
            updateConnectFloatingActionButton(CLOSED.getIcon());
            invalidateOptionsMenu();
            updateMainLayoutVisibility(false);
        } else {
            String title = device.getName();
            DeviceRegisterInfo registerInfo = device.getRegisterInfo();
            if(!device.isLocal()) {
                title += ("-" + ((WebDeviceRegisterInfo) registerInfo).getBroadcastName());
            }
            toolbarManager.setTitle(title, device.getAddress());
            toolbarManager.setBattery(device.getBattery());
            updateConnectFloatingActionButton(device.getState().getIcon());
            updateCloseMenuItemVisible(true);
            updateMainLayoutVisibility(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_REGISTER_DEVICE: // 注册设备返回
                if(resultCode == RESULT_OK) {
                    BleDeviceRegisterInfo registerInfo = (BleDeviceRegisterInfo) data.getSerializableExtra(DEVICE_REGISTER_INFO);
                    registerDevice(registerInfo);
                }
                break;

            case RC_MODIFY_REGISTER_INFO: // 修改注册信息返回
                if ( resultCode == RESULT_OK) {
                    BleDeviceRegisterInfo registerInfo = (BleDeviceRegisterInfo) data.getSerializableExtra(DEVICE_REGISTER_INFO);
                    IDevice device = DeviceManager.findDevice(registerInfo);
                    if(device != null && registerInfo.saveToPref(pref)) {
                        Toast.makeText(MainActivity.this, "设备信息修改成功", Toast.LENGTH_SHORT).show();
                        device.updateRegisterInfo(registerInfo);
                        updateDeviceList();
                        Drawable drawable;
                        if(TextUtils.isEmpty(device.getImagePath())) {
                            DeviceType deviceType = DeviceType.getFromUuid(device.getUuidString());
                            if(deviceType == null) {
                                throw new IllegalStateException("The device type is not supported.");
                            }
                            drawable = ContextCompat.getDrawable(this, deviceType.getDefaultImageId());
                        } else {
                            drawable = new BitmapDrawable(getResources(), device.getImagePath());
                        }
                        fragTabManager.updateTabInfo(fragTabManager.findFragment(device), drawable, device.getName());
                        if(fragTabManager.isFragmentSelected(device)) {
                            toolbarManager.setTitle(device.getName(), device.getAddress());
                            toolbarManager.setBattery(device.getBattery());
                        }
                    } else {
                        showMessageUsingShortToast("设备信息修改失败");
                    }
                }
                break;

            case RC_MODIFY_ACCOUNT_INFO: // 修改用户信息返回
                if(resultCode == RESULT_OK) {
                    updateNavigation();
                    toolbarManager.setNavigationIcon(AccountManager.getInstance().getAccount().getImagePath());
                } else {
                    boolean logout = (data != null && data.getBooleanExtra("logout", false));
                    if(logout) { // 退出登录
                        logoutAccount();
                    }
                }
                break;
        }
    }

    private void registerDevice(BleDeviceRegisterInfo registerInfo) {
        if(registerInfo != null) {
            IDevice device = DeviceManager.createDeviceIfNotExist(registerInfo);
            if(device != null) {
                if(registerInfo.saveToPref(pref)) {
                    Toast.makeText(MainActivity.this, "设备注册成功", Toast.LENGTH_SHORT).show();
                    updateDeviceList();
                    device.addListener(notifyService);
                } else {
                    Toast.makeText(MainActivity.this, "设备注册失败", Toast.LENGTH_SHORT).show();
                    DeviceManager.deleteDevice(device);
                }
            }
        }
    }

    private void updateDeviceList() {
        localDevicesFragment.update();
        //webDevicesFragment.update();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuConfig = menu.findItem(R.id.toolbar_config);
        MenuItem menuClose = menu.findItem(R.id.toolbar_close);
        toolbarManager.setMenuItems(new MenuItem[]{menuConfig, menuClose});
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if(fragTabManager.size() == 0) {
            toolbarManager.updateMenuItemsVisible(new boolean[]{false, true});
        } else {
            toolbarManager.updateMenuItemsVisible(new boolean[]{true, true});
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DeviceFragment fragment;
        switch (item.getItemId()) {
            case android.R.id.home:
                openDrawer(true);
                break;

            case R.id.toolbar_config:
                fragment = (DeviceFragment) fragTabManager.getCurrentFragment();
                if(fragment != null) {
                    fragment.openConfigureActivity();
                }
                break;

            case R.id.toolbar_close:
                fragment = (DeviceFragment) fragTabManager.getCurrentFragment();
                if(fragment != null) {
                    closeFragment(fragment);
                } else {
                    requestFinish();
                }
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        ViseLog.e("MainActivity.onDestroy()");
        super.onDestroy();

        DeviceManager.removeDeviceListener(this);

        unbindService(serviceConnection);
        if(stopNotifyService) {
            Intent stopIntent = new Intent(MainActivity.this, NotifyService.class);
            stopService(stopIntent);
        }

        unregisterReceiver(btStateChangedReceiver);
        unregisterReceiver(bondStateReceiver);
    }

    private void requestFinish() {
        if(DeviceManager.hasOpenedDevice()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("退出应用");
            builder.setMessage("有设备打开，退出将关闭这些设备。");
            builder.setPositiveButton("退出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    stopNotifyService = true;
                    finish();
                }
            });
            builder.setNegativeButton("最小化", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    stopNotifyService = false;
                    openDrawer(false);
                    MainActivity.this.moveTaskToBack(true);
                }
            });
            builder.show();
        } else {
            stopNotifyService = true;
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        openDrawer(!drawerLayout.isDrawerOpen(GravityCompat.START));
    }

    // 设备状态更新
    @Override
    public void onStateUpdated(final IDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 更新设备列表Adapter
                updateDeviceList();
                // 更新设备的Fragment界面
                DeviceFragment deviceFrag = fragTabManager.findFragment(device);
                if(deviceFrag != null) deviceFrag.updateState();
                if(fragTabManager.isFragmentSelected(device)) {
                    updateConnectFloatingActionButton(device.getState().getIcon());
                    updateCloseMenuItemVisible(true);
                }
            }
        });

    }

    // 异常通知
    @Override
    public void onExceptionNotified(IDevice device, BleException ex) {
        showMessageUsingShortToast(device.getAddress() + "-" + ex.getDescription());
    }

    // 电量更新
    @Override
    public void onBatteryUpdated(final IDevice device) {
        if(fragTabManager.isFragmentSelected(device)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toolbarManager.setBattery(device.getBattery());
                }
            });
        }
    }

    // Fragment更新
    @Override
    public void onFragmentUpdated() {
        DeviceFragment fragment = (DeviceFragment)fragTabManager.getCurrentFragment();
        IDevice device = (fragment == null) ? null : fragment.getDevice();
        updateMainLayout(device);
    }

    // 打开设备
    public void openDevice(IDevice device) {
        if(device == null) return;

        DeviceFragment fragment = fragTabManager.findFragment(device);
        if(fragment != null) {
            openDrawer(false);
            fragTabManager.showFragment(fragment);
        } else {
            if(device.getState() == CLOSED)
                createAndOpenFragment(device);
        }
    }

    private void createAndOpenFragment(IDevice device) {
        if(device == null) return;

        DeviceFactory factory = DeviceFactory.getFactory(device.getRegisterInfo());
        if(factory != null) {
            openDrawer(false);
            Drawable drawable;
            if(TextUtils.isEmpty(device.getImagePath())) {
                DeviceType deviceType = DeviceType.getFromUuid(device.getUuidString());
                if(deviceType == null) {
                    throw new IllegalStateException("The device type is not supported.");
                }
                drawable = ContextCompat.getDrawable(this, deviceType.getDefaultImageId());
            } else {
                drawable = new BitmapDrawable(getResources(), device.getImagePath());
            }
            if(drawable == null) {
                DeviceType deviceType = DeviceType.getFromUuid(device.getUuidString());
                if(deviceType == null) {
                    throw new IllegalStateException("The device type is not supported.");
                }
                drawable = ContextCompat.getDrawable(this, deviceType.getDefaultImageId());
            }
            fragTabManager.openFragment(factory.createFragment(), drawable, device.getName());
            updateMainLayout(device);
        }
    }

    // 关闭Fragment
    private void closeFragment(final DeviceFragment fragment) {
        if(fragment != null)
            fragment.close();
    }

    // 移除Fragment
    public void removeFragment(DeviceFragment fragment) {
        fragTabManager.removeFragment(fragment);
    }

    // 从设备列表中删除设备
    public void removeRegisteredDevice(final IDevice device) {
        if(device == null) return;

        if(fragTabManager.isFragmentOpened(device)) {
            showMessageUsingShortToast("请先关闭该设备。");
            return;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除设备").setMessage("确定删除设备：" + device.getAddress()+'\n'+device.getName());
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DeviceRegisterInfo registerInfo = device.getRegisterInfo();
                if(registerInfo instanceof BleDeviceRegisterInfo && ((BleDeviceRegisterInfo)registerInfo).deleteFromPref(pref)) {
                    DeviceManager.deleteDevice(device);
                    updateDeviceList();
                } else {
                    Toast.makeText(MainActivity.this, "无法删除设备。", Toast.LENGTH_SHORT).show();
                }
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).show();
    }

    // 修改设备注册信息 
    public void modifyRegisterInfo(final DeviceRegisterInfo registerInfo) {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra(DEVICE_REGISTER_INFO, registerInfo);
        startActivityForResult(intent, RC_MODIFY_REGISTER_INFO);
    }

    private void initMainLayout() {
        TextView tvVersionName = noDeviceOpenedLayout.findViewById(R.id.tv_versionname);
        tvVersionName.setText(String.format("Ver%s", APKVersionCodeUtils.getVerName(this)));
        updateMainLayout(null);
    }

    // 打开或关闭侧滑菜单
    private void openDrawer(boolean open) {
        if(open) {
            drawerLayout.openDrawer(GravityCompat.START);
        } else {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    // 退出登录
    private void logoutAccount() {
        /*if(DeviceManager.hasOpenedDevice()) {
            Toast.makeText(this, "有设备打开，请先关闭设备。", Toast.LENGTH_SHORT).show();
            return;
        }

        AccountManager.getInstance().signOut();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SplashActivity.saveLoginInfo(pref, "", -1);

        Intent intent = new Intent(MainActivity.this, SplashActivity.class);
        startActivity(intent);
        finish();*/
    }

    private void updateNavigation() {
        Account account = AccountManager.getInstance().getAccount();
        if(account == null) {
            throw new IllegalStateException();
        }

        if(account.getName() == null || "".equals(account.getName().trim())) {
            tvAccountName.setText("请设置");
        } else {
            tvAccountName.setText(account.getName());
        }

        // load icon by platform name
        ivAccountImage.setImageResource(SUPPORT_PLATFORM.get(account.getPlatName()));
        //Glide.with(MyApplication.getContext()).load(R.id.ib_qq_login).into(ivAccountImage);

        /* load icon from imagePath
        String imagePath = account.getImagePath();
        if(imagePath != null && !"".equals(imagePath))
            Glide.with(MyApplication.getContext()).load(imagePath).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(ivAccountImage);*/
    }

    // 更新MainLayout的可视性
    private void updateMainLayoutVisibility(boolean hasDeviceOpened) {
        if(hasDeviceOpened && hasDeviceOpenedLayout.getVisibility() == View.INVISIBLE) {
            noDeviceOpenedLayout.setVisibility(View.INVISIBLE);
            hasDeviceOpenedLayout.setVisibility(View.VISIBLE);
        } else if(!hasDeviceOpened && hasDeviceOpenedLayout.getVisibility() == View.VISIBLE){
            noDeviceOpenedLayout.setVisibility(View.VISIBLE);
            hasDeviceOpenedLayout.setVisibility(View.INVISIBLE);
        }
    }

    // 更新连接浮动动作按钮
    private void updateConnectFloatingActionButton(int icon) {
        fabConnect.clearAnimation();
        fabConnect.setImageResource(icon);
        if(fabConnect.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) fabConnect.getDrawable()).start();
        }
    }

    private void updateCloseMenuItemVisible(boolean canClosed) {
        toolbarManager.updateMenuItemVisible(1, canClosed);
    }

}
