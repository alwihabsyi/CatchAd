package com.catchad.core.data.local.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

class DataStoreDataSource(
    private val preference: PreferencesDataStore
) {
    suspend fun setDeviceId(id: String) = preference.setString(DEVICE_ID, id)

    fun getDeviceId() = preference.getString(DEVICE_ID)

    suspend fun setRegistered(registered: Boolean) = preference.setBoolean(REGISTERED, registered)

    fun getRegistered() = preference.getBoolean(REGISTERED)

    suspend fun setRssiLimit(limit: String) = preference.setString(RSSI_LIMIT, limit)

    fun getRssiLimit() = preference.getString(RSSI_LIMIT)

    companion object {
        val REGISTERED = booleanPreferencesKey("registered")
        val DEVICE_ID = stringPreferencesKey("device_id")
        val RSSI_LIMIT = stringPreferencesKey("rssi_limit")
    }
}