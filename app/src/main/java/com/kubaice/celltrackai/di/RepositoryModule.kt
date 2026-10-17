package com.kubaice.celltrackai.di

import com.kubaice.celltrackai.data.repository.CellCultureRepositoryImpl
import com.kubaice.celltrackai.data.repository.CultureEventRepositoryImpl
import com.kubaice.celltrackai.data.repository.ImageStorageRepositoryImpl
import com.kubaice.celltrackai.data.repository.PublicationRepositoryImpl
import com.kubaice.celltrackai.domain.repository.CellCultureRepository
import com.kubaice.celltrackai.domain.repository.CultureEventRepository
import com.kubaice.celltrackai.domain.repository.ImageStorageRepository
import com.kubaice.celltrackai.domain.repository.PublicationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCellCultureRepository(
        impl: CellCultureRepositoryImpl
    ): CellCultureRepository

    @Binds
    @Singleton
    abstract fun bindCultureEventRepository(
        impl: CultureEventRepositoryImpl
    ): CultureEventRepository

    @Binds
    @Singleton
    abstract fun bindPublicationRepository(
        impl: PublicationRepositoryImpl
    ): PublicationRepository

    @Binds
    @Singleton
    abstract fun bindImageStorageRepository(
        impl: ImageStorageRepositoryImpl
    ): ImageStorageRepository
}
