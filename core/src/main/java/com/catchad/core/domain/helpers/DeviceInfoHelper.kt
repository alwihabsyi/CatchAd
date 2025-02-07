package com.catchad.core.domain.helpers

import android.os.Build

class DeviceInfoHelper {

    fun getUserDeviceName(): String {
        return Build.MODEL.takeIf { it.isNotBlank() } ?: "Unknown Device"
    }

    fun getBrandName(): String {
        return Build.BRAND.takeIf { it.isNotBlank() } ?: "Unknown Brand"
    }

    fun getManufacturerName(): String {
        return Build.MANUFACTURER.takeIf { it.isNotBlank() } ?: "Unknown Manufacturer"
    }
}
