package com.catchad.core.domain.model

import java.util.UUID

data class Content(
    val id: String = UUID.randomUUID().toString().take(5),
    val title: String? = null,
    val description: String? = null,
    val contentUrl: String? = null,
    val date: String? = null
)
