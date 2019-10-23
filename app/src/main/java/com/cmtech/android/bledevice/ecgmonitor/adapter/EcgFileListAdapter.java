package com.cmtech.android.bledevice.ecgmonitor.adapter;

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

import com.cmtech.android.bledevice.ecgmonitor.model.ecgfile.EcgFile;
import com.cmtech.android.bledevice.ecgmonitor.view.EcgFileExploreActivity;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.model.UserManager;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
  *
  * ClassName:      EcgFileListAdapter
  * Description:    Ecg文件列表Adapter
  * Author:         chenm
  * CreateDate:     2018/11/10 下午4:09
  * UpdateUser:     chenm
  * UpdateDate:     2018/11/10 下午4:09
  * UpdateRemark:   更新说明
  * Version:        1.0
 */

public class EcgFileListAdapter extends RecyclerView.Adapter<EcgFileListAdapter.ViewHolder> {
    private final EcgFileExploreActivity activity;
    private List<EcgFile> fileList = new ArrayList<>();
    private List<File> updatedFileList = new ArrayList<>();
    private EcgFile selectedFile;
    private Drawable defaultBackground; // 缺省背景

    static class ViewHolder extends RecyclerView.ViewHolder {
        View fileView;
        TextView tvCreator; // 创建人
        TextView tvCreatedTime; // 创建时间
        TextView tvLength; // 信号长度
        TextView tvHrNum; // 心率次数
        View vIsUpdate; // 是否已更新

        ViewHolder(View itemView) {
            super(itemView);
            fileView = itemView;
            tvCreator = fileView.findViewById(R.id.ecgfile_creator);
            tvCreatedTime = fileView.findViewById(R.id.ecgfile_createtime);
            tvLength = fileView.findViewById(R.id.ecgfile_length);
            tvHrNum = fileView.findViewById(R.id.ecgfile_hr_num);
            vIsUpdate = fileView.findViewById(R.id.ecgfile_update);
        }
    }

    public EcgFileListAdapter(EcgFileExploreActivity activity) {
        this.activity = activity;
    }

    @NonNull
    @Override
    public EcgFileListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_ecg_file, parent, false);

        final EcgFileListAdapter.ViewHolder holder = new EcgFileListAdapter.ViewHolder(view);
        defaultBackground = holder.fileView.getBackground();
        holder.fileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.selectFile(fileList.get(holder.getAdapterPosition()));
            }
        });
        holder.tvCreator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EcgFile file = fileList.get(holder.getAdapterPosition());
                User creator = file.getCreator();
                Toast.makeText(MyApplication.getContext(), creator.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull EcgFileListAdapter.ViewHolder holder, final int position) {
        EcgFile file = fileList.get(position);
        if(file == null) return;

        User fileCreator = file.getCreator();
        User account = UserManager.getInstance().getUser();
        if(fileCreator.equals(account)) {
            holder.tvCreator.setText(Html.fromHtml("<u>您本人</u>"));
        } else {
            holder.tvCreator.setText(Html.fromHtml("<u>" + file.getCreatorName() + "</u>"));
        }

        String createdTime = DateTimeUtil.timeToShortStringWithTodayYesterday(file.getCreatedTime());
        holder.tvCreatedTime.setText(createdTime);

        if(file.getDataNum() == 0) {
            holder.tvLength.setText("无");
        } else {
            String dataTimeLength = DateTimeUtil.secToTimeInChinese(file.getDataNum() / file.getSampleRate());
            holder.tvLength.setText(dataTimeLength);
        }

        int hrNum = file.getHrList().size();
        holder.tvHrNum.setText(String.valueOf(hrNum));

        if(file.equals(selectedFile)) {
            int bgdColor = ContextCompat.getColor(MyApplication.getContext(), R.color.secondary);
            holder.fileView.setBackgroundColor(bgdColor);
        } else {
            holder.fileView.setBackground(defaultBackground);
        }

        if (updatedFileList.contains(file.getFile())) {
            holder.vIsUpdate.setVisibility(View.VISIBLE);
        } else {
            holder.vIsUpdate.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }


    public void updateFileList(List<EcgFile> fileList) {
        this.fileList = fileList;
        updatedFileList = activity.getUpdatedFiles();
        notifyDataSetChanged();
    }


    public void updateSelectedFile(EcgFile selectFile) {
        this.selectedFile = selectFile;
        notifyDataSetChanged();
    }
}
