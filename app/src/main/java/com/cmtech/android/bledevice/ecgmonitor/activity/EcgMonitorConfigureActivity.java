package com.cmtech.android.bledevice.ecgmonitor.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.cmtech.android.bledeviceapp.R;

public class EcgMonitorConfigureActivity extends AppCompatActivity {
    private static final String TAG = "EcgMonitorConfigureActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgmonitor_configure);


    }
}