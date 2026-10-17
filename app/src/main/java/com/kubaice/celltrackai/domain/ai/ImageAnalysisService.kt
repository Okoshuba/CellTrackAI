package com.kubaice.celltrackai.domain.ai

interface ImageAnalysisService {
    suspend fun analyzeImage(
        imageUri: String,
        analysisType: AnalysisType = AnalysisType.CONFLUENCY_ESTIMATION
    ): ImageAnalysisResult
}
