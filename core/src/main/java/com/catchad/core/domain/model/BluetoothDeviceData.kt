package com.catchad.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BluetoothDeviceData(
    val id: String?,
    val name: String?,
    val address: String,
    val manufacturerData: String,
    val rssi: Int
): Parcelable