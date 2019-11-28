package com.cmtech.android.bledeviceapp.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UserUtil {
    private static final String getuser_url = "http://huawei.tighoo.com/home/GetUsers?";

    public interface IGetUserInfoCallback {
        void onReceived(String userId, String name, String description, String image);
    }

    public interface ISaveUserInfoCallback {
        void onReceived(boolean success);
    }

    private static class User {
        private String id; // 用户的华为Id
        private String name; // 用户名
        private String description; // 用户简介
        private String imageBase64Str; // 用户头像URL
    }

    public static void getUserInfo(final String userId, final IGetUserInfoCallback callback) {
        Map<String, String> data = new HashMap<>();
        data.put("user_id", userId);
        String url = getuser_url + convertString(data);
        HttpUtils.upload(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(callback != null) {
                    callback.onReceived(null, null, null, null);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                User user = parseUser(responseStr);
                if(callback != null) {
                    if(user != null) {
                        callback.onReceived(user.id, user.name, user.description, user.imageBase64Str);
                    }else {
                        callback.onReceived(null, null, null, null);
                    }
                }
            }
        });
    }

    public static void saveUserInfo(final String id, String name, String description, Bitmap image, final ISaveUserInfoCallback callback) {
        Map<String, String> data = new HashMap<>();
        data.put("user_id", id);
        data.put("user_name", name);
        data.put("user_description", description);
        data.put("user_image", bitmapToString(image));
        String url = getuser_url + convertString(data);
        HttpUtils.upload(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(callback != null) {
                    callback.onReceived(false);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                if(callback != null) {
                    callback.onReceived(true);
                }
            }
        });
    }

    private static User parseUser(String jsonData) {
        User user = new User();
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            user.id = jsonObject.getString("user_id");
            user.name = jsonObject.getString("user_name");
            user.description = jsonObject.getString("user_description");
            user.imageBase64Str = jsonObject.getString("user_image");
        } catch (Exception e) {
            e.printStackTrace();
            user = null;
        }
        return user;
    }

    /**
     * 把bitmap转换成Base64编码String
     */
    private static String bitmapToString(Bitmap bitmap) {
        return Base64.encodeToString(bitmapToByte(bitmap), Base64.DEFAULT);
    }

    /**
     * convert Bitmap to byte array
     */
    private static byte[] bitmapToByte(Bitmap b) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, o);
        return o.toByteArray();
    }

    /**
     * convert byte array to Bitmap
     */
    public static Bitmap byteToBitmap(byte[] b) {
        return (b == null || b.length == 0) ? null : BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    private static String convertString(Map<String, String> data) {
        if (data == null || data.isEmpty()) return "";

        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (Map.Entry entry : data.entrySet()) {
            if (isFirst) {
                isFirst = false;
            } else {
                builder.append("&");
            }
            builder.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return builder.toString();
    }
}
