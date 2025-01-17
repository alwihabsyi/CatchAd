package com.catchad.core.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.catchad.core.databinding.ItemDeviceBinding
import com.catchad.core.domain.model.BluetoothDeviceData

class DeviceListAdapter : ListAdapter<BluetoothDeviceData, DeviceListAdapter.DeviceViewHolder>(DeviceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = getItem(position)
        holder.bind(device)
    }

    inner class DeviceViewHolder(private val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(device: BluetoothDeviceData) {
            binding.deviceName.text = "Name: ${device.name}"
            binding.deviceAddress.text = "Address: ${device.address}"
        }
    }

    class DeviceDiffCallback : DiffUtil.ItemCallback<BluetoothDeviceData>() {
        override fun areItemsTheSame(oldItem: BluetoothDeviceData, newItem: BluetoothDeviceData): Boolean {
            return oldItem.id == newItem.id && oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: BluetoothDeviceData, newItem: BluetoothDeviceData): Boolean {
            return oldItem.id == newItem.id
        }
    }
}
