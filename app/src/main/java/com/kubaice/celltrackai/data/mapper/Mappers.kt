package com.kubaice.celltrackai.data.mapper

import com.kubaice.celltrackai.data.local.entity.CellCultureEntity
import com.kubaice.celltrackai.data.local.entity.CultureEventEntity
import com.kubaice.celltrackai.data.local.entity.PublicationEntity
import com.kubaice.celltrackai.domain.model.CellCulture
import com.kubaice.celltrackai.domain.model.CultureEvent
import com.kubaice.celltrackai.domain.model.CultureStatus
import com.kubaice.celltrackai.domain.model.EventType
import com.kubaice.celltrackai.domain.model.ScientificPublication

fun CellCultureEntity.toDomain(): CellCulture {
    return CellCulture(
        id = id,
        name = name,
        cellType = cellType,
        startedAt = startedAt,
        status = try { CultureStatus.valueOf(status) } catch (e: Exception) { CultureStatus.ACTIVE },
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun CellCulture.toEntity(): CellCultureEntity {
    return CellCultureEntity(
        id = id,
        name = name,
        cellType = cellType,
        startedAt = startedAt,
        status = status.name,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun CultureEventEntity.toDomain(): CultureEvent {
    return CultureEvent(
        id = id,
        cultureId = cultureId,
        occurredAt = occurredAt,
        type = try { EventType.valueOf(type) } catch (e: Exception) { EventType.OBSERVATION },
        passageNumber = passageNumber,
        confluencyPercentage = confluencyPercentage,
        notes = notes,
        imageUris = imageUris
    )
}

fun CultureEvent.toEntity(): CultureEventEntity {
    return CultureEventEntity(
        id = id,
        cultureId = cultureId,
        occurredAt = occurredAt,
        type = type.name,
        passageNumber = passageNumber,
        confluencyPercentage = confluencyPercentage,
        notes = notes,
        imageUris = imageUris
    )
}

fun PublicationEntity.toDomain(): ScientificPublication {
    return ScientificPublication(
        id = id,
        title = title,
        authors = authors,
        publicationYear = publicationYear,
        source = source,
        externalUrl = externalUrl,
        abstractText = abstractText
    )
}

fun ScientificPublication.toEntity(): PublicationEntity {
    return PublicationEntity(
        id = id,
        title = title,
        authors = authors,
        publicationYear = publicationYear,
        source = source,
        externalUrl = externalUrl,
        abstractText = abstractText
    )
}
