package com.kubaice.celltrackai

import com.kubaice.celltrackai.data.local.entity.CellCultureEntity
import com.kubaice.celltrackai.data.local.entity.CultureEventEntity
import com.kubaice.celltrackai.data.local.entity.PublicationEntity
import com.kubaice.celltrackai.data.mapper.toDomain
import com.kubaice.celltrackai.data.mapper.toEntity
import com.kubaice.celltrackai.domain.model.CellCulture
import com.kubaice.celltrackai.domain.model.CultureEvent
import com.kubaice.celltrackai.domain.model.CultureStatus
import com.kubaice.celltrackai.domain.model.EventType
import com.kubaice.celltrackai.domain.model.ScientificPublication
import org.junit.Assert.assertEquals
import org.junit.Test

class MappersTest {

    @Test
    fun testCellCultureMapping() {
        val domain = CellCulture(
            id = "C-123",
            name = "iPSC Line #4",
            cellType = "Human iPSC",
            startedAt = 1600000000000L,
            status = CultureStatus.ARCHIVED,
            notes = "Passage #20 stocks frozen",
            createdAt = 1600000000000L,
            updatedAt = 1600005000000L
        )

        val entity = domain.toEntity()

        assertEquals(domain.id, entity.id)
        assertEquals(domain.name, entity.name)
        assertEquals(domain.cellType, entity.cellType)
        assertEquals(domain.startedAt, entity.startedAt)
        assertEquals("ARCHIVED", entity.status)
        assertEquals(domain.notes, entity.notes)
        assertEquals(domain.createdAt, entity.createdAt)
        assertEquals(domain.updatedAt, entity.updatedAt)

        val mappedBackDomain = entity.toDomain()
        assertEquals(domain, mappedBackDomain)
    }

    @Test
    fun testCellCultureEntityUnknownStatusFallback() {
        val entityWithInvalidStatus = CellCultureEntity(
            id = "C-456",
            name = "Unknown Status Line",
            cellType = "Vero E6",
            startedAt = 1600000000000L,
            status = "UNKNOWN_OR_INVALID_STATUS",
            notes = null,
            createdAt = 1600000000000L,
            updatedAt = 1600000000000L
        )

        val domain = entityWithInvalidStatus.toDomain()
        assertEquals(CultureStatus.ACTIVE, domain.status)
    }

    @Test
    fun testCultureEventMapping() {
        val domain = CultureEvent(
            id = "E-999",
            cultureId = "C-123",
            occurredAt = 1600001000000L,
            type = EventType.PASSAGE,
            passageNumber = 15,
            confluencyPercentage = 85.0f,
            notes = "Split 1:3 into two T-75 flasks",
            imageUris = listOf("file:///images/p15_1.jpg", "file:///images/p15_2.jpg")
        )

        val entity = domain.toEntity()

        assertEquals(domain.id, entity.id)
        assertEquals(domain.cultureId, entity.cultureId)
        assertEquals(domain.occurredAt, entity.occurredAt)
        assertEquals("PASSAGE", entity.type)
        assertEquals(domain.passageNumber, entity.passageNumber)
        assertEquals(domain.confluencyPercentage, entity.confluencyPercentage)
        assertEquals(domain.notes, entity.notes)
        assertEquals(domain.imageUris, entity.imageUris)

        val mappedBackDomain = entity.toDomain()
        assertEquals(domain, mappedBackDomain)
    }

    @Test
    fun testCultureEventEntityUnknownTypeFallback() {
        val entityWithInvalidType = CultureEventEntity(
            id = "E-888",
            cultureId = "C-123",
            occurredAt = 1600001000000L,
            type = "INVALID_EVENT_TYPE_STRING",
            passageNumber = null,
            confluencyPercentage = null,
            notes = "Some notes",
            imageUris = emptyList()
        )

        val domain = entityWithInvalidType.toDomain()
        assertEquals(EventType.OBSERVATION, domain.type)
    }

    @Test
    fun testScientificPublicationMapping() {
        val domain = ScientificPublication(
            id = "PUB-101",
            title = "Protocol for High-Yield iPSC Culturing",
            authors = listOf("Dr. Jane Doe", "Dr. John Smith"),
            publicationYear = 2024,
            source = "Stem Cell Journal",
            externalUrl = "https://example.com/pub/101",
            abstractText = "This study details automated passage tracking..."
        )

        val entity = domain.toEntity()

        assertEquals(domain.id, entity.id)
        assertEquals(domain.title, entity.title)
        assertEquals(domain.authors, entity.authors)
        assertEquals(domain.publicationYear, entity.publicationYear)
        assertEquals(domain.source, entity.source)
        assertEquals(domain.externalUrl, entity.externalUrl)
        assertEquals(domain.abstractText, entity.abstractText)

        val mappedBackDomain = entity.toDomain()
        assertEquals(domain, mappedBackDomain)
    }
}
