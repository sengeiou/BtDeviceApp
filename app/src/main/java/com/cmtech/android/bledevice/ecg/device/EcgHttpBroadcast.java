package com.cmtech.android.bledevice.ecg.device;


import android.util.Log;

import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.util.HttpUtils;
import com.vise.log.ViseLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;


/**
 *
 * ClassName:      EcgHttpBroadcast
 * Description:    心电Http广播
 * Author:         chenm
 * CreateDate:     2019/11/16 上午4:52
 * UpdateUser:     chenm
 * UpdateDate:     2019/11/16 上午4:52
 * UpdateRemark:   优化代码
 * Version:        1.0
 */

public class EcgHttpBroadcast {
    private static final String TAG = "EcgHttpBroadcast";

    private static final String upload_url = "http://huawei.tighoo.com/home/upload?";
    private static final String getuser_url = "http://huawei.tighoo.com/home/GetUsers?";

    private static final String TYPE_USER_ID = "open_id"; // 用户ID
    private static final String TYPE_DEVICE_ID = "deviceId"; // 设备ID
    private static final String TYPE_SAMPLE_RATE = "SR"; // 采样率
    private static final String TYPE_CALI_VALUE = "CALI"; // 标定值
    private static final String TYPE_LEAD_TYPE = "LEAD"; // 导联类型
    private static final String TYPE_DATA = "data"; // 数据
    private static final String TYPE_RECEIVER_ID = "receiverId"; // 接收者ID

    private volatile boolean stopped; // 是否已经停止广播
    private final String userId; // 用户ID
    private final String deviceId; // 设备ID
    private final int sampleRate; // 采样率
    private final int caliValue; // 标定值
    private final int leadTypeCode; // 导联类型码
    private final LinkedBlockingQueue<Integer> ecgBuffer;
    private final List<Short> hrBuffer; // 心率缓存
    private volatile boolean waiting; // 是否在等待数据响应
    private final List<Receiver> receivers; // 接收者列表
    private OnEcgHttpBroadcastListener listener; // 广播监听器

    public static class Receiver extends Account {
        private boolean isReceiving;

        Receiver(String platName, String platId) {
            super(platName, platId);
            isReceiving = false;
        }
        public boolean isReceiving() {
            return isReceiving;
        }
        public void setReceiving(boolean receiving) {
            isReceiving = receiving;
        }
    }

    public interface OnEcgHttpBroadcastListener {
        void onBroadcastInitialized(List<Receiver> receivers); // 初始化
        void onReceiverUpdated(); // 接收者更新
    }

    public EcgHttpBroadcast(String userId, String deviceId, int sampleRate, int caliValue, int leadTypeCode) {
        stopped = true;
        this.userId = userId;
        this.deviceId = deviceId;
        this.sampleRate = sampleRate;
        this.caliValue = caliValue;
        this.leadTypeCode = leadTypeCode;
        this.ecgBuffer = new LinkedBlockingQueue<>();
        this.hrBuffer = new ArrayList<>();
        this.waiting = false;
        receivers = new ArrayList<>();
    }

    public void removeListener() {
        listener = null;
    }
    public void setListener(OnEcgHttpBroadcastListener listener) {
        this.listener = listener;
    }

