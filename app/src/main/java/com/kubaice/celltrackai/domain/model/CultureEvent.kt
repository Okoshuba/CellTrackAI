package com.kubaice.celltrackai.domain.model

import java.util.UUID

data class CultureEvent(
    val id: String = UUID.randomUUID().toString(),
    val cultureId: String,
    val occurredAt: Long = System.currentTimeMillis(),
    val type: EventType,
    val passageNumber: Int? = null,
    val confluencyPercentage: Float? = null,
    val notes: String? = null,
    val imageUris: List<String> = emptyList()
)
