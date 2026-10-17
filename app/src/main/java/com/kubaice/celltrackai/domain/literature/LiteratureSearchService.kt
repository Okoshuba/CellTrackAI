package com.kubaice.celltrackai.domain.literature

import com.kubaice.celltrackai.domain.model.ScientificPublication

interface LiteratureSearchService {
    suspend fun searchPublications(query: String): List<ScientificPublication>
    suspend fun getPrepopulatedPublications(): List<ScientificPublication>
}
