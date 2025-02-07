package com.catchad.core.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map


class PreferencesDataStore(private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

    suspend fun setString(key: Preferences.Key<String>, value: String) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun getString(key: Preferences.Key<String>) = context.dataStore.data.map { preferences ->
        preferences[key]
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    suspend fun setBoolean(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun getBoolean(key: Preferences.Key<Boolean>) = context.dataStore.data.map { preferences ->
        preferences[key] ?: false
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    suspend fun setInt(key: Preferences.Key<Int>, value: Int) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun getBooleanNullable(key: Preferences.Key<Boolean>) = context.dataStore.data.map { preferences ->
        preferences[key]
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    fun getInt(key: Preferences.Key<Int>) = context.dataStore.data.map { preferences ->
        preferences[key] ?: 0
    }.flowOn(Dispatchers.IO).distinctUntilChanged()

    fun getIntNullable(key: Preferences.Key<Int>) = context.dataStore.data.map { preferences ->
        preferences[key]
    }.flowOn(Dispatchers.IO).distinctUntilChanged()
}