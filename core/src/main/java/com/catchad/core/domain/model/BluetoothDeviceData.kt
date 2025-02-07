package com.catchad.core.domain.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class BluetoothDeviceData(
    @Json(name = "device_id")
    val id: String?,
    val name: String?,
    val address: String,
    @Json(name = "manufacturer")
    val manufacturerData: String,
    val rssi: Int
): Parcelable