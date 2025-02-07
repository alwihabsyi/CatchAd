package com.catchad.core.data.remote

import android.util.Log
import com.catchad.core.data.util.Response
import com.catchad.core.domain.model.BluetoothDeviceData
import com.catchad.core.domain.model.Device
import com.catchad.core.domain.model.DeviceLog
import com.catchad.core.domain.model.WifiDeviceData

class ApiDataSource(
    private val apiService: ApiService
) {

    suspend fun publishRegister(device: Device) = try {
        when (val r = apiService.registerDevice(device)) {
            is ApiResponse.Error -> {
                Log.e(TAG, r.error)
                Response.Error
            }

            is ApiResponse.Success -> {
                Log.i(TAG, r.message)
                Response.Success
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, e.message, e)
        Response.Error
    }

    suspend fun publishBluetooth(bluetooth: BluetoothDeviceData) = try {
        when(val r = apiService.publishBluetooth(bluetooth)) {
            is ApiResponse.Error -> {
                Log.e(TAG, r.error)
                Response.Error
            }

            is ApiResponse.Success -> {
                Log.i(TAG, r.message)
                Response.Success
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, e.message, e)
        Response.Error
    }

    suspend fun publishWifi(wifi: WifiDeviceData) = try {
        when(val r = apiService.publishWifi(wifi)) {
            is ApiResponse.Error -> {
                Log.e(TAG, r.error)
                Response.Error
            }

            is ApiResponse.Success -> {
                Log.i(TAG, r.message)
                Response.Success
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, e.message, e)
        Response.Error
    }

    suspend fun publishLog(log: DeviceLog) = try {
        when(val r = apiService.publishLog(log)) {
            is ApiResponse.Error -> {
                Log.e(TAG, r.error)
                Response.Error
            }

            is ApiResponse.Success -> {
                Log.i(TAG, r.message)
                Response.Success
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, e.message, e)
        Response.Error
    }

    companion object {
        const val TAG = "ApiDataSource"
    }
}