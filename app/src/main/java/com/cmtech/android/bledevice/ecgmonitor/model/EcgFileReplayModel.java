package com.cmtech.android.bledevice.ecgmonitor.model;


import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgComment;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledeviceapp.model.UserAccountManager;
import com.cmtech.bmefile.BmeFileHead30;
import com.cmtech.bmefile.exception.FileException;

import java.util.Date;
import java.util.List;

public class EcgFileReplayModel {
    private EcgFile ecgFile;            // 播放的EcgFile

    private boolean updated = false;            // 文件是否已更新
    public boolean isUpdated() { return updated; }

    // 文件播放观察者
    private IEcgFileReplayObserver observer;

    // 用于设置EcgWaveView的参数
    private int viewGridWidth = 10;               // 设置ECG View中的每小格有10个像素点
    // 下面两个参数可用来计算View中的xRes和yRes
    private float viewXGridTime = 0.04f;          // 设置ECG View中的横向每小格代表0.04秒，即25格/s，这是标准的ECG走纸速度
    private float viewYGridmV = 0.1f;             // 设置ECG View中的纵向每小格代表0.1mV

    private final int totalSecond;                   // 信号总的秒数
    public int getTotalSecond() {
        return totalSecond;
    }

    private int currentSecond = -1;                 // 记录当前播放的Ecg的秒数
    public void setCurrentSecond(int currentSecond) {
        this.currentSecond = currentSecond;
    }

    private int secondWhenComment = -1;        // 留言时间

    private boolean showSecondInComment = false;       // 是否在留言中加入时间定位
    public boolean isShowSecondInComment() {
        return showSecondInComment;
    }
    public void setShowSecondInComment(boolean showSecondInComment) {
        this.showSecondInComment = showSecondInComment;
        if(showSecondInComment) {
            secondWhenComment = currentSecond;
        }
        if(observer != null) {
            observer.updateShowSecondInComment(showSecondInComment, secondWhenComment);
        }
    }

    public EcgFileReplayModel(String ecgFileName) throws FileException{
        ecgFile = EcgFile.openBmeFile(ecgFileName);
        totalSecond = ecgFile.getDataNum()/ecgFile.getFs();
    }

    public EcgFile getEcgFile() {
        return ecgFile;
    }

    public List<EcgComment> getCommentList() {
        return ecgFile.getCommentList();
    }

    // 播放初始化
    public void initReplay() {
        initEcgView();
    }

    // 添加一个留言
    public void addComment(String comment) {
        if(showSecondInComment) {
            addComment(secondWhenComment, comment);
            showSecondInComment = false;
            if(observer != null) {
                observer.updateShowSecondInComment(false, -1);
            }
        }
        else
            addComment(-1, comment);
    }

    // 添加一个有时间定位的留言
    private void addComment(int secondInEcg, String comment) {
        String commentator = UserAccountManager.getInstance().getUserAccount().getUserName();
        long timeCreated = new Date().getTime();
        ecgFile.addComment(new EcgComment(commentator, timeCreated, secondInEcg, comment));
        updated = true;
        updateCommentList();
    }

    public void deleteComment(EcgComment comment) {
        ecgFile.deleteComment(comment);
        updated = true;
        updateCommentList();
    }


    public void close() {
        try {
            ecgFile.close();
        } catch (FileException e) {
            e.printStackTrace();
        }
    }

    // 登记心电回放观察者
    public void registerEcgFileReplayObserver(IEcgFileReplayObserver observer) {
        this.observer = observer;
    }

    // 删除心电回放观察者
    public void removeEcgFileObserver() {
        observer = null;
    }

    private void updateCommentList() {
        if(observer != null) {
            observer.updateCommentList();
        }
    }

    private void initEcgView() {
        if(observer != null) {
            int sampleRate = ecgFile.getFs();
            int value1mV = ((BmeFileHead30)ecgFile.getBmeFileHead()).getCalibrationValue();
            int xRes = Math.round(viewGridWidth / (viewXGridTime * sampleRate));   // 计算横向分辨率
            float yRes = value1mV * viewYGridmV / viewGridWidth;                     // 计算纵向分辨率
            observer.initEcgView(xRes, yRes, viewGridWidth, 0.5);
        }
    }
}