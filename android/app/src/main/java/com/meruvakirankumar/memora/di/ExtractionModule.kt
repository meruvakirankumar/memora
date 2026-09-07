package com.meruvakirankumar.memora.di

import com.meruvakirankumar.memora.domain.extraction.DefaultMemoryExtractor
import com.meruvakirankumar.memora.domain.extraction.DefaultOcrTextNormalizer
import com.meruvakirankumar.memora.domain.extraction.MemoryExtractor
import com.meruvakirankumar.memora.domain.extraction.OcrTextNormalizer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Composition root bindings for the deterministic (non-Android) extraction pieces. */
@Module
@InstallIn(SingletonComponent::class)
abstract class ExtractionModule {

    @Binds
    @Singleton
    abstract fun bindOcrTextNormalizer(impl: DefaultOcrTextNormalizer): OcrTextNormalizer

    @Binds
    @Singleton
    abstract fun bindMemoryExtractor(impl: DefaultMemoryExtractor): MemoryExtractor
}
