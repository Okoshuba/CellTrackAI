package com.kubaice.celltrackai.domain.ai

data class ImageAnalysisResult(
    val analysisId: String,
    val status: AnalysisStatus,
    val measurements: Map<String, Any> = emptyMap(),
    val annotatedImageUri: String? = null,
    val algorithmVersion: String = "CellTrack-CV-v2.1",
    val warnings: List<String> = emptyList()
)
