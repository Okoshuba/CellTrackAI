package com.kubaice.celltrackai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "culture_publication_cross_ref",
    primaryKeys = ["cultureId", "publicationId"],
    foreignKeys = [
        ForeignKey(
            entity = CellCultureEntity::class,
            parentColumns = ["id"],
            childColumns = ["cultureId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PublicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["publicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("cultureId"),
        Index("publicationId")
    ]
)
data class CulturePublicationCrossRef(
    val cultureId: String,
    val publicationId: String
)
