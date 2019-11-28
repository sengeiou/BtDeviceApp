package com.cmtech.android.bledeviceapp.model;

import com.cmtech.android.bledeviceapp.util.DataIOUtil;

import org.litepal.crud.LitePalSupport;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

/**
  *
  * ClassName:      Account
  * Description:    用户类
  * Author:         chenm
  * CreateDate:     2018/10/27 上午3:57
  * UpdateUser:     chenm
  * UpdateDate:     2019/4/20 上午3:57
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class Account extends LitePalSupport implements Serializable, Cloneable{
    private static final int HUAWEI_ID_CHAR_LEN = 255;
    private static final int NAME_CHAR_LEN = 10;
    private static final int DESCRIPTION_CHAR_LEN = 50;

    private int id; // id
    private String huaweiId = ""; // 华为ID
    private String name = ""; // 名称
    private String imagePath = ""; // 头像文件路径
    private String description = ""; // 个人信息

    public Account() {
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getHuaweiId() {
        return huaweiId;
    }
    public String getShortHuaweiId() {
        if(huaweiId.length() > 3) {
            return String.format("%s****%s", huaweiId.substring(0, 3), huaweiId.substring(huaweiId.length() - 3));
        } else
            return huaweiId;
    }
    public void setHuaweiId(String huaweiId) {
        this.huaweiId = huaweiId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getImagePath() {
        return imagePath;
    }
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public boolean readFromStream(DataInput in) throws IOException{
        huaweiId = DataIOUtil.readFixedString(in, HUAWEI_ID_CHAR_LEN);
        name = DataIOUtil.readFixedString(in, NAME_CHAR_LEN);
        description = DataIOUtil.readFixedString(in, DESCRIPTION_CHAR_LEN);
        return true;
    }

    public boolean writeToStream(DataOutput out) throws IOException{
        DataIOUtil.writeFixedString(out, huaweiId, HUAWEI_ID_CHAR_LEN);
        DataIOUtil.writeFixedString(out, name, NAME_CHAR_LEN);
        DataIOUtil.writeFixedString(out, description, DESCRIPTION_CHAR_LEN);
        return true;
    }

    public int length() {
        return (HUAWEI_ID_CHAR_LEN + NAME_CHAR_LEN + DESCRIPTION_CHAR_LEN)*2;
    }

    @Override
    public String toString() {
        return "HuaweiId: " + getShortHuaweiId() + " Name：" + name + ' ' + " Personal Info：" + description;
    }

    @Override
    public int hashCode() {
        return huaweiId.hashCode();
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(!(otherObject instanceof Account)) return false;
        Account other = (Account) otherObject;
        return huaweiId.equals(other.huaweiId);
    }

    @Override
    public Object clone() {
        Account account = new Account();
        account.huaweiId = huaweiId;
        account.name = name;
        account.imagePath = imagePath;
        account.description = description;
        return account;
    }

}
