package com.kubaice.celltrackai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "culture_events",
    foreignKeys = [
        ForeignKey(
            entity = CellCultureEntity::class,
            parentColumns = ["id"],
            childColumns = ["cultureId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cultureId"])]
)
data class CultureEventEntity(
    @PrimaryKey val id: String,
    val cultureId: String,
    val occurredAt: Long,
    val type: String,
    val passageNumber: Int?,
    val confluencyPercentage: Float?,
    val notes: String?,
    val imageUris: List<String>
)
