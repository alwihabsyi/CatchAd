package com.catchad.core.data.repository

import com.catchad.core.data.local.RoomDataSource
import com.catchad.core.data.mapper.ContentMapper
import com.catchad.core.domain.model.Content
import com.catchad.core.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow

class ContentRepositoryImpl(
    private val contentMapper: ContentMapper,
    private val roomDataSource: RoomDataSource
) : ContentRepository {

    override fun getAllContent(): Flow<List<Content>> =
        contentMapper.mapEntityToDomain(roomDataSource.getAllContents())

    override suspend fun delete() = roomDataSource.deleteContents()

}