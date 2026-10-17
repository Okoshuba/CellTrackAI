package com.kubaice.celltrackai.data.repository

import com.kubaice.celltrackai.data.local.dao.PublicationDao
import com.kubaice.celltrackai.data.local.entity.CulturePublicationCrossRef
import com.kubaice.celltrackai.data.mapper.toDomain
import com.kubaice.celltrackai.data.mapper.toEntity
import com.kubaice.celltrackai.domain.model.ScientificPublication
import com.kubaice.celltrackai.domain.repository.PublicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PublicationRepositoryImpl @Inject constructor(
    private val dao: PublicationDao
) : PublicationRepository {

    override suspend fun searchPublications(query: String): List<ScientificPublication> {
        return dao.searchPublications(query).map { it.toDomain() }
    }

    override suspend fun savePublication(publication: ScientificPublication) {
        dao.insertPublication(publication.toEntity())
    }

    override fun getPublicationsForCulture(cultureId: String): Flow<List<ScientificPublication>> {
        return dao.getPublicationsForCulture(cultureId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun associateWithCulture(cultureId: String, publicationId: String) {
        dao.insertCrossRef(CulturePublicationCrossRef(cultureId, publicationId))
    }

    override suspend fun removeAssociationWithCulture(cultureId: String, publicationId: String) {
        dao.deleteCrossRef(cultureId, publicationId)
    }
}
