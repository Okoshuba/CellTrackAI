package com.kubaice.celltrackai

import com.kubaice.celltrackai.domain.ai.AnalysisStatus
import com.kubaice.celltrackai.domain.ai.AnalysisType
import com.kubaice.celltrackai.domain.ai.MockImageAnalysisService
import com.kubaice.celltrackai.domain.ai.MockScientificAssistantService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MockServicesTest {

    private val assistantService = MockScientificAssistantService()
    private val imageAnalysisService = MockImageAnalysisService()

    @Test
    fun testScientificAssistantServicePassagingQuestion() = runBlocking {
        val response = assistantService.answerQuestion("C-1", "When and how should I split or passage my culture?")

        assertNotNull(response)
        assertTrue(response.answerText.contains("subculture when cells reach 80-90% confluency", ignoreCase = true))
        assertTrue(response.referencedRecordIds.contains("SOP-CELL-PAS-01"))
        assertTrue(response.confidenceAndLimitationsNote.isNotEmpty())
    }

    @Test
    fun testScientificAssistantServiceDoublingTimeQuestion() = runBlocking {
        val response = assistantService.answerQuestion("C-1", "What is the expected doubling time for HeLa?")

        assertNotNull(response)
        assertTrue(response.answerText.contains("HeLa: ~20-24 hours", ignoreCase = true))
        assertTrue(response.referencedRecordIds.contains("LIT-GROWTH-KINETICS-2024"))
    }

    @Test
    fun testScientificAssistantServiceContaminationQuestion() = runBlocking {
        val response = assistantService.answerQuestion("C-1", "Media turned yellow and turbid, is it contaminated?")

        assertNotNull(response)
        assertTrue(response.answerText.contains("bacterial or yeast proliferation", ignoreCase = true))
        assertTrue(response.referencedRecordIds.contains("SOP-CONTAM-ISOLATION-03"))
    }

    @Test
    fun testScientificAssistantServiceMediaQuestion() = runBlocking {
        val response = assistantService.answerQuestion("C-1", "How often should I feed or change media?")

        assertNotNull(response)
        assertTrue(response.answerText.contains("replenishment schedule is every 48 hours", ignoreCase = true))
        assertTrue(response.referencedRecordIds.contains("SOP-MEDIA-PREP-02"))
    }

    @Test
    fun testScientificAssistantServiceGeneralQuestion() = runBlocking {
        val response = assistantService.answerQuestion("C-100", "How is my culture performing?")

        assertNotNull(response)
        assertTrue(response.answerText.contains("culture #C-100", ignoreCase = true))
        assertTrue(response.referencedRecordIds.contains("REC-GENERAL-OBS-01"))
    }

    @Test
    fun testImageAnalysisServiceConfluencyEstimation() = runBlocking {
        val imageUri = "content://media/external/images/media/101"
        val result = imageAnalysisService.analyzeImage(imageUri, AnalysisType.CONFLUENCY_ESTIMATION)

        assertEquals(AnalysisStatus.COMPLETED, result.status)
        assertEquals(imageUri, result.annotatedImageUri)
        assertEquals("CellTrack-CV-v2.1", result.algorithmVersion)
        assertTrue(result.warnings.isEmpty())

        val confluency = result.measurements["confluency_percentage"] as? Float
        assertNotNull(confluency)
        assertEquals(82.5f, confluency)
    }

    @Test
    fun testImageAnalysisServiceCellCounting() = runBlocking {
        val result = imageAnalysisService.analyzeImage("content://media/1", AnalysisType.CELL_COUNTING)

        assertEquals(AnalysisStatus.COMPLETED, result.status)
        val totalCount = result.measurements["total_cell_count"] as? Int
        val viability = result.measurements["viability_percentage"] as? Float
        assertNotNull(totalCount)
        assertNotNull(viability)
        assertEquals(1420, totalCount)
        assertEquals(96.1f, viability)
    }

    @Test
    fun testImageAnalysisServiceMorphologyInspection() = runBlocking {
        val result = imageAnalysisService.analyzeImage("content://media/2", AnalysisType.MORPHOLOGY_INSPECTION)

        assertEquals(AnalysisStatus.COMPLETED, result.status)
        val circularity = result.measurements["circularity_index"] as? Float
        assertNotNull(circularity)
        assertEquals(0.84f, circularity)
    }

    @Test
    fun testImageAnalysisServiceContaminationDetection() = runBlocking {
        val result = imageAnalysisService.analyzeImage("content://media/3", AnalysisType.CONTAMINATION_DETECTION)

        assertEquals(AnalysisStatus.COMPLETED, result.status)
        val flagRisk = result.measurements["bacterial_flag_risk"] as? String
        assertEquals("LOW", flagRisk)
    }

    @Test
    fun testImageAnalysisServiceBlankUriWarning() = runBlocking {
        val result = imageAnalysisService.analyzeImage("", AnalysisType.CONFLUENCY_ESTIMATION)

        assertEquals(AnalysisStatus.COMPLETED, result.status)
        assertTrue(result.warnings.any { it.contains("empty", ignoreCase = true) })
    }
}
