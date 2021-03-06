package com.cmtech.android.bledeviceapp.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.model.PhoneAccount;
import com.mob.MobSDK;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

import static com.cmtech.android.bledeviceapp.global.AppConstant.PHONE_PLAT_NAME;

public class LoginActivity extends AppCompatActivity {
    private ImageButton qqLogin;
    private ImageButton wxLogin;
    private ImageButton phoneLogin;
    private CheckBox cbGrant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*if(true) {
            //loginUsingPhone(this);
            login(PHONE_PLAT_NAME, "8615019187404", "", "");
            return;
        }*/

        Account phoneAccount = PhoneAccount.getAccount();
        if(phoneAccount != null) {
            login(PHONE_PLAT_NAME, phoneAccount.getPlatId(), phoneAccount.getName(), phoneAccount.getIcon());
            return;
        }

        /*Platform plat = ShareSDK.getPlatform(QQ.NAME);
        if(plat.isAuthValid()) {
            loginUsingQQorWechat(plat);
            return;
        }
        plat = ShareSDK.getPlatform(Wechat.NAME);
        if(plat.isAuthValid()) {
            loginUsingQQorWechat(plat);
            return;
        }*/

        qqLogin = findViewById(R.id.ib_qq_login);
        qqLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPrivacyGrantChecked()) return;
                Platform plat = ShareSDK.getPlatform(QQ.NAME);
                loginUsingQQorWechat(plat);
            }
        });

        wxLogin = findViewById(R.id.ib_wechat_login);
        wxLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPrivacyGrantChecked()) return;
                Platform plat = ShareSDK.getPlatform(Wechat.NAME);
                loginUsingQQorWechat(plat);
            }
        });

        phoneLogin = findViewById(R.id.ib_phone_login);
        phoneLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPrivacyGrantChecked()) return;
                loginUsingPhone(LoginActivity.this);
            }
        });

        cbGrant = findViewById(R.id.cb_privacy_grant);

        TextView tvPrivacy = findViewById(R.id.tv_privacy);
        tvPrivacy.setMovementMethod(LinkMovementMethod.getInstance());

        TextView tvAgreement = findViewById(R.id.tv_agreement);
        tvAgreement.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private boolean isPrivacyGrantChecked() {
        boolean granted = cbGrant.isChecked();
        if(granted) {
            MobSDK.submitPolicyGrantResult(granted, null);
        } else {
            Toast.makeText(this, R.string.pls_check_privacy, Toast.LENGTH_SHORT).show();
        }
        return granted;
    }

    @Override
    public void onBackPressed() {

    }

    private void loginUsingQQorWechat(Platform plat) {
        final String platName = plat.getName();
        ShareSDK.setActivity(LoginActivity.this);
        if (plat.isAuthValid()) {
            String platId = plat.getDb().getUserId();
            String username = plat.getDb().getUserName();
            String icon = plat.getDb().getUserIcon();
            login(platName, platId, username, icon);
        } else {
            //授权回调监听，监听oncomplete，onerror，oncancel三种状态
            plat.setPlatformActionListener(new PlatformActionListener() {
                @Override
                public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                    String platId = platform.getDb().getUserId();
                    String userName = platform.getDb().getUserName();
                    String icon = platform.getDb().getUserIcon();
                    login(platName, platId, userName, icon);
                }

                @Override
                public void onError(Platform platform, int i, Throwable throwable) {
                    Toast.makeText(LoginActivity.this, MyApplication.getStr(R.string.login_failure), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancel(Platform platform, int i) {

                }
            });
            //单独授权，OnComplete返回的hashmap是空的
            plat.authorize();
        }
    }

    private void loginUsingPhone(Context context) {
        RegisterPage page = new RegisterPage();
        page.setTempCode(null);
        page.setRegisterCallback(new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                if (result == SMSSDK.RESULT_COMPLETE) {
                    HashMap<String,Object> phoneMap = (HashMap<String, Object>) data;
                    String country = (String) phoneMap.get("country");
                    String phone = (String) phoneMap.get("phone");
                    String platId = country+phone;
                    login(PHONE_PLAT_NAME, platId, "", "");
                } else{
                    Toast.makeText(LoginActivity.this, MyApplication.getStr(R.string.login_failure), Toast.LENGTH_SHORT).show();
                }
            }
        });
        page.show(context);
    }

    private void login(String platName, String platId, String name, String icon) {
        MyApplication.getAccountManager().localLogin(platName, platId, name, icon);
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

        MyApplication.getAccountManager().webLogin(this);
    }

}
