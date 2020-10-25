/*
 * * SignalScanView: ��ɨ��ķ�ʽ������ʾ��������ʾ�ź�
 * Ŀǰû�м����κ��˲�����
 * Wrote by chenm, BME, GDMC
 * 2013.09.25
 */
package com.cmtech.android.bledevice.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.cmtech.android.ble.utils.ExecutorUtil;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.util.FixSizeLinkedList;

import java.util.ArrayList;
import java.util.List;

import kotlin.collections.ArrayDeque;

/**
 * RollWaveView: 卷轴滚动式的波形显示视图
 * Created by bme on 2018/12/06.
 */

public abstract class RollWaveView extends WaveView {
    protected int dataNumInView; // X方向上一屏包含的数据点数

    protected List<Integer> viewData = new FixSizeLinkedList<>(1); //要显示的信号数据缓冲

    protected OnRollWaveViewListener listener;

    public RollWaveView(Context context) {
        super(context);
    }

    public RollWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    // 重置view
    // includeBackground: 是否重绘背景bitmap
    @Override
    public void resetView(boolean includeBackground)
    {
        super.resetView(includeBackground);

        setDataNumInView(viewWidth, pixelPerData);
    }

    @Override
    public void initForePaint() {
        forePaint.setAlpha(255);
        forePaint.setStyle(Paint.Style.STROKE);
        forePaint.setStrokeWidth(waveWidth);
        forePaint.setColor(waveColor);
    }

    public int getDataNumInView() {
        return dataNumInView;
    }

    public void setDataNumInView(int viewWidth, int pixelPerData) {
        dataNumInView = viewWidth/pixelPerData+1;
        viewData = new FixSizeLinkedList<>(dataNumInView);
    }

    public void clearData() {
        viewData.clear();
    }

    // 添加数据
    @Override
    public void addData(final int datum, boolean show) {
        viewData.add(datum);
        if(show) {
            drawDataOnForeCanvas();
            postInvalidate();
        }
    }

    // 添加数据
    @Override
    public synchronized void addData(List<Integer> data, boolean show) {
        viewData.addAll(data);
        if(show) {
            drawDataOnForeCanvas();
            postInvalidate();
        }
    }

    public void setListener(OnRollWaveViewListener listener) {
        this.listener = listener;
    }

    protected void drawDataOnForeCanvas()
    {
        foreCanvas.drawBitmap(backBitmap, 0, 0, null);

        if(viewData.size() <= 1) {
            return;
        }

        int beginPos =  dataNumInView - viewData.size();

        preX = initX + pixelPerData * beginPos;
        preY = initY - Math.round(viewData.get(0)/ valuePerPixel);
        Path path = new Path();
        path.moveTo(preX, preY);
        for(int i = 1; i < viewData.size(); i++) {
            preX += pixelPerData;
            preY = initY - Math.round(viewData.get(i)/ valuePerPixel);
            path.lineTo(preX, preY);
        }

        foreCanvas.drawPath(path, forePaint);
    }
}
