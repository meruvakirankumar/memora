package com.meruvakirankumar.memora.core.di

import com.meruvakirankumar.memora.core.time.AppClock
import com.meruvakirankumar.memora.core.time.SystemClock
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TimeModule {

    @Binds
    @Singleton
    abstract fun bindAppClock(impl: SystemClock): AppClock
}
