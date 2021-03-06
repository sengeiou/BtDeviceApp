/*
 * * SignalScanView: ��ɨ��ķ�ʽ������ʾ��������ʾ�ź�
 * Ŀǰû�м����κ��˲�����
 * Wrote by chenm, BME, GDMC
 * 2013.09.25
 */
package com.cmtech.android.bledeviceapp.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.cmtech.android.bledeviceapp.R;

import java.util.List;


public abstract class WaveView extends View {
    private static final int DEFAULT_SIZE = 100; // 缺省View的大小
    private static final int DEFAULT_PIXEL_PER_DATA = 2; // 缺省横向每个数据占的像素数
    private static final float DEFAULT_VALUE_PER_PIXEL = 1.0f; // 缺省纵向每个像素代表的数值
    public static final float DEFAULT_ZERO_LOCATION = 0.5f; // 缺省的零值位置在纵向的高度比
    private static final int DEFAULT_PIXEL_PER_GRID = 10; // 每个小栅格的像素个数
    private static final int SMALL_GRID_NUM_PER_LARGE_GRID = 5; // 每个大栅格包含多少个小栅格
    private static final int DEFAULT_BACKGROUND_COLOR = Color.BLACK; // 背景色
    private static final int DEFAULT_LARGE_GRID_LINE_COLOR = Color.RED; // 大栅格线颜色
    private static final int DEFAULT_SMALL_GRID_LINE_COLOR = Color.RED; // 小栅格线颜色
    private static final int DEFAULT_WAVE_COLOR = Color.YELLOW; // 波形颜色
    private static final int DEFAULT_ZERO_LINE_WIDTH = 4; // 零位置线宽
    private static final int DEFAULT_LARGE_GRID_LINE_WIDTH = 2; // 大栅格线宽
    private static final int DEFAULT_SMALL_GRID_LINE_WIDTH = 0; // 小栅格线宽，0代表头发丝风格
    private static final int DEFAULT_WAVE_WIDTH = 2; // 波形线宽

    protected int viewWidth = DEFAULT_SIZE; //视图宽度
    protected int viewHeight = DEFAULT_SIZE; //视图高度
    protected int initX, initY; //画图起始坐标
    protected int preX, preY; //画线的前一个点坐标
    protected final Paint wavePaint = new Paint(); // 波形画笔
    protected Bitmap backBitmap;  //背景bitmap

    private final boolean showGridLine; // 是否显示栅格线

    private final int bgColor; // 背景颜色
    private final int largeGridLineColor; // 大栅格线颜色
    private final int smallGridLineColor; // 小栅格线颜色
    private final int waveColor; // 波形颜色

    private final int zeroLineWidth; // 零线宽度
    private final int largeGridLineWidth; // 大栅格线宽
    private final int smallGridLineWidth; // 小栅格线宽
    private final int waveWidth = DEFAULT_WAVE_WIDTH; // 波形线宽

    protected int pixelPerGrid = DEFAULT_PIXEL_PER_GRID; // 每个栅格的像素个数
    protected int pixelPerData = DEFAULT_PIXEL_PER_DATA; //X方向分辨率，表示X方向每个数据点占多少个像素，pixel/data
    protected float valuePerPixel = DEFAULT_VALUE_PER_PIXEL; //Y方向分辨率，表示Y方向每个像素代表的信号值，value/pixel
    private float zeroLocation = DEFAULT_ZERO_LOCATION; //表示零值位置占视图高度的百分比

    protected OnWaveViewListener listener; // 监听器

    public WaveView(Context context) {
        super(context);

        showGridLine = true;
        bgColor = DEFAULT_BACKGROUND_COLOR;
        largeGridLineColor = DEFAULT_LARGE_GRID_LINE_COLOR;
        smallGridLineColor = DEFAULT_SMALL_GRID_LINE_COLOR;
        waveColor = DEFAULT_WAVE_COLOR;

        zeroLineWidth = DEFAULT_ZERO_LINE_WIDTH;
        largeGridLineWidth = DEFAULT_LARGE_GRID_LINE_WIDTH;
        smallGridLineWidth = DEFAULT_SMALL_GRID_LINE_WIDTH;
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.WaveView);
        showGridLine = styledAttributes.getBoolean(R.styleable.WaveView_show_grid_line, true);
        bgColor = styledAttributes.getColor(R.styleable.WaveView_background_color, DEFAULT_BACKGROUND_COLOR);
        largeGridLineColor = styledAttributes.getColor(R.styleable.WaveView_large_grid_line_color, DEFAULT_LARGE_GRID_LINE_COLOR);
        smallGridLineColor = styledAttributes.getColor(R.styleable.WaveView_small_grid_line_color, DEFAULT_SMALL_GRID_LINE_COLOR);
        waveColor = styledAttributes.getColor(R.styleable.WaveView_wave_color, DEFAULT_WAVE_COLOR);

