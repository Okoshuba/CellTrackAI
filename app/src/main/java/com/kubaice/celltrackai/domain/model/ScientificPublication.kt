package com.kubaice.celltrackai.domain.model

import java.util.UUID

data class ScientificPublication(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val authors: List<String> = emptyList(),
    val publicationYear: Int? = null,
    val source: String? = null,
    val externalUrl: String? = null,
    val abstractText: String? = null
)
