package com.kubaice.celltrackai

import com.kubaice.celltrackai.domain.ai.MockObservationStructuringService
import com.kubaice.celltrackai.domain.model.CellCulture
import com.kubaice.celltrackai.domain.model.CultureStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ObservationStructuringServiceTest {

    private val service = MockObservationStructuringService()

    @Test
    fun testStructureObservationWithConfluencyAndPassage() = runBlocking {
        val rawText = "Culture looks healthy at passage 12. Estimated confluency is 85%. Cobblestone morphology observed."
        val cultureContext = CellCulture(
            id = "C-1",
            name = "HeLa Test",
            cellType = "HeLa",
            startedAt = System.currentTimeMillis(),
            status = CultureStatus.ACTIVE
        )

        val result = service.structureObservation(rawText, cultureContext)

        assertNotNull(result)
        assertEquals(85.0f, result.parsedConfluencyPct)
        assertEquals(12, result.parsedPassageNumber)
        assertEquals("HeLa", result.parsedCellType)
        assertTrue(result.observedPhenotypes.any { it.contains("Cobblestone", ignoreCase = true) })
        assertTrue(result.recommendedActions.any { it.contains("Passage/split", ignoreCase = true) })
        assertTrue(result.missingOrUncertainFields.isEmpty())
        assertTrue(result.explanation.isNotEmpty())
    }

    @Test
    fun testStructureObservationDetectsContaminationRisk() = runBlocking {
        val rawText = "Media turned yellow and cloudy overnight with black dots near floating cells."
        val result = service.structureObservation(rawText, null)

        assertNotNull(result)
        assertTrue(result.contaminationRisk?.contains("HIGH", ignoreCase = true) == true)
        assertTrue(result.recommendedActions.any { it.contains("Isolate culture", ignoreCase = true) })
    }

    @Test
    fun testStructureObservationCellTypeExtractionFromText() = runBlocking {
        val rawText = "Subcultured iPSC colonies at passage 5. Cells are at 75% confluency."
        val result = service.structureObservation(rawText, null)

        assertNotNull(result)
        assertEquals("iPSC", result.parsedCellType)
        assertEquals(5, result.parsedPassageNumber)
        assertEquals(75.0f, result.parsedConfluencyPct)
    }

    @Test
    fun testStructureObservationMissingFieldsDetection() = runBlocking {
        val rawText = "Cells look fine and viable, monolayer intact."
        val result = service.structureObservation(rawText, null)

        assertNotNull(result)
        assertNull(result.parsedConfluencyPct)
        assertNull(result.parsedPassageNumber)

        // Missing fields list should explicitly report missing confluency and passage number
        assertTrue(result.missingOrUncertainFields.any { it.contains("Confluency percentage", ignoreCase = true) })
        assertTrue(result.missingOrUncertainFields.any { it.contains("Passage number missing", ignoreCase = true) })
    }
}
