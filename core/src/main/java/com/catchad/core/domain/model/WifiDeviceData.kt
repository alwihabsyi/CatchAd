package com.catchad.core.domain.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class WifiDeviceData(
    @Json(name = "device_id")
    val id: String?,
    val ssid: String?,
    val bssid: String,
    val frequency: Int,
    val rssi: Int
): Parcelable