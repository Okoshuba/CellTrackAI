package com.kubaice.celltrackai.di

import android.content.Context
import androidx.room.Room
import com.kubaice.celltrackai.data.local.CellTrackDatabase
import com.kubaice.celltrackai.data.local.dao.CellCultureDao
import com.kubaice.celltrackai.data.local.dao.CultureEventDao
import com.kubaice.celltrackai.data.local.dao.PublicationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCellTrackDatabase(
        @ApplicationContext context: Context
    ): CellTrackDatabase {
        return Room.databaseBuilder(
            context,
            CellTrackDatabase::class.java,
            "cell_track_database"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    @Provides
    fun provideCellCultureDao(database: CellTrackDatabase): CellCultureDao {
        return database.cellCultureDao()
    }

    @Provides
    fun provideCultureEventDao(database: CellTrackDatabase): CultureEventDao {
        return database.cultureEventDao()
    }

    @Provides
    fun providePublicationDao(database: CellTrackDatabase): PublicationDao {
        return database.publicationDao()
    }
}
