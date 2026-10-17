package com.kubaice.celltrackai.domain.ai

data class AssistantResponse(
    val answerText: String,
    val referencedRecordIds: List<String> = emptyList(),
    val confidenceAndLimitationsNote: String
)
