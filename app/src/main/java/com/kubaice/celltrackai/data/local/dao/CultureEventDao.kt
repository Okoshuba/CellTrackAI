package com.kubaice.celltrackai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kubaice.celltrackai.data.local.entity.CultureEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CultureEventDao {

    @Query("SELECT * FROM culture_events WHERE cultureId = :cultureId ORDER BY occurredAt DESC")
    fun getEventsForCulture(cultureId: String): Flow<List<CultureEventEntity>>

    @Query("SELECT * FROM culture_events WHERE cultureId = :cultureId ORDER BY occurredAt DESC")
    suspend fun getEventsListForCulture(cultureId: String): List<CultureEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CultureEventEntity)

    @Update
    suspend fun updateEvent(event: CultureEventEntity)

    @Query("DELETE FROM culture_events WHERE id = :id")
    suspend fun deleteEvent(id: String)
}
