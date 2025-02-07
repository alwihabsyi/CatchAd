package com.catchad.core.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.catchad.core.databinding.ItemDeviceBinding
import com.catchad.core.domain.model.BluetoothDeviceData

class DeviceListAdapter : RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder>() {

    private val diffUtil = object : DiffUtil.ItemCallback<BluetoothDeviceData>() {
        override fun areItemsTheSame(oldItem: BluetoothDeviceData, newItem: BluetoothDeviceData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BluetoothDeviceData, newItem: BluetoothDeviceData): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = differ.currentList[position]
        holder.bind(device)
    }

    inner class DeviceViewHolder(private val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(device: BluetoothDeviceData) {
            binding.deviceName.text = "Name: ${device.name}"
            binding.deviceAddress.text = "Address: ${device.address}"
        }
    }
}
