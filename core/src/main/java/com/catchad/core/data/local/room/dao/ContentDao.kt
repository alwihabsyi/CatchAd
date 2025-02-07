package com.catchad.core.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.catchad.core.data.local.room.entity.ContentEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ContentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contentEntity: ContentEntity)

    @Query("UPDATE content set id = :id WHERE id = :oldId")
    suspend fun updateContent(oldId: String, id: String = UUID.randomUUID().toString().take(5))

    @Query("SELECT * FROM content ORDER BY date DESC")
    fun getAllContents(): Flow<List<ContentEntity>>

    @Query("DELETE FROM content where id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM content")
    suspend fun deleteContents()
}