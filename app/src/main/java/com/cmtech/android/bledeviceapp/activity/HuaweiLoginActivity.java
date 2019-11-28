package com.cmtech.android.bledeviceapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.util.HttpUtils;
import com.vise.log.ViseLog;

public class HuaweiLoginActivity extends AppCompatActivity {
    private static final String KEY_PHONE = "phone";
    private static final String KEY_TIME = "time";

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_huwei_login);
        final WebView webView = findViewById(R.id.wvLoginHuawei);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.requestFocus();
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains("open_id=")) {
                    ViseLog.e("Huawei login response: " + url);
                    String huaweiId = HttpUtils.parseUrl(url).get("open_id");
                    String name = HttpUtils.parseUrl(url).get("displayName");
                    signIn(huaweiId, name, false);
                    Intent intent = new Intent(HuaweiLoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                return false;
            }

            @Deprecated
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                Toast.makeText(HuaweiLoginActivity.this, view.getUrl() + "----" + description, Toast.LENGTH_LONG).show();
                //Intent intent = new Intent(HuaweiLoginActivity.this, MainActivity.class);
                //startActivity(intent);
                finish();
            }

        });
        String url = "http://huawei.tighoo.com/home/login";
        webView.loadUrl(url);
    }

    // 登录
    private void signIn(String phone, String name, boolean isSaveLoginInfo) {
        AccountManager manager = AccountManager.getInstance();
        if(manager.signIn(phone) || manager.signUp(phone, name)) {
            Intent intent = new Intent(HuaweiLoginActivity.this, HuaweiLoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(HuaweiLoginActivity.this, "登录错误。", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
