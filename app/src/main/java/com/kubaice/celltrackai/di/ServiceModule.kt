package com.kubaice.celltrackai.di

import com.kubaice.celltrackai.domain.ai.ImageAnalysisService
import com.kubaice.celltrackai.domain.ai.MockImageAnalysisService
import com.kubaice.celltrackai.domain.ai.MockObservationStructuringService
import com.kubaice.celltrackai.domain.ai.MockScientificAssistantService
import com.kubaice.celltrackai.domain.ai.ObservationStructuringService
import com.kubaice.celltrackai.domain.ai.ScientificAssistantService
import com.kubaice.celltrackai.domain.literature.LiteratureSearchService
import com.kubaice.celltrackai.domain.literature.MockLiteratureSearchService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    @Singleton
    abstract fun bindObservationStructuringService(
        impl: MockObservationStructuringService
    ): ObservationStructuringService

    @Binds
    @Singleton
    abstract fun bindScientificAssistantService(
        impl: MockScientificAssistantService
    ): ScientificAssistantService

    @Binds
    @Singleton
    abstract fun bindImageAnalysisService(
        impl: MockImageAnalysisService
    ): ImageAnalysisService

    @Binds
    @Singleton
    abstract fun bindLiteratureSearchService(
        impl: MockLiteratureSearchService
    ): LiteratureSearchService
}
