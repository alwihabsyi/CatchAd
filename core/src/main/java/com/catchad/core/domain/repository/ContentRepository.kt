package com.catchad.core.domain.repository

import com.catchad.core.domain.model.Content
import kotlinx.coroutines.flow.Flow

interface ContentRepository {
    fun getAllContent(): Flow<List<Content>>
    suspend fun delete()
}