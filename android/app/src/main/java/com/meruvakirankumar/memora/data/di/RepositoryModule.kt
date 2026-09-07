package com.meruvakirankumar.memora.data.di

import com.meruvakirankumar.memora.data.repository.MemoryRepositoryImpl
import com.meruvakirankumar.memora.data.repository.ReminderRepositoryImpl
import com.meruvakirankumar.memora.domain.repository.MemoryRepository
import com.meruvakirankumar.memora.domain.repository.ReminderRepository
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
    abstract fun bindMemoryRepository(impl: MemoryRepositoryImpl): MemoryRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository
}
