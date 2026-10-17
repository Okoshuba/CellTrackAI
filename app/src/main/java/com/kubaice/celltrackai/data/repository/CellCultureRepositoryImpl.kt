package com.kubaice.celltrackai.data.repository

import com.kubaice.celltrackai.data.local.dao.CellCultureDao
import com.kubaice.celltrackai.data.mapper.toDomain
import com.kubaice.celltrackai.data.mapper.toEntity
import com.kubaice.celltrackai.domain.model.CellCulture
import com.kubaice.celltrackai.domain.model.CultureStatus
import com.kubaice.celltrackai.domain.repository.CellCultureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CellCultureRepositoryImpl @Inject constructor(
    private val dao: CellCultureDao
) : CellCultureRepository {

    override fun getCultures(): Flow<List<CellCulture>> {
        return dao.getCultures().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCultureById(id: String): Flow<CellCulture?> {
        return dao.getCultureById(id).map { it?.toDomain() }
    }

    override suspend fun insertCulture(culture: CellCulture) {
        dao.insertCulture(culture.toEntity())
    }

    override suspend fun updateCulture(culture: CellCulture) {
        dao.updateCulture(culture.copy(updatedAt = System.currentTimeMillis()).toEntity())
    }

    override suspend fun archiveCulture(id: String) {
        dao.archiveCulture(id, CultureStatus.ARCHIVED.name, System.currentTimeMillis())
    }

    override suspend fun deleteCulture(id: String) {
        dao.deleteCulture(id)
    }

    override suspend fun getCultureCount(): Int {
        return dao.getCultureCount()
    }
}
