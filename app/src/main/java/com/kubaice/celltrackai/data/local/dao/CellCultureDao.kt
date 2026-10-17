package com.kubaice.celltrackai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kubaice.celltrackai.data.local.entity.CellCultureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CellCultureDao {

    @Query("SELECT * FROM cell_cultures ORDER BY updatedAt DESC")
    fun getCultures(): Flow<List<CellCultureEntity>>

    @Query("SELECT * FROM cell_cultures WHERE id = :id")
    fun getCultureById(id: String): Flow<CellCultureEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCulture(culture: CellCultureEntity)

    @Update
    suspend fun updateCulture(culture: CellCultureEntity)

    @Query("UPDATE cell_cultures SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archiveCulture(id: String, status: String, updatedAt: Long)

    @Query("DELETE FROM cell_cultures WHERE id = :id")
    suspend fun deleteCulture(id: String)

    @Query("SELECT COUNT(*) FROM cell_cultures")
    suspend fun getCultureCount(): Int
}
