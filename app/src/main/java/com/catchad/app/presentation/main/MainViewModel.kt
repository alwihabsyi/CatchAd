package com.catchad.app.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.catchad.core.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MainViewModel(
    private val deviceRepository: DeviceRepository
): ViewModel() {

    fun getRssiLimit() = deviceRepository.getRssiLimit().distinctUntilChanged().asLiveData()
    fun setRssiLimit(limit: String) = viewModelScope.launch { deviceRepository.setRssiLimit(limit) }
}