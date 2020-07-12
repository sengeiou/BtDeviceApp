package com.cmtech.android.bledeviceapp.model;


import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.vise.log.ViseLog;
import com.vise.utils.file.FileUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.cmtech.android.bledevice.record.RecordWebAsyncTask.CODE_SUCCESS;
import static com.cmtech.android.bledeviceapp.AppConstant.PHONE_PLAT_NAME;
import static com.cmtech.android.bledeviceapp.AppConstant.QQ_PLAT_NAME;
import static com.cmtech.android.bledeviceapp.AppConstant.WX_PLAT_NAME;
import static com.vise.utils.handler.HandlerUtil.runOnUiThread;

/**
  *
  * ClassName:      AccountManager
  * Description:    账户管理器
  * Author:         chenm
  * CreateDate:     2018/10/27 上午4:01
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/20 上午4:01
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class AccountManager {
    private static User account; // account

    private AccountManager() {
    }

    public static User getAccount() {
        return account;
    }

    // login account
    public static void login(String platName, String platId, String name, String icon) {
        User account = LitePal.where("platName = ? and platId = ?", platName, platId).findFirst(User.class);
        if(account == null) {
            account = new User(platName, platId, name, "", icon);
        } else {
            account.setName(name);
            account.setIcon(icon);
        }
        account.save();
        AccountManager.account = account;
    }

    public static void webLogin(final Context context) {
        if(!isLogin()) return;

        KMWebService.signUporLogin(account.getPlatName(), account.getPlatId(), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, R.string.web_failure, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.body() == null) return;
                String respBody = response.body().string();
                try {
                    JSONObject json = new JSONObject(respBody);
                    int code = json.getInt("code");
                    if(code != CODE_SUCCESS) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, R.string.login_failure, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    ViseLog.e("login/sign up:"+code);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // logout account
    public static void logout(boolean isRemoved) {
        if(!isLogin()) return;

        if(isRemoved) {
            if(account.getPlatName().equals(QQ_PLAT_NAME)) {
                Platform plat = ShareSDK.getPlatform(QQ.NAME);
                plat.removeAccount(true);
            } else if(account.getPlatName().equals(WX_PLAT_NAME)) {
                Platform plat = ShareSDK.getPlatform(Wechat.NAME);
                plat.removeAccount(true);
            } else if(account.getPlatName().equals(PHONE_PLAT_NAME)) {
                PhoneAccount.removeAccount();
            }

            if(!TextUtils.isEmpty(account.getIcon())) {
                try {
                    FileUtil.deleteFile(new File(account.getIcon()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        account = null;
    }

    // is a valid account login
    public static boolean isLogin() {
        return account != null;
    }
}
