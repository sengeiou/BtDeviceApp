package com.cmtech.android.ble.core;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

import com.cmtech.android.ble.callback.IBleConnectCallback;
import com.cmtech.android.ble.callback.IBleDataCallback;
import com.cmtech.android.ble.exception.BleException;
import com.vise.log.ViseLog;

import static com.cmtech.android.ble.core.DeviceConnectState.CLOSED;
import static com.cmtech.android.ble.core.DeviceConnectState.CONNECT;
import static com.cmtech.android.ble.core.DeviceConnectState.DISCONNECT;
import static com.cmtech.android.ble.core.DeviceConnectState.FAILURE;

/**
 * ClassName:      BleConnector
 * Description:    低功耗蓝牙Connector
 * Author:         chenm
 * CreateDate:     2018-02-19 07:02
 * UpdateUser:     chenm
 * UpdateDate:     2019-06-05 07:02
 * UpdateRemark:   更新说明
 * Version:        1.0
 * UpdateUser:     chenm
 * UpdateDate:     2019-10-21 07:02
 * UpdateRemark:   优化代码，
 * Version:        1.1
 */

public class BleConnector extends AbstractConnector {
    private BleGatt bleGatt; // Gatt，连接成功后赋值，完成连接状态改变处理以及数据通信功能
    private BleSerialGattCommandExecutor gattCmdExecutor; // Gatt命令执行器，在内部的一个单线程池中执行。连接成功后启动，连接失败或者断开时停止

    // connection callback
    private final IBleConnectCallback connectCallback = new IBleConnectCallback() {
        // connection success
        @Override
        public void onConnectSuccess(final BleGatt bleGatt) {
            // 防止重复连接成功
            if (state != CONNECT) {
                if (state == CLOSED) { // 设备已经关闭了，强行清除
                    bleGatt.close();
                    return;
                }

                ViseLog.e("Connect success: " + bleGatt);

                BleConnector.this.bleGatt = bleGatt;
                gattCmdExecutor.start();

                if (!connectorCallback.onConnectSuccess()) {
                    disconnect(false);
                } else {
                    setState(CONNECT);
                }
            }
        }

        // connection failure
        @Override
        public void onConnectFailure(final BleException exception) {
            if (state != FAILURE && state != CLOSED) {
                ViseLog.e("Connect failure: " + exception);

                bleGatt = null;
                if(gattCmdExecutor != null)
                    gattCmdExecutor.stop();
                setState(FAILURE);
                reconnect();
                connectorCallback.onConnectFailure();
            }
        }

        // disconnection
        @Override
        public void onDisconnect() {
            if (state != DISCONNECT && state != CLOSED) {
                ViseLog.e("Disconnect.");

                bleGatt = null;
                if(gattCmdExecutor != null)
                    gattCmdExecutor.stop();
                setState(DISCONNECT);
                reconnect();
                connectorCallback.onDisconnect();
            }
        }
    };

    public BleConnector(String address, IConnectorCallback connectorCallback) {
        super(address, connectorCallback);
    }

    public BleGatt getBleGatt() {
        return bleGatt;
    }

    // 设备是否包含所有指定gatt elements
    public boolean containGattElements(BleGattElement[] elements) {
        for (BleGattElement element : elements) {
            if (element == null || element.transformToGattObject(this) == null)
                return false;
        }
        return true;
    }

    // 设备是否包含gatt element
    public boolean containGattElement(BleGattElement element) {
        return !(element == null || element.transformToGattObject(this) == null);
    }

    @Override
    public boolean open(Context context) {
        ViseLog.e("BleConnector.open()");
        boolean success = super.open(context);
        if(success)
            gattCmdExecutor = new BleSerialGattCommandExecutor(this);
        return success;
    }

    @Override
    public void close() {
        ViseLog.e("BleConnector.close()");
        if(bleGatt != null)
            bleGatt.close();

        if(gattCmdExecutor != null) {
            gattCmdExecutor.stop();
            gattCmdExecutor = null;
        }
        bleGatt = null;

        super.close();
    }

    @Override
    public void connect() {
        if(BleScanner.isBleDisabled()) {
            if(context != null) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
            return;
        }
        ViseLog.e("BleConnector.connect()");
        super.connect();
        new BleGatt(context, address, connectCallback).connect();
    }

    @Override
    public void disconnect(boolean forever) {
        ViseLog.e("BleConnector.disconnect(): " + (forever ? "forever" : ""));
        super.disconnect(forever);
        if (bleGatt != null) {
            bleGatt.disconnect();
        }
    }

    public boolean isGattExecutorAlive() {
        return gattCmdExecutor.isAlive();
    }

    public final void read(BleGattElement element, IBleDataCallback dataCallback) {
        gattCmdExecutor.read(element, dataCallback);
    }

    public final void write(BleGattElement element, byte[] data, IBleDataCallback dataCallback) {
        gattCmdExecutor.write(element, data, dataCallback);
    }

    public final void write(BleGattElement element, byte data, IBleDataCallback dataCallback) {
        gattCmdExecutor.write(element, data, dataCallback);
    }

    public final void notify(BleGattElement element, boolean enable, IBleDataCallback receiveCallback) {
        gattCmdExecutor.notify(element, enable, receiveCallback);
    }

    public final void indicate(BleGattElement element, boolean enable, IBleDataCallback receiveCallback) {
        gattCmdExecutor.indicate(element, enable, receiveCallback);
    }

    public final void runInstantly(IBleDataCallback callback) {
        gattCmdExecutor.runInstantly(callback);
    }
}
