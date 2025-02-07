package com.catchad.core.domain.repository

import android.os.Parcelable
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun getDeviceId(): Flow<String?>
    suspend fun setDeviceId(id: String)
    fun getRegistered(): Flow<Boolean>
    suspend fun registerDevice()
    suspend fun publishDevice(device: Parcelable)
}