package com.catchad.core.data.mapper

import com.catchad.core.data.local.room.entity.ContentEntity
import com.catchad.core.domain.model.Content
import com.catchad.core.util.toDateString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date

class ContentMapper {
    fun mapDomainToEntity(from: Content): ContentEntity =
        ContentEntity(
            id = from.id,
            contentUrl = from.contentUrl ?: "",
            title = from.title ?: "",
            description = from.description ?: "",
            date = Date().toDateString()
        )

    fun mapEntityToDomain(from: Flow<List<ContentEntity>>): Flow<List<Content>> =
        from.map { contents ->
            contents.map {
                Content(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    contentUrl = it.contentUrl,
                    date = it.date
                )
            }
        }
}