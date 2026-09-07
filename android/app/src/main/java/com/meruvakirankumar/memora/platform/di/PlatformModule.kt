package com.meruvakirankumar.memora.platform.di

import com.meruvakirankumar.memora.platform.image.AndroidTempImageStore
import com.meruvakirankumar.memora.platform.image.TempImageStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlatformModule {

    @Binds
    @Singleton
    abstract fun bindTempImageStore(impl: AndroidTempImageStore): TempImageStore
}
