package com.kubaice.celltrackai.domain.repository

import com.kubaice.celltrackai.domain.model.ScientificPublication
import kotlinx.coroutines.flow.Flow

interface PublicationRepository {
    suspend fun searchPublications(query: String): List<ScientificPublication>
    suspend fun savePublication(publication: ScientificPublication)
    fun getPublicationsForCulture(cultureId: String): Flow<List<ScientificPublication>>
    suspend fun associateWithCulture(cultureId: String, publicationId: String)
    suspend fun removeAssociationWithCulture(cultureId: String, publicationId: String)
}
