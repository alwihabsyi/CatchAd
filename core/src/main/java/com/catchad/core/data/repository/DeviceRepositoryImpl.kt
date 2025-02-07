package com.catchad.core.data.repository

import android.os.Parcelable
import com.catchad.core.data.local.datastore.DataStoreDataSource
import com.catchad.core.data.remote.ApiDataSource
import com.catchad.core.data.util.Response
import com.catchad.core.domain.helpers.DeviceInfoHelper
import com.catchad.core.domain.model.BluetoothDeviceData
import com.catchad.core.domain.model.Device
import com.catchad.core.domain.model.DeviceLog
import com.catchad.core.domain.model.WifiDeviceData
import com.catchad.core.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class DeviceRepositoryImpl(
    private val dataStore: DataStoreDataSource,
    private val deviceInfoHelper: DeviceInfoHelper,
    private val apiDataSource: ApiDataSource
) : DeviceRepository {
    private val isRegistered get() = dataStore.getRegistered()

    override fun getDeviceId(): Flow<String?> = dataStore.getDeviceId()

    override suspend fun setDeviceId(id: String) = dataStore.setDeviceId(id)

    override suspend fun registerDevice() {
        val deviceId = UUID.randomUUID().toString().also {
            dataStore.setDeviceId(it)
        }

        val register = apiDataSource.publishRegister(
            Device(
                id = deviceId,
                name = deviceInfoHelper.getUserDeviceName(),
                brand = deviceInfoHelper.getBrandName(),
                manufacturer = deviceInfoHelper.getManufacturerName()
            )
        )

        if (register is Response.Success) dataStore.setRegistered(true)
    }

    override suspend fun publishDevice(device: Parcelable) {
        if (!isRegistered.first()) return

        val id = dataStore.getDeviceId().first().orEmpty()
        when (device) {
            is BluetoothDeviceData -> apiDataSource.run {
                publishBluetooth(device.copy(id = id))
                publishLog(DeviceLog(
                    logMessage = "Detected BLE Device ${device.name} with signal strength ${device.rssi}",
                    deviceId = id
                ))
            }

            is WifiDeviceData -> apiDataSource.run {
                publishWifi(device.copy(id = id))
                publishLog(DeviceLog(
                    logMessage = "Detected WiFi Device ${device.ssid} with signal strength ${device.rssi}",
                    deviceId = id
                ))
            }
        }
    }

    override fun getRegistered(): Flow<Boolean> = dataStore.getRegistered()
}