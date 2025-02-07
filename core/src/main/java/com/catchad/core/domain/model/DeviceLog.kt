package com.catchad.core.domain.model

import com.squareup.moshi.Json

data class DeviceLog(
    @Json(name = "log_message")
    val logMessage: String,
    @Json(name = "device_id")
    val deviceId: String
)