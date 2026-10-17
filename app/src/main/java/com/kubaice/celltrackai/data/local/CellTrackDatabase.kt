package com.kubaice.celltrackai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kubaice.celltrackai.data.local.converters.Converters
import com.kubaice.celltrackai.data.local.dao.CellCultureDao
import com.kubaice.celltrackai.data.local.dao.CultureEventDao
import com.kubaice.celltrackai.data.local.dao.PublicationDao
import com.kubaice.celltrackai.data.local.entity.CellCultureEntity
import com.kubaice.celltrackai.data.local.entity.CultureEventEntity
import com.kubaice.celltrackai.data.local.entity.CulturePublicationCrossRef
import com.kubaice.celltrackai.data.local.entity.PublicationEntity

@Database(
    entities = [
        CellCultureEntity::class,
        CultureEventEntity::class,
        PublicationEntity::class,
        CulturePublicationCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CellTrackDatabase : RoomDatabase() {
    abstract fun cellCultureDao(): CellCultureDao
    abstract fun cultureEventDao(): CultureEventDao
    abstract fun publicationDao(): PublicationDao
}
