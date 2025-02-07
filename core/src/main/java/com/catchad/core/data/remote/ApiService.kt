package com.catchad.core.data.remote

import com.catchad.core.domain.model.BluetoothDeviceData
import com.catchad.core.domain.model.Device
import com.catchad.core.domain.model.DeviceLog
import com.catchad.core.domain.model.WifiDeviceData
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("devices")
    suspend fun registerDevice(@Body device: Device): ApiResponse

    @POST("bluetooth")
    suspend fun publishBluetooth(@Body bluetooth: BluetoothDeviceData): ApiResponse

    @POST("wifi")
    suspend fun publishWifi(@Body wifi: WifiDeviceData): ApiResponse

    @POST("logs")
    suspend fun publishLog(@Body log: DeviceLog): ApiResponse
}