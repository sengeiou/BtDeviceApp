package com.cmtech.android.bledevice.ecgmonitor.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.ecgmonitor.model.IEcgAppendixOperator;
import com.cmtech.android.bledevice.ecgmonitor.model.ecgappendix.EcgAppendix;
import com.cmtech.android.bledeviceapp.MyApplication;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.model.AccountManager;
import com.cmtech.android.bledeviceapp.model.User;
import com.cmtech.android.bledeviceapp.util.DateTimeUtil;

import java.util.Date;
import java.util.List;

public class EcgAppendixAdapter extends RecyclerView.Adapter<EcgAppendixAdapter.ViewHolder> {
    private List<EcgAppendix> appendixList; // 附加信息列表
    private final IEcgAppendixOperator appendixOperator; // 附加信息操作者

    static class ViewHolder extends RecyclerView.ViewHolder {
        View appendixView;
        TextView tvCreatorName;
        TextView tvModifyTime;
        EditText etContent;
        ImageButton ibSave;

        private ViewHolder(View itemView) {
            super(itemView);
            appendixView = itemView;
            etContent = appendixView.findViewById(R.id.ecgappendix_content);
            tvCreatorName = appendixView.findViewById(R.id.ecgappendix_creator);
            tvModifyTime = appendixView.findViewById(R.id.ecgappendix_modifytime);
            ibSave = appendixView.findViewById(R.id.ib_ecgappendix_save);
        }
    }

    public EcgAppendixAdapter(List<EcgAppendix> appendixList, IEcgAppendixOperator appendixOperator) {
        this.appendixList = appendixList;
        this.appendixOperator = appendixOperator;
    }

    @NonNull
    @Override
    public EcgAppendixAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_ecgappendix, parent, false);

        final EcgAppendixAdapter.ViewHolder holder = new EcgAppendixAdapter.ViewHolder(view);

        holder.tvCreatorName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User creator = appendixList.get(holder.getAdapterPosition()).getCreator();
                Toast.makeText(MyApplication.getContext(), creator.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        holder.ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User creator = appendixList.get(holder.getAdapterPosition()).getCreator();
                User account = AccountManager.getInstance().getAccount();
                if(appendixOperator != null && creator.equals(account)) {
                    EcgAppendix appendix = appendixList.get(holder.getAdapterPosition());
                    appendix.setContent(holder.etContent.getText().toString());
                    appendix.setModifyTime(new Date().getTime());
                    appendixOperator.saveAppendix();
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final EcgAppendixAdapter.ViewHolder holder, final int position) {
        EcgAppendix appendix = appendixList.get(position);
        User creator = appendix.getCreator();
        User account = AccountManager.getInstance().getAccount();
        if(creator.equals(account)) {
            holder.tvCreatorName.setText(Html.fromHtml("<u>您本人</u>"));
        } else {
            holder.tvCreatorName.setText(Html.fromHtml("<u>" + appendix.getCreator().getUserName() + "</u>"));
        }

        holder.tvModifyTime.setText(DateTimeUtil.timeToShortStringWithTodayYesterday(appendix.getModifyTime()));
        holder.etContent.setText(appendix.getContent());

        if(appendixOperator != null && creator.equals(account)) {
            holder.ibSave.setVisibility(View.VISIBLE);
            holder.etContent.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    holder.etContent.setFocusableInTouchMode(true);
                    return false;
                }
            });

        } else {
            holder.ibSave.setVisibility(View.GONE);

        }


    }

    @Override
    public int getItemCount() {
        return appendixList.size();
    }

    public void update(List<EcgAppendix> commentList) {
        this.appendixList = commentList;
        notifyDataSetChanged();
    }


}