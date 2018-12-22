package com.cmtech.android.bledevice.thermo.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.cmtech.android.bledevice.core.BleDataOpException;
import com.cmtech.android.bledevice.core.BleGattElement;
import com.cmtech.android.bledevice.core.IBleDataOpCallback;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledevice.core.BleDevice;
import com.cmtech.android.bledevice.core.BleDeviceBasicInfo;

import java.util.LinkedList;
import java.util.List;

import static com.cmtech.android.bledevice.core.BleDeviceConstant.CCCUUID;
import static com.cmtech.android.bledevice.core.BleDeviceConstant.MY_BASE_UUID;

/**
 * ThermoDevice: 体温计设备类
 * Created by bme on 2018/9/20.
 */


public class ThermoDevice extends BleDevice {
    private static final int MSG_THERMODATA = 1;

    ///////////////// 体温计Service相关的常量////////////////
    private static final String thermoServiceUuid       = "aa30";           // 体温计服务UUID:aa30
    private static final String thermoDataUuid          = "aa31";           // 体温数据特征UUID:aa31
    private static final String thermoControlUuid       = "aa32";           // 体温测量控制UUID:aa32
    private static final String thermoPeriodUuid        = "aa33";           // 体温采样周期UUID:aa33

    private static final BleGattElement THERMODATA =
            new BleGattElement(thermoServiceUuid, thermoDataUuid, null, MY_BASE_UUID, "体温值");
    private static final BleGattElement THERMOCONTROL =
            new BleGattElement(thermoServiceUuid, thermoControlUuid, null, MY_BASE_UUID, "体温Ctrl");
    private static final BleGattElement THERMOPERIOD =
            new BleGattElement(thermoServiceUuid, thermoPeriodUuid, null, MY_BASE_UUID, "采集周期(s)");
    private static final BleGattElement THERMODATACCC =
            new BleGattElement(thermoServiceUuid, thermoDataUuid, CCCUUID, MY_BASE_UUID, "体温CCC");

    private static final byte DEFAULT_SAMPLE_PERIOD = (byte)0x01;
    ///////////////////////////////////////////////////////

    // 当前体温数据观察者列表
    private final List<IThermoDataObserver> thermoDataObserverList = new LinkedList<>();


    private double curTemp = 0.0;

    public double getCurTemp() {
        return curTemp;
    }

    public void setCurTemp(double curTemp) {
        this.curTemp = curTemp;
    }

    private double highestTemp = 0.0;

    public double getHighestTemp() { return highestTemp; }

    public void setHighestTemp(double highestTemp) {
        this.highestTemp = highestTemp;
    }

    public void resetHighestTemp() {
        highestTemp = curTemp;
        updateThermoData();
    }

    public ThermoDevice(BleDeviceBasicInfo basicInfo) {
        super(basicInfo);
        initializeAfterConstruction();
    }

    private void initializeAfterConstruction() {
    }

    @Override
    public boolean executeAfterConnectSuccess() {
        gattOperator.start();

        // 检查是否有正常的温湿度服务和特征值
        BleGattElement[] elements = new BleGattElement[]{THERMODATA, THERMOCONTROL, THERMOPERIOD, THERMODATACCC};
        if(!gattOperator.checkGattElement(elements)) return false;

        resetHighestTemp();

        // 读温度数据
        readThermoData();

        startThermometer(DEFAULT_SAMPLE_PERIOD);
        return true;
    }

    @Override
    public void executeAfterDisconnect() {
        gattOperator.stop();
    }

    @Override
    public void executeAfterConnectFailure() {
        gattOperator.stop();
    }

    @Override
    public synchronized void processGattMessage(Message msg)
    {
        if (msg.what == MSG_THERMODATA) {
            if (msg.obj != null) {
                byte[] data = (byte[]) msg.obj;
                double temp = ByteUtil.getShort(data)/100.0;
                setCurTemp(temp);

                if(temp > highestTemp) {
                    setHighestTemp(temp);
                }

                updateThermoData();
            }
        }
    }



    // 登记体温数据观察者
    public void registerThermoDataObserver(IThermoDataObserver observer) {
        if(!thermoDataObserverList.contains(observer)) {
            thermoDataObserverList.add(observer);
        }
    }

    // 删除体温数据观察者
    public void removeThermoDataObserver(IThermoDataObserver observer) {
        int index = thermoDataObserverList.indexOf(observer);
        if(index >= 0) {
            thermoDataObserverList.remove(index);
        }
    }

    // 通知体温数据观察者
    private void updateThermoData() {
        for(final IThermoDataObserver observer : thermoDataObserverList) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(observer != null)
                        observer.updateThermoData();
                }
            });
        }
    }

    private void readThermoData() {
        // 读温度数据
        gattOperator.addReadCommand(THERMODATA, new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                sendGattMessage(MSG_THERMODATA, data);
            }

            @Override
            public void onFailure(BleDataOpException exception) {
            }
        });
    }

    /*
    启动体温计，设置采样周期
    period: 采样周期，单位：秒
     */
    private void startThermometer(byte period) {
        // 设置采样周期
        gattOperator.addWriteCommand(THERMOPERIOD, period, null);

        IBleDataOpCallback notifyCallback = new IBleDataOpCallback() {
            @Override
            public void onSuccess(byte[] data) {
                sendGattMessage(MSG_THERMODATA, data);
            }

            @Override
            public void onFailure(BleDataOpException exception) {

            }
        };

        // enable温度数据notify
        gattOperator.addNotifyCommand(THERMODATACCC, true, null, notifyCallback);

        // 启动温度采集
        gattOperator.addWriteCommand(THERMOCONTROL, (byte)0x03, null);
    }

}
