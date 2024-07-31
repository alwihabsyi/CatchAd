package com.catchad.app.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catchad.core.domain.repository.ContentRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val contentRepository: ContentRepository
): ViewModel() {
    fun getAllContent() = contentRepository.getAllContent()
    fun deleteNotifications() = viewModelScope.launch { contentRepository.delete() }
}