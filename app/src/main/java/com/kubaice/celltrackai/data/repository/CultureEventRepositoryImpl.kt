package com.kubaice.celltrackai.data.repository

import com.kubaice.celltrackai.data.local.dao.CultureEventDao
import com.kubaice.celltrackai.data.mapper.toDomain
import com.kubaice.celltrackai.data.mapper.toEntity
import com.kubaice.celltrackai.domain.model.CultureEvent
import com.kubaice.celltrackai.domain.repository.CultureEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CultureEventRepositoryImpl @Inject constructor(
    private val dao: CultureEventDao
) : CultureEventRepository {

    override fun getEventsForCulture(cultureId: String): Flow<List<CultureEvent>> {
        return dao.getEventsForCulture(cultureId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertEvent(event: CultureEvent) {
        dao.insertEvent(event.toEntity())
    }

    override suspend fun updateEvent(event: CultureEvent) {
        dao.updateEvent(event.toEntity())
    }

    override suspend fun deleteEvent(id: String) {
        dao.deleteEvent(id)
    }
}
