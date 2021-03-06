package com.cmtech.android.bledevice.ecg.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cmtech.android.bledevice.ecg.adapter.EcgCommentAdapter;
import com.cmtech.android.bledevice.ecg.process.hr.HrStatisticsInfo;
import com.cmtech.android.bledevice.ecg.record.EcgRecord;
import com.cmtech.android.bledevice.ecg.record.ecgcomment.EcgNormalComment;
import com.cmtech.android.bledevice.ecg.view.EcgHrHistogramChart;
import com.cmtech.android.bledevice.ecg.view.EcgHrLineChart;
import com.cmtech.android.bledeviceapp.view.OnRollWaveViewListener;
import com.cmtech.android.bledeviceapp.view.RollRecordView;
import com.cmtech.android.bledeviceapp.view.RollWaveView;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.cmtech.android.bledevice.ecg.process.signal.EcgSignalProcessor.HR_FILTER_SECOND;
import static com.cmtech.android.bledevice.ecg.process.signal.EcgSignalProcessor.HR_HISTOGRAM_BAR_NUM;

public class EcgRecordActivity extends AppCompatActivity implements OnRollWaveViewListener, EcgCommentAdapter.OnEcgCommentListener{
    private EcgRecord record;
    private long modifyTime;

    private TextView tvModifyTime; // 更新时间
    private TextView tvCreator; // 创建人
    private TextView tvCreateTime; // 创建时间
    private TextView tvLength; // 信号长度
    private TextView tvHrNum; // 心率次数

    private LinearLayout signalLayout;
    private RollRecordView signalView; // signalView
    private TextView tvTotalTime; // 总时长
    private TextView tvCurrentTime; // 当前播放信号的时刻
    private SeekBar sbReplay; // 播放条
    private ImageButton btnReplayCtrl; // 转换播放状态
    private EcgCommentAdapter commentAdapter; // 留言Adapter
    private RecyclerView rvComments; // 留言RecycleView

    private TextView tvAverageHr; // 平均心率
    private TextView tvMaxHr; // 最大心率
    private EcgHrLineChart hrLineChart; // 心率折线图
    private EcgHrHistogramChart hrHistChart; // 心率直方图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgrecord);

        int recordId = getIntent().getIntExtra("record_id", -1);
        try {
            record = LitePal.find(EcgRecord.class, recordId, true);
            ViseLog.e(record);
            if(record == null) {
                setResult(RESULT_CANCELED);
                finish();
            }
            record.openSigFile();
        } catch (IOException e) {
            e.printStackTrace();
            setResult(RESULT_CANCELED);
            finish();
        }

        modifyTime = record.getModifyTime();

        tvModifyTime = findViewById(R.id.tv_modify_time);
        tvCreator = findViewById(R.id.tv_creator);
        tvCreateTime = findViewById(R.id.tv_create_time);
        tvLength = findViewById(R.id.tv_signal_length);
        tvHrNum = findViewById(R.id.tv_time_length);

