package com.cmtech.android.bledevice.hrmonitor.model;

import android.content.Context;
import android.widget.Toast;

import com.cmtech.android.ble.callback.IBleDataCallback;
import com.cmtech.android.ble.core.AbstractDevice;
import com.cmtech.android.ble.core.BleConnector;
import com.cmtech.android.ble.core.BleGattElement;
import com.cmtech.android.ble.core.DeviceInfo;
import com.cmtech.android.ble.core.DeviceState;
import com.cmtech.android.ble.exception.BleException;
import com.cmtech.android.ble.utils.UuidUtil;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.util.ByteUtil;
import com.cmtech.android.bledeviceapp.util.UnsignedUtil;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.cmtech.android.bledevice.view.ScanWaveView.DEFAULT_ZERO_LOCATION;
import static com.cmtech.android.bledeviceapp.AppConstant.CCC_UUID;
import static com.cmtech.android.bledeviceapp.AppConstant.MY_BASE_UUID;
import static com.cmtech.android.bledeviceapp.AppConstant.STANDARD_BLE_UUID;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.model
 * ClassName:      HRMonitorDevice
 * Description:    BLE heart rate monitor device
 * Author:         chenm
 * CreateDate:     2020-02-04 06:16
 * UpdateUser:     chenm
 * UpdateDate:     2020-02-04 06:16
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class HRMonitorDevice extends AbstractDevice {
    public static final short INVALID_HEART_RATE = -1;
    private static final int DEFAULT_CALI_1MV = 164; // default 1mV calibration value
    private static final int DEFAULT_SAMPLE_RATE = 125; // default sample rate, unit: Hz
    private static final EcgLeadType DEFAULT_LEAD_TYPE = EcgLeadType.LEAD_I; // default lead type

    private static final byte ECG_LOCKED = (byte)0x00;
    private static final byte ECG_UNLOCKED = (byte)0x01;

    // heart rate measurement service
    private static final String hrMonitorServiceUuid = "180D"; // standart ble heart rate service UUID
    private static final String hrMonitorMeasUuid = "2A37"; // 心率测量特征UUID
    private static final String hrMonitorSensLocUuid = "2A38"; // 测量位置UUID
    private static final String hrMonitorCtrlPtUuid = "2A39"; // 控制点UUID

    private static final UUID hrMonitorServiceUUID = UuidUtil.stringToUUUID(hrMonitorServiceUuid, STANDARD_BLE_UUID);
    private static final UUID hrMonitorMeasUUID = UuidUtil.stringToUUUID(hrMonitorMeasUuid, STANDARD_BLE_UUID);
    private static final UUID hrMonitorSensLocUUID = UuidUtil.stringToUUUID(hrMonitorSensLocUuid, STANDARD_BLE_UUID);
    private static final UUID hrMonitorCtrlPtUUID = UuidUtil.stringToUUUID(hrMonitorCtrlPtUuid, STANDARD_BLE_UUID);

    private static final BleGattElement HRMONITORMEAS =
            new BleGattElement(hrMonitorServiceUUID, hrMonitorMeasUUID, null, "heart rate measurement");
    private static final BleGattElement HRMONITORMEASCCC =
            new BleGattElement(hrMonitorServiceUUID, hrMonitorMeasUUID, CCC_UUID, "heart rate measurement CCC");
    private static final BleGattElement HRMONITORSENSLOC =
            new BleGattElement(hrMonitorServiceUUID, hrMonitorSensLocUUID, null, "sensor location");
    private static final BleGattElement HRMONITORCTRLPT =
            new BleGattElement(hrMonitorServiceUUID, hrMonitorCtrlPtUUID, null, "control point for energy expire");

    // battery service
    private static final String battServiceUuid = "180F";
    private static final String battLevelUuid = "2A19";
    private static final UUID battServiceUUID = UuidUtil.stringToUUUID(battServiceUuid, STANDARD_BLE_UUID);
    private static final UUID battLevelUUID = UuidUtil.stringToUUUID(battLevelUuid, STANDARD_BLE_UUID);
    private static final BleGattElement BATTLEVEL = new BleGattElement(battServiceUUID, battLevelUUID, null, "电池电量百分比");
    private static final BleGattElement BATTLEVELCCC = new BleGattElement(battServiceUUID, battLevelUUID, CCC_UUID, "电池电量CCC");

    // ecg service
    private static final String ecgServiceUuid = "AA40";
    private static final String ecgMeasUuid = "AA41";
    private static final String ecg1mVCaliUuid = "AA42";
    private static final String ecgSampleRateUuid = "AA43";
    private static final String ecgLeadTypeUuid = "AA44";
    private static final String ecgLockStatusUuid = "AA45";
    private static final UUID ecgServiceUUID = UuidUtil.stringToUUUID(ecgServiceUuid, MY_BASE_UUID);
    private static final UUID ecgMeasUUID = UuidUtil.stringToUUUID(ecgMeasUuid, MY_BASE_UUID);
    private static final UUID ecg1mVCaliUUID = UuidUtil.stringToUUUID(ecg1mVCaliUuid, MY_BASE_UUID);
    private static final UUID ecgSampleRateUUID = UuidUtil.stringToUUUID(ecgSampleRateUuid, MY_BASE_UUID);
    private static final UUID ecgLeadTypeUUID = UuidUtil.stringToUUUID(ecgLeadTypeUuid, MY_BASE_UUID);
    private static final UUID ecgLockStatusUUID = UuidUtil.stringToUUUID(ecgLockStatusUuid, MY_BASE_UUID);

    private static final BleGattElement ECGMEAS = new BleGattElement(ecgServiceUUID, ecgMeasUUID, null, "ECG Data Packet");
    private static final BleGattElement ECGMEASCCC = new BleGattElement(ecgServiceUUID, ecgMeasUUID, CCC_UUID, "ECG Data Packet CCC");
    private static final BleGattElement ECG1MVCALI = new BleGattElement(ecgServiceUUID, ecg1mVCaliUUID, null, "ECG 1mV Calibration");
    private static final BleGattElement ECGSAMPLERATE = new BleGattElement(ecgServiceUUID, ecgSampleRateUUID, null, "ECG Sample Rate");
    private static final BleGattElement ECGLEADTYPE = new BleGattElement(ecgServiceUUID, ecgLeadTypeUUID, null, "ECG Lead Type");
    private static final BleGattElement ECGLOCKSTATUS = new BleGattElement(ecgServiceUUID, ecgLockStatusUUID, null, "ECG Lock Status");


    private int sampleRate = DEFAULT_SAMPLE_RATE; // sample rate
    private int cali1mV = DEFAULT_CALI_1MV; // 1mV calibration value
    private EcgLeadType leadType = DEFAULT_LEAD_TYPE; // lead type

    private boolean hasBattService = false; // has battery service
    private boolean ecgLock = true; // ecg lock status
    private EcgDataProcessor ecgProcessor; // ecg processor

    private OnHRMonitorDeviceListener listener; // device listener

    private final HRMonitorConfiguration config; // hr device configuration

    private BleHrRecord10 record;
    private boolean isRecord = false;

    private Timer ttsTimer = new Timer();
    private volatile boolean waitSpeak = false;

    public HRMonitorDevice(DeviceInfo registerInfo) {
        super(registerInfo);
        HRMonitorConfiguration config = LitePal.where("address = ?", getAddress()).findFirst(HRMonitorConfiguration.class);
        if (config == null) {
            config = new HRMonitorConfiguration();
            config.setAddress(getAddress());
            config.save();
        }
        this.config = config;
    }

    public void switchRecord() {
        isRecord = !isRecord;
        if(isRecord) {
            record = BleHrRecord10.create(new byte[]{0x01,0x00}, getAddress(), AccountManager.getInstance().getAccount());
            if(record != null && listener != null)
                listener.onHRStatInfoUpdated(record.getFilterHrList(), record.getHrMax(), record.getHrAve(), record.getHrHistogram());
        } else {
            if(record != null) {
                if (record.getFilterHrList().size() < 6) {
                    Toast.makeText(MyApplication.getContext(), "记录太短，未保存。", Toast.LENGTH_SHORT).show();
                } else {
                    record.save();
                    Toast.makeText(MyApplication.getContext(), "记录已保存。", Toast.LENGTH_SHORT).show();
                    ViseLog.e(record.toString());
                }
                record = null;
            }
        }
    }

    @Override
    public void open(Context context) {
        super.open(context);
    }

    @Override
    public void close() {
        super.close();

        if(isRecord) {
            switchRecord();
        }

        stopSpeak();
    }

    @Override
    public boolean onConnectSuccess() {
        BleConnector connector = (BleConnector)this.connector;

        BleGattElement[] elements = new BleGattElement[]{HRMONITORMEAS, HRMONITORMEASCCC};
        if(connector.containGattElements(elements)) {
            readSensorLocation();
            switchHRMeasure(true);
        } else {
            return false;
        }

        elements = new BleGattElement[]{BATTLEVEL, BATTLEVELCCC};
        if(connector.containGattElements(elements)) {
            hasBattService = true;
            readBatteryLevel();
            switchBatteryMeasure(true);
        }

        elements = new BleGattElement[]{ECGMEAS, ECGMEASCCC, ECG1MVCALI, ECGSAMPLERATE, ECGLEADTYPE, ECGLOCKSTATUS};
        if(connector.containGattElements(elements)) {
            readEcgLockStatus();
            connector.runInstantly(new IBleDataCallback() {
                @Override
                public void onSuccess(byte[] data, BleGattElement element) {
                    if(ecgLock) {
                        if (listener != null)
                            listener.onFragmentUpdated(sampleRate, cali1mV, DEFAULT_ZERO_LOCATION, ecgLock);
                    }
                    else {
                        initEcgService();
                    }
                }

                @Override
                public void onFailure(BleException exception) {

                }
            });
        }

        if(config.isSpeak())
            startSpeak(config.getSpeakPeriod());

        return true;
    }

    @Override
    public void onConnectFailure() {
        if(ecgProcessor != null) {
            ecgProcessor.stop();
        }

        stopSpeak();
    }

    @Override
    public void onDisconnect() {
        if(ecgProcessor != null) {
            ecgProcessor.stop();
        }

        stopSpeak();
    }

    @Override
    public void disconnect(final boolean forever) {
        switchHRMeasure(false);
        switchBatteryMeasure(false);
        switchEcgSignal(false);
        ((BleConnector)connector).runInstantly(new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                HRMonitorDevice.super.disconnect(forever);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
        super.disconnect(forever);
    }

    public final int getSampleRate() {
        return sampleRate;
    }

    public final int getCali1mV() {
        return cali1mV;
    }

    public final boolean isEcgLock() {
        return ecgLock;
    }

    public final boolean isRecord() {
        return isRecord;
    }

    public final HRMonitorConfiguration getConfig() {
        return config;
    }

    public void updateConfig(HRMonitorConfiguration config) {
        boolean isSpeakChanged = (this.config.isSpeak() != config.isSpeak() || this.config.getSpeakPeriod() != config.getSpeakPeriod());
        this.config.copyFrom(config);
        this.config.save();
        if(isSpeakChanged && getState() == DeviceState.CONNECT) {
            if(this.config.isSpeak())
                startSpeak(this.config.getSpeakPeriod());
            else {
                stopSpeak();
            }
        }
    }

    public void setListener(OnHRMonitorDeviceListener listener) {
        this.listener = listener;
    }

    public void removeListener() {
        this.listener = null;
    }

    private void startSpeak(int speakPeriod) {
        ttsTimer.cancel();
        waitSpeak = false;
        ttsTimer = new Timer();
        ttsTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                waitSpeak = true;
            }
        }, speakPeriod * 60 * 1000L, speakPeriod * 60 * 1000L);
    }

    private void stopSpeak() {
        ttsTimer.cancel();
        waitSpeak = false;
    }

    private void readSensorLocation() {
        ((BleConnector)connector).read(HRMONITORSENSLOC, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                if(listener != null) {
                    listener.onHRSensLocUpdated(data[0]);
                }
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
    }

    private void switchHRMeasure(boolean isStart) {
        //((BleConnector)connector).notify(HRMONITORMEASCCC, false, null);

        if(isStart) {
            IBleDataCallback notifyCallback = new IBleDataCallback() {
                @Override
                public void onSuccess(byte[] data, BleGattElement element) {
                    try {
                        BleHeartRateData heartRateData = new BleHeartRateData(data);
                        if(listener != null) {
                            listener.onHRUpdated(heartRateData);
                        }
                        if(isRecord && record.process((short) heartRateData.getBpm(), heartRateData.getTime())) {
                            if(listener != null)
                                listener.onHRStatInfoUpdated(record.getFilterHrList(), record.getHrMax(), record.getHrAve(), record.getHrHistogram());
                        }

                        if(waitSpeak) {
                            waitSpeak = false;
                            int hundred = heartRateData.getBpm()/100;
                            String hundredStr = (hundred > 0) ? hundred+"百" : "";
                            String str = "当前心率:" + hundredStr + heartRateData.getBpm()%100;
                            MyApplication.getTTS().speak(str);
                            ViseLog.e(str);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(BleException exception) {

                }
            };
            ((BleConnector)connector).notify(HRMONITORMEASCCC, true, notifyCallback);
        } else {
            ((BleConnector)connector).notify(HRMONITORMEASCCC, false, null);
        }

    }

    private void switchBatteryMeasure(boolean isStart) {
        if(!hasBattService) return;

        //((BleConnector)connector).notify(BATTLEVELCCC, false, null);
        if(isStart) {
            IBleDataCallback notifyCallback = new IBleDataCallback() {
                @Override
                public void onSuccess(byte[] data, BleGattElement element) {
                    setBattery(data[0]);
                }

                @Override
                public void onFailure(BleException exception) {

                }
            };
            ((BleConnector)connector).notify(BATTLEVELCCC, true, notifyCallback);
        } else {
            ((BleConnector)connector).notify(BATTLEVELCCC, false, null);
        }
    }

    private void readBatteryLevel() {
        ((BleConnector)connector).read(BATTLEVEL, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                setBattery(data[0]);
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
    }

    private void readEcgLockStatus() {
        ((BleConnector)connector).read(ECGLOCKSTATUS, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                ecgLock = (data[0] == ECG_LOCKED);
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
    }

    private void initEcgService() {
        readSampleRate();
        read1mVCali();
        readLeadType();
        ((BleConnector)connector).runInstantly(new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                ecgProcessor = new EcgDataProcessor(HRMonitorDevice.this);
                if (listener != null)
                    listener.onFragmentUpdated(sampleRate, cali1mV, DEFAULT_ZERO_LOCATION, ecgLock);
            }

            @Override
            public void onFailure(BleException exception) {

            }
        });
    }

    private void readSampleRate() {
        ((BleConnector)connector).read(ECGSAMPLERATE, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                sampleRate = UnsignedUtil.getUnsignedShort(ByteUtil.getShort(data));
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
    }

    private void read1mVCali() {
        ((BleConnector)connector).read(ECG1MVCALI, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                cali1mV = UnsignedUtil.getUnsignedShort(ByteUtil.getShort(data));
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
    }

    private void readLeadType() {
        ((BleConnector)connector).read(ECGLEADTYPE, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                leadType = EcgLeadType.getFromCode(UnsignedUtil.getUnsignedByte(data[0]));
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
    }

    public void lockEcg(final boolean ecgLock) {
        if(this.ecgLock == ecgLock) return;

        byte data = (ecgLock) ? ECG_LOCKED : ECG_UNLOCKED;

        ((BleConnector) connector).write(ECGLOCKSTATUS, data, new IBleDataCallback() {
            @Override
            public void onSuccess(byte[] data, BleGattElement element) {
                HRMonitorDevice.this.ecgLock = ecgLock;
            }

            @Override
            public void onFailure(BleException exception) {
            }
        });
    }

    public void switchEcgSignal(boolean isStart) {
        if(ecgLock) return;

        if(ecgProcessor != null)
            ecgProcessor.stop();

        //((BleConnector)connector).notify(ECGMEASCCC, false, null);

        if(isStart) {
            ecgProcessor.start();
            IBleDataCallback notifyCallback = new IBleDataCallback() {
                @Override
                public void onSuccess(byte[] data, BleGattElement element) {
                    //ViseLog.i("ecg data: " + Arrays.toString(data));
                    ecgProcessor.processData(data);
                }

                @Override
                public void onFailure(BleException exception) {

                }
            };
            ((BleConnector)connector).notify(ECGMEASCCC, true, notifyCallback);
        } else {
            ((BleConnector)connector).notify(ECGMEASCCC, false, null);
        }
    }

    public void showEcgSignal(int ecgSignal) {
        if (listener != null) {
            listener.onEcgSignalShowed(ecgSignal);
        }
    }
}
