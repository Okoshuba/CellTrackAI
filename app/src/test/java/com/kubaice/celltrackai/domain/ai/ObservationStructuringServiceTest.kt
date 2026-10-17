package com.kubaice.celltrackai.domain.ai

import com.kubaice.celltrackai.domain.model.CellCulture
import com.kubaice.celltrackai.domain.model.CultureStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    }

    @Test
    fun testStructureObservationDetectsContaminationRisk() = runBlocking {
        val rawText = "Media turned yellow and cloudy overnight with black dots near floating cells."
        val result = service.structureObservation(rawText, null)

        assertNotNull(result)
        assertTrue(result.contaminationRisk?.contains("HIGH") == true)
        assertTrue(result.recommendedActions.any { it.contains("Isolate culture", ignoreCase = true) })
    }
}
