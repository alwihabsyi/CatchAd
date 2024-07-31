package com.catchad.core.data.local

import com.catchad.core.data.local.dao.ContentDao

class RoomDataSource(
    private val contentDao: ContentDao
) {
    // Content
    suspend fun deleteContents() = contentDao.deleteContents()
    fun getAllContents() = contentDao.getAllContents()
}