package com.cmtech.android.bledevice.ecg.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.ecg.activity.EcgRecordExplorerActivity;
import com.cmtech.android.bledevice.ecg.record.EcgRecord;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;
import com.vise.log.ViseLog;

import java.util.List;


/**
  *
  * ClassName:      EcgRecordListAdapter
  * Description:    Ecg记录列表Adapter
  * Author:         chenm
  * CreateDate:     2018/11/10 下午4:09
  * UpdateUser:     chenm
  * UpdateDate:     2018/11/10 下午4:09
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class EcgRecordListAdapter extends RecyclerView.Adapter<EcgRecordListAdapter.ViewHolder>{
    private final EcgRecordExplorerActivity activity;
    private final List<EcgRecord> allRecordList;
    private final List<EcgRecord> updatedRecordList;
    private EcgRecord selectedRecord;
    private Drawable defaultBackground; // 缺省背景

    class ViewHolder extends RecyclerView.ViewHolder {
        View fileView;

        TextView tvModifyTime; // 更新时间
        TextView tvCreator; // 创建人
        TextView tvCreateTime; // 创建时间
        TextView tvLength; // 信号长度
        TextView tvHrNum; // 心率次数

        ViewHolder(View itemView) {
            super(itemView);
            fileView = itemView;
            tvModifyTime = fileView.findViewById(R.id.tv_modify_time);
            tvCreator = fileView.findViewById(R.id.tv_creator);
            tvCreateTime = fileView.findViewById(R.id.tv_create_time);
            tvLength = fileView.findViewById(R.id.tv_signal_length);
            tvHrNum = fileView.findViewById(R.id.tv_time_length);
        }
    }

    public EcgRecordListAdapter(EcgRecordExplorerActivity activity, List<EcgRecord> allRecordList, List<EcgRecord> updatedRecordList, EcgRecord selectedRecord) {
        this.activity = activity;
        this.allRecordList = allRecordList;
        this.updatedRecordList = updatedRecordList;
        this.selectedRecord = selectedRecord;
    }

    @NonNull
    @Override
    public EcgRecordListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_ecgrecord, parent, false);

        final EcgRecordListAdapter.ViewHolder holder = new EcgRecordListAdapter.ViewHolder(view);

        defaultBackground = holder.fileView.getBackground();
        holder.fileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.selectRecord(allRecordList.get(holder.getAdapterPosition()));
            }
        });
        holder.tvCreator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MyApplication.getContext(), allRecordList.get(holder.getAdapterPosition()).getCreator().toString(), Toast.LENGTH_SHORT).show();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull EcgRecordListAdapter.ViewHolder holder, final int position) {
        ViseLog.e("onBindViewHolder " + position);
        EcgRecord record = allRecordList.get(position);
        if(record == null) return;

        holder.tvModifyTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterday(record.getModifyTime()));

        Account creator = record.getCreator();
        Account account = MyApplication.getAccount();
        if(account.equals(creator)) {
            holder.tvCreator.setText(Html.fromHtml("<u>您本人</u>"));
        } else {
            holder.tvCreator.setText(Html.fromHtml("<u>" + creator.getName() + "</u>"));
        }

        String createTime = DateTimeUtil.timeToShortStringWithTodayYesterday(record.getCreateTime());
        holder.tvCreateTime.setText(createTime);

        if(record.getDataNum() == 0) {
            holder.tvLength.setText("无");
        } else {
            String dataTimeLength = DateTimeUtil.secToTimeInChinese(record.getDataNum() / record.getSampleRate());
            holder.tvLength.setText(dataTimeLength);
        }

        if(record.getHrList() == null)
            holder.tvHrNum.setText(String.valueOf(0));
        else
            holder.tvHrNum.setText(String.valueOf(record.getHrList().size()));

        if (updatedRecordList.contains(record)) {
            holder.tvModifyTime.setTextColor(Color.RED);
        } else {
            holder.tvModifyTime.setTextColor(Color.BLACK);
        }

        if(record.equals(selectedRecord)) {
            int bgdColor = ContextCompat.getColor(MyApplication.getContext(), R.color.secondary);
            holder.fileView.setBackgroundColor(bgdColor);
        } else {
            holder.fileView.setBackground(defaultBackground);
        }
    }
    @Override
    public int getItemCount() {
        return allRecordList.size();
    }

    public void updateRecordList() {
        notifyDataSetChanged();
    }

    public void updateSelectedFile(EcgRecord selectFile) {
        int beforePos = allRecordList.indexOf(this.selectedRecord);
        int afterPos = allRecordList.indexOf(selectFile);

        if(beforePos != afterPos) {
            this.selectedRecord = selectFile;
            notifyItemChanged(beforePos);
            notifyItemChanged(afterPos);
        } else {
            notifyItemChanged(beforePos);
        }
    }

    public void insertNewFile(EcgRecord file) {
        ViseLog.e("insert " + file);

        //notifyItemInserted(getItemCount()-1);
        notifyDataSetChanged();
    }

    public void clear() {
        allRecordList.clear();
    }
}