    /**
     * 启动广播，这里上传广播相关参数。服务器端接收后，如果允许广播，应该在Response中返回广播ID
     */
    public void start() {
        if(!stopped) return; // 不能重复启动
        receivers.clear();
        Map<String, String> data = new HashMap<>();
        data.put(TYPE_USER_ID, userId);
        data.put(TYPE_DEVICE_ID, deviceId);
        data.put(TYPE_SAMPLE_RATE, String.valueOf(sampleRate));
        data.put(TYPE_CALI_VALUE, String.valueOf(caliValue));
        data.put(TYPE_LEAD_TYPE, String.valueOf(leadTypeCode));
        HttpUtils.requestGet(upload_url, data, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "broadcast start fail.");
                stopped = true;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try(ResponseBody responseBody = response.body()) {
                    String responseStr = responseBody.string();
                    ViseLog.e("broadcast start: " + responseStr);
                    stopped = false;
                    if(listener != null) listener.onBroadcastInitialized(receivers);
                    getReceivers();
                }
            }
        });
    }

    /**
     * 停止广播
     */
    public void stop() {
        if(stopped) return;
        uncheckAllReceivers();
        stopped = true;
    }

    private void uncheckAllReceivers() {
        for(Receiver receiver : receivers) {
            if(receiver.isReceiving()) {
                uncheckReceiver(receiver);
            }
        }
    }

    /**
     * 发送心电信号
     * @param ecgSignal ：心电数据
     */
    public void sendEcgSignal(int ecgSignal) {
        if(stopped) return;
        ecgBuffer.add(ecgSignal);
        synchronized ((Boolean)waiting) {
            if(!waiting && ecgBuffer.size() >= sampleRate) {
                waiting = true;
                sendData();
            }
        }
    }

    /**
     * 发送心率值
     * @param hr ：心率值
     */
    public void sendHrValue(short hr) {
        if(stopped) return;
        hrBuffer.add(hr);
    }

    private void sendData() {
        Map<String, String> data = new HashMap<>();
        data.put(TYPE_USER_ID, userId);
        data.put(TYPE_DEVICE_ID,deviceId);

        StringBuilder sb = new StringBuilder();
        while (sb.length() < 1600) {
            Integer n = ecgBuffer.poll();
            if(n == null) break;
            sb.append(n).append(",");
        }
        String ecgStr = sb.toString();

        data.put(TYPE_DATA, HttpUtils.convertToString(hrBuffer)+ ";"+ ecgStr);
        hrBuffer.clear();
        HttpUtils.requestGet(upload_url, data, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ViseLog.e(e.getMessage());
                synchronized ((Boolean)waiting) {
                    waiting = false;
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try(ResponseBody responseBody = response.body()) {
                    String responseStr = responseBody.string();
                    ViseLog.e("send data: " + responseStr);
                    synchronized ((Boolean)waiting) {
                        waiting = false;
                    }
                }
            }
        });
    }

    /**
     * 添加一个可接收该广播的接收者
     * @param receiver ：接收者
     */
    public void checkReceiver(final Receiver receiver) {
        if(stopped) return;
        if(receiver == null || receiver.isReceiving()) return;

        Map<String, String> data = new HashMap<>();
        data.put(TYPE_USER_ID, userId);
        data.put(TYPE_DEVICE_ID, deviceId);
        data.put(TYPE_RECEIVER_ID, receiver.getPlatId());

        HttpUtils.requestGet(upload_url, data, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                notifyReceiverUpdated();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try(ResponseBody responseBody = response.body()) {
                    String responseStr = responseBody.string();
                    ViseLog.e("checkReceiver: " + responseStr);
                    receiver.setReceiving(true);
                    notifyReceiverUpdated();
                }
            }
        });
    }

    private void notifyReceiverUpdated() {
        if(listener != null) {
            listener.onReceiverUpdated();
        }
    }

    /**
     * 删除一个可接收该广播的接收者
     * @param receiver ：接收者
     */
    public void uncheckReceiver(final Receiver receiver) {
        if(stopped) return;
        if(receiver == null || !receiver.isReceiving()) return;

        Map<String, String> data = new HashMap<>();
        data.put(TYPE_USER_ID, userId);
        data.put(TYPE_DEVICE_ID, deviceId);
        data.put(TYPE_RECEIVER_ID, receiver.getPlatId());

        HttpUtils.requestGet(upload_url, data, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                notifyReceiverUpdated();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try(ResponseBody responseBody = response.body()) {
                    String responseStr = responseBody.string();
                    ViseLog.e("uncheckReceiver: " + responseStr);
                    receiver.setReceiving(false);
                    notifyReceiverUpdated();
                }

            }
        });
    }

    public void updateReceivers() {
        updateNewReceivers();
    }

    private void getReceivers() {
        if(stopped) return;
        Map<String, String> data = new HashMap<>();
        data.put(TYPE_USER_ID, userId);
        data.put(TYPE_DEVICE_ID, deviceId);
        HttpUtils.requestGet(getuser_url, data, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "getReceivers failure.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try(ResponseBody responseBody = response.body()) {
                    String responseStr = responseBody.string();
                    ViseLog.e("getReceivers success: " + responseStr);
                    receivers.addAll(parseReceivers(responseStr));
                    notifyReceiverUpdated();
                }
            }
        });
    }

    private void updateNewReceivers() {
        if(stopped) return;
        Map<String, String> data = new HashMap<>();
        data.put(TYPE_USER_ID, userId);
        data.put(TYPE_DEVICE_ID, deviceId);
        HttpUtils.requestGet(getuser_url, data, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "getReceivers failure.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try(ResponseBody responseBody = response.body()) {
                    String responseStr = responseBody.string();
                    ViseLog.e("getReceivers success: " + responseStr);
                    List<Receiver> newReceiver = parseReceivers(responseStr);
                    List<Receiver> deleteReceivers = new ArrayList<>();
                    for(Receiver receiver : receivers) {
                        if(!receiver.isReceiving && !newReceiver.contains(receiver))
                            deleteReceivers.add(receiver);
                    }
                    for(Receiver receiver : deleteReceivers) {
                        receivers.remove(receiver);
                    }
                    deleteReceivers.clear();
                    for(Receiver receiver : newReceiver) {
                        if(!receivers.contains(receiver)) {
                            receivers.add(receiver);
                        }
                    }
                    notifyReceiverUpdated();
                }
            }
        });
    }

    private static List<Receiver> parseReceivers(String jsonData) {
        List<Receiver> receivers = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String huaweiId = jsonObject.getString("open_id");
                Receiver receiver = new Receiver("HW", huaweiId);
                String name = jsonObject.getString("name");
                String displayName = jsonObject.getString("displayName");
                String description = jsonObject.getString("description");
                //receiver.setPlatId(huaweiId);
                if(name.equals("null")) receiver.setName(displayName); else receiver.setName(name);
                if(description.equals("null")) receiver.setNote(""); else receiver.setNote(description);
                receivers.add(receiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return receivers;
    }
}
