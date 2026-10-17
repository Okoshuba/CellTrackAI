package com.kubaice.celltrackai.domain.repository

import com.kubaice.celltrackai.domain.model.CultureEvent
import kotlinx.coroutines.flow.Flow

interface CultureEventRepository {
    fun getEventsForCulture(cultureId: String): Flow<List<CultureEvent>>
    suspend fun insertEvent(event: CultureEvent)
    suspend fun updateEvent(event: CultureEvent)
    suspend fun deleteEvent(id: String)
}
