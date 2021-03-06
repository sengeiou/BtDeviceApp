package com.cmtech.android.bledevice.ecg.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cmtech.android.bledevice.ecg.device.EcgHttpBroadcast;
import com.cmtech.android.bledeviceapp.R;

import java.util.ArrayList;
import java.util.List;

public class EcgReceiverAdapter extends RecyclerView.Adapter<EcgReceiverAdapter.ViewHolder> {
    private List<EcgHttpBroadcast.Receiver> receivers;
    private boolean enable = false;

    public interface OnReceiverChangedListener {
        void onReceiverChanged(EcgHttpBroadcast.Receiver receiver, boolean isChecked);
    }
    private final OnReceiverChangedListener listener;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbReceiver;
        TextView tvReceiverDescription;

        ViewHolder(View itemView) {
            super(itemView);
            cbReceiver = itemView.findViewById(R.id.cb_ecg_signal_receiver);
            tvReceiverDescription = itemView.findViewById(R.id.tv_ecg_signal_receiver_description);
        }
    }

    public EcgReceiverAdapter(OnReceiverChangedListener listener) {
        this.receivers = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public EcgReceiverAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycle_item_ecg_receiver, parent, false);
        final EcgReceiverAdapter.ViewHolder holder = new EcgReceiverAdapter.ViewHolder(view);

        holder.cbReceiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null) {
                    listener.onReceiverChanged(receivers.get(holder.getAdapterPosition()), holder.cbReceiver.isChecked());
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(EcgReceiverAdapter.ViewHolder holder, final int position) {
        EcgHttpBroadcast.Receiver receiver = receivers.get(position);
        if(TextUtils.isEmpty(receiver.getName())) {
            holder.cbReceiver.setText("匿名");
        }
        else {
            holder.cbReceiver.setText(receiver.getName());
        }

        if(TextUtils.isEmpty(receiver.getNote())) {
            holder.tvReceiverDescription.setText("无个人信息");
        } else {
            holder.tvReceiverDescription.setText(receiver.getNote());
        }

        holder.cbReceiver.setChecked(receiver.isReceiving());
        holder.cbReceiver.setEnabled(enable);
    }

    @Override
    public int getItemCount() {
        return receivers.size();
    }

    public void setEnabled(boolean enable) {
        this.enable = enable;
        notifyDataSetChanged();
    }

    public void setReceivers(List<EcgHttpBroadcast.Receiver> receivers) {
        this.receivers = receivers;
        notifyDataSetChanged();
    }
}
