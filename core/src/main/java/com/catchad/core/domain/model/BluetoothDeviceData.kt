package com.catchad.core.domain.model

data class BluetoothDeviceData(
    val id: String?,
    val name: String?,
    val address: String,
    val manufacturerData: String,
    val rssi: Int
)