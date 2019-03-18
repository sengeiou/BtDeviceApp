package com.cmtech.android.bledevice.ecgmonitor.model;

import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgAppendix;

public interface IEcgAppendixOperator {
    void deleteAppendix(EcgAppendix appendix); // 删除一条附加信息
    void saveAppendix(); // 保存留言
}