        zeroLineWidth = styledAttributes.getInt(R.styleable.WaveView_zero_line_width, DEFAULT_ZERO_LINE_WIDTH);
        largeGridLineWidth = styledAttributes.getInt(R.styleable.WaveView_large_grid_line_width, DEFAULT_LARGE_GRID_LINE_WIDTH);
        smallGridLineWidth = styledAttributes.getInt(R.styleable.WaveView_small_grid_line_width, DEFAULT_SMALL_GRID_LINE_WIDTH);
        styledAttributes.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = getWidth();
        viewHeight = getHeight();
        resetView(true);
    }

    public int getPixelPerData() {
        return pixelPerData;
    }

    public void setPixelPerGrid(int pixelPerGrid) {
        this.pixelPerGrid = pixelPerGrid;
    }

    // 设置分辨率
    public void setResolution(int pixelPerData, float valuePerPixel)
    {
        if((pixelPerData < 1) || (valuePerPixel < 0)) {
            throw new IllegalArgumentException();
        }
        this.pixelPerData = pixelPerData;
        this.valuePerPixel = valuePerPixel;
    }

    // 设置零值位置
    public void setZeroLocation(float zeroLocation)
    {
        this.zeroLocation = zeroLocation;
        initY = (int)(viewHeight * this.zeroLocation);
    }

    public void setListener(OnWaveViewListener listener) {
        this.listener = listener;
    }

    // 重置view
    // includeBackground: 是否重绘背景bitmap
    public void resetView(boolean includeBackground)
    {
        initX = 0;
        initY = (int)(viewHeight * zeroLocation);

        //重新创建背景Bitmap
        if(includeBackground) {
            backBitmap = createBackBitmap();
            setBackground(new BitmapDrawable(getResources(), backBitmap));
        }

        // 初始化画图起始位置
        preX = initX;
        preY = initY;

        initWavePaint();

        postInvalidate();
    }

    // 初始化波形画笔
    public void initWavePaint() {
        wavePaint.setAlpha(255);
        wavePaint.setStrokeWidth(waveWidth);
        wavePaint.setColor(waveColor);
    }

    // 开始显示
    public abstract void startShow();

    // 停止显示
    public abstract void stopShow();

    // 添加单个数据
    public abstract void addData(final int datum, boolean show);

    // 添加数据
    public abstract void addData(final List<Integer> data, boolean show);

    //创建背景Bitmap
    private Bitmap createBackBitmap()
    {
        Bitmap backBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Config.ARGB_8888);
        Canvas backCanvas = new Canvas(backBitmap);
        backCanvas.drawColor(bgColor);

        if(!showGridLine) return null;

        Paint paint = new Paint();

        // 画零位线
        setPaint(paint, largeGridLineColor, zeroLineWidth);
        backCanvas.drawLine(initX, initY, initX + viewWidth, initY, paint);

        // 画水平线
        int deltaY;
        for(int drawed = 0; drawed < 2; drawed++ ) {
            if(drawed == 0) { // 零线以上
                deltaY = -pixelPerGrid;
            } else { // 零线以下
                deltaY = pixelPerGrid;
            }

            setPaint(paint, smallGridLineColor, smallGridLineWidth);
            int y = initY + deltaY;
            int n = 1;
            while((drawed == 0 && y >= 0) || (drawed == 1 && y <= viewHeight) ) {
                backCanvas.drawLine(initX, y, initX + viewWidth, y, paint);
                y += deltaY;
                if(++n == SMALL_GRID_NUM_PER_LARGE_GRID) {
                    setPaint(paint, largeGridLineColor, largeGridLineWidth);
                    n = 0;
                }
                else {
                    setPaint(paint, smallGridLineColor, smallGridLineWidth);
                }
            }
        }

        // 画垂直线
        setPaint(paint, largeGridLineColor, largeGridLineWidth);
        backCanvas.drawLine(initX, 0, initX, viewHeight, paint);
        setPaint(paint, smallGridLineColor, smallGridLineWidth);

        int x = initX + pixelPerGrid;
        int n = 1;
        while(x <= viewWidth) {
            backCanvas.drawLine(x, 0, x, viewHeight, paint);
            x += pixelPerGrid;
            if(++n == SMALL_GRID_NUM_PER_LARGE_GRID) {
                setPaint(paint, largeGridLineColor, largeGridLineWidth);
                n = 0;
            }
            else {
                setPaint(paint, smallGridLineColor, smallGridLineWidth);
            }
        }

        // 画定标脉冲
        /*mainPaint.setStrokeWidth(2);
		mainPaint.setColor(Color.BLACK);
		c.drawLine(0, initY, 2*pixelPerGrid, initY, mainPaint);
		c.drawLine(2*pixelPerGrid, initY, 2*pixelPerGrid, initY-10*pixelPerGrid, mainPaint);
		c.drawLine(2*pixelPerGrid, initY-10*pixelPerGrid, 7*pixelPerGrid, initY-10*pixelPerGrid, mainPaint);
        c.drawLine(7*pixelPerGrid, initY-10*pixelPerGrid, 7*pixelPerGrid, initY, mainPaint);
        c.drawLine(7*pixelPerGrid, initY, 10*pixelPerGrid, initY, mainPaint);*/

        //mainPaint.setColor(waveColor);
        //mainPaint.setStrokeWidth(2);

        return backBitmap;
    }

    private void setPaint(Paint paint, int color, int lineWidth) {
        paint.setColor(color);
        paint.setStrokeWidth(lineWidth);
    }
}
