package com.kubaice.celltrackai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "publications")
data class PublicationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val authors: List<String>,
    val publicationYear: Int?,
    val source: String?,
    val externalUrl: String?,
    val abstractText: String?
)
