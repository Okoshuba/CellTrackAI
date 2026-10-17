package com.kubaice.celltrackai.domain.model

import java.util.UUID

data class CellCulture(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val cellType: String?,
    val startedAt: Long,
    val status: CultureStatus = CultureStatus.ACTIVE,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
