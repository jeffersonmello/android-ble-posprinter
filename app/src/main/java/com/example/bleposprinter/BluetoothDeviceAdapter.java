package com.example.bleposprinter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {
    private List<BluetoothDeviceModel> devices;
    private LayoutInflater inflater;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(BluetoothDeviceModel device);
    }

    public BluetoothDeviceAdapter(Context context, List<BluetoothDeviceModel> devices, OnItemClickListener listener) {
        this.devices = devices;
        this.listener = listener;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.device_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            BluetoothDeviceModel device = devices.get(position);

            if (device == null) return;

            if (device.getName() == null || device.getName() == "DEVICE NAME") return;

            holder.tvDeviceName.setText(device.getName() + " (" + device.getAddress() + ")");
            holder.tvDeviceName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(device);
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return devices != null ? devices.size() : 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearDevices() {
        devices.clear();
        notifyDataSetChanged();
    }

    public Boolean checkIfDeviceAdded(BluetoothDeviceModel device) {
        for (BluetoothDeviceModel d : devices) {
            if (d.getAddress().equals(device.getAddress())) {
                return true;
            }
        }

        return false;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addDevice(BluetoothDeviceModel device) {
        if (!checkIfDeviceAdded(device)) {
            devices.add(device);
            notifyDataSetChanged();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDeviceName = itemView.findViewById(R.id.tv_device_name);
        }
    }
}
