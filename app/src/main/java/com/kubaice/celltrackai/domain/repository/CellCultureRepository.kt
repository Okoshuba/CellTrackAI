package com.kubaice.celltrackai.domain.repository

import com.kubaice.celltrackai.domain.model.CellCulture
import kotlinx.coroutines.flow.Flow

interface CellCultureRepository {
    fun getCultures(): Flow<List<CellCulture>>
    fun getCultureById(id: String): Flow<CellCulture?>
    suspend fun insertCulture(culture: CellCulture)
    suspend fun updateCulture(culture: CellCulture)
    suspend fun archiveCulture(id: String)
    suspend fun deleteCulture(id: String)
    suspend fun getCultureCount(): Int
}
