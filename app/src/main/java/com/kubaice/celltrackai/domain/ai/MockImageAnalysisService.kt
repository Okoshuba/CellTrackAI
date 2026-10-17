package com.kubaice.celltrackai.domain.ai

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockImageAnalysisService @Inject constructor() : ImageAnalysisService {

    override suspend fun analyzeImage(
        imageUri: String,
        analysisType: AnalysisType
    ): ImageAnalysisResult {
        val analysisId = "ANALYSIS-" + UUID.randomUUID().toString().take(8).uppercase()

        val measurements: Map<String, Any> = when (analysisType) {
            AnalysisType.CONFLUENCY_ESTIMATION -> mapOf(
                "confluency_percentage" to 82.5f,
                "occupied_surface_mm2" to 7.84f,
                "free_surface_mm2" to 1.66f,
                "monolayer_uniformity_score" to 0.91f
            )
            AnalysisType.CELL_COUNTING -> mapOf(
                "total_cell_count" to 1420,
                "viable_cell_count" to 1365,
                "viability_percentage" to 96.1f,
                "cell_density_per_cm2" to 28400,
                "mean_diameter_um" to 17.8f
            )
            AnalysisType.MORPHOLOGY_INSPECTION -> mapOf(
                "mean_cell_area_px" to 420.5f,
                "circularity_index" to 0.84f,
                "elongation_ratio" to 1.35f,
                "multinucleated_count" to 3,
                "detached_cell_ratio" to 0.02f
            )
            AnalysisType.CONTAMINATION_DETECTION -> mapOf(
                "particulate_debris_coverage_pct" to 1.2f,
                "bacterial_flag_risk" to "LOW",
                "fungal_hyphae_count" to 0,
                "media_clarity_score" to 0.95f
            )
        }

        val warnings = if (imageUri.isBlank()) {
            listOf("Sample image URI empty; using default benchmark parameters.")
        } else emptyList()

        return ImageAnalysisResult(
            analysisId = analysisId,
            status = AnalysisStatus.COMPLETED,
            measurements = measurements,
            annotatedImageUri = if (imageUri.isNotBlank()) imageUri else null,
            algorithmVersion = "CellTrack-CV-v2.1",
            warnings = warnings
        )
    }
}
