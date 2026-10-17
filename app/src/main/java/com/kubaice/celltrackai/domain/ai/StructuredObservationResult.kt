package com.kubaice.celltrackai.domain.ai

data class StructuredObservationResult(
    val parsedCellType: String? = null,
    val parsedConfluencyPct: Float? = null,
    val parsedPassageNumber: Int? = null,
    val observedPhenotypes: List<String> = emptyList(),
    val contaminationRisk: String? = null,
    val recommendedActions: List<String> = emptyList(),
    val missingOrUncertainFields: List<String> = emptyList(),
    val explanation: String
)
