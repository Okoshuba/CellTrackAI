package com.kubaice.celltrackai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cell_cultures")
data class CellCultureEntity(
    @PrimaryKey val id: String,
    val name: String,
    val cellType: String?,
    val startedAt: Long,
    val status: String,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long
)
