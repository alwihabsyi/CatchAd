package com.catchad.core.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "content")
data class ContentEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val contentUrl: String,
    val date: String
)
