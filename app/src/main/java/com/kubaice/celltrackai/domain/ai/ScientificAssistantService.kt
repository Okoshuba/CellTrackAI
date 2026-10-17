package com.kubaice.celltrackai.domain.ai

interface ScientificAssistantService {
    suspend fun answerQuestion(
        cultureId: String,
        question: String
    ): AssistantResponse
}
