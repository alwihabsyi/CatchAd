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

    companion object {
        val REGISTERED = booleanPreferencesKey("registered")
        val DEVICE_ID = stringPreferencesKey("device_id")
    }
}