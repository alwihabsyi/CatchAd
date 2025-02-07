package com.catchad.core.data.local.room

import com.catchad.core.data.local.room.dao.ContentDao

class RoomDataSource(
    private val contentDao: ContentDao
) {
    // Content
    suspend fun deleteContents() = contentDao.deleteContents()
    fun getAllContents() = contentDao.getAllContents()
}