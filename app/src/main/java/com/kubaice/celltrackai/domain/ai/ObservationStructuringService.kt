package com.kubaice.celltrackai.domain.ai

import com.kubaice.celltrackai.domain.model.CellCulture

interface ObservationStructuringService {
    suspend fun structureObservation(
        rawText: String,
        cultureContext: CellCulture? = null
    ): StructuredObservationResult
}