        signalLayout = findViewById(R.id.layout_signal_part);
        signalView = findViewById(R.id.roll_ecg_view);
        signalView.setListener(this);
        rvComments = findViewById(R.id.rv_comment_list);
        LinearLayoutManager commentLayoutManager = new LinearLayoutManager(this);
        commentLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvComments.setLayoutManager(commentLayoutManager);
        rvComments.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        commentAdapter = new EcgCommentAdapter(record.getCommentList(), this);
        rvComments.setAdapter(commentAdapter);

        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        btnReplayCtrl = findViewById(R.id.ib_replay_control);
        btnReplayCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(signalView.isShowing()) {
                    signalView.stopShow();
                } else {
                    signalView.startShow();
                }
            }
        });
        sbReplay = findViewById(R.id.sb_replay);
        sbReplay.setEnabled(false);
        sbReplay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    signalView.showAtSecond(i);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        hrHistChart = findViewById(R.id.chart_hr_histogram);
        hrLineChart = findViewById(R.id.line_chart);
        tvAverageHr = findViewById(R.id.tv_hr_ave_value);
        tvMaxHr = findViewById(R.id.tv_hr_max_value);

        initialize();
    }

    private void initialize() {
        tvModifyTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterday(modifyTime));

        Account fileCreator = record.getCreator();
        Account account = MyApplication.getAccount();
        if(fileCreator.equals(account)) {
            tvCreator.setText(Html.fromHtml("<u>您本人</u>"));
        } else {
            tvCreator.setText(Html.fromHtml("<u>" + record.getCreatorName() + "</u>"));
        }

        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
        tvCreateTime.setText(createTime);

        int second = record.getDataNum()/ record.getSampleRate();
        if(record.getDataNum() == 0) {
            tvLength.setText("无");
        } else {
            String timeLength = DateTimeUtil.secToTimeInChinese(second);
            tvLength.setText(timeLength);
        }

        if(record.getHrList() == null)
            tvHrNum.setText(String.valueOf(0));
        else
            tvHrNum.setText(String.valueOf(record.getHrList().size()));

        initEcgView(record);

        tvCurrentTime.setText(DateTimeUtil.secToTime(0));
        tvTotalTime.setText(DateTimeUtil.secToTime(second));
        sbReplay.setMax(second);

        /*List<EcgNormalComment> commentList = getCommentListInRecord(record);
        commentAdapter.updateCommentList(commentList);
        if(commentList.size() > 0)
            rvComments.smoothScrollToPosition(0);*/

        HrStatisticsInfo hrStatisticsInfo = new HrStatisticsInfo(record.getHrList(), HR_FILTER_SECOND);
        tvAverageHr.setText(String.valueOf(hrStatisticsInfo.getAverageHr()));
        tvMaxHr.setText(String.valueOf(hrStatisticsInfo.getMaxHr()));
        hrLineChart.showLineChart(hrStatisticsInfo.getFilteredHrList(), "心率时序图", Color.BLUE);
        hrHistChart.update(hrStatisticsInfo.getNormHistogram(HR_HISTOGRAM_BAR_NUM));

        if(record.getDataNum() == 0) {
            signalLayout.setVisibility(View.GONE);
        } else {
            signalLayout.setVisibility(View.VISIBLE);
            signalView.startShow();
        }
    }

    private void initEcgView(EcgRecord ecgRecord) {
        if(ecgRecord == null) return;
        signalView.setRecord(ecgRecord);
        signalView.setZeroLocation(RollWaveView.DEFAULT_ZERO_LOCATION);
    }

    // 获取选中记录的留言列表，如果没有当前账户的留言，就加入一条当前账户的留言
    private List<EcgNormalComment> getCommentListInRecord(EcgRecord ecgRecord) {
        if(ecgRecord == null)
            return new ArrayList<>();
        else {
            Account account = MyApplication.getAccount();
            boolean found = false;
            for(EcgNormalComment comment : ecgRecord.getCommentList()) {
                if(comment.getCreator().equals(account)) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                ecgRecord.addComment(EcgNormalComment.create());
            }
            return ecgRecord.getCommentList();
        }
    }

    @Override
    public void onShowStateUpdated(boolean show) {
        if(show) {
            btnReplayCtrl.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_pause_32px));
        } else {
            btnReplayCtrl.setImageDrawable(ContextCompat.getDrawable(MyApplication.getContext(), R.mipmap.ic_play_32px));
        }
        sbReplay.setEnabled(!show);
    }

    @Override
    public void onDataLocationUpdated(long location, int second) {
        tvCurrentTime.setText(DateTimeUtil.secToTime(second));
        sbReplay.setProgress(second);
    }

    @Override
    public void onCommentSaved(EcgNormalComment comment) {
        modifyTime = comment.getModifyTime();
        tvModifyTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterday(modifyTime));
    }

    @Override
    public void onCommentDeleted(EcgNormalComment comment) {
        /*signalView.stopShow();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除留言").setMessage("确定删除该留言吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //fileReplayModel.deleteComment(appendix);
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).show();*/
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("updated", modifyTime != record.getModifyTime());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        signalView.stopShow();
        try {
            record.closeSigFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
