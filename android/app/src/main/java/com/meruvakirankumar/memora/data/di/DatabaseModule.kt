package com.meruvakirankumar.memora.data.di

import android.content.Context
import androidx.room.Room
import com.meruvakirankumar.memora.data.local.MemoraDatabase
import com.meruvakirankumar.memora.data.local.dao.MemoryDao
import com.meruvakirankumar.memora.data.local.dao.ReminderDao
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
    fun provideDatabase(@ApplicationContext context: Context): MemoraDatabase =
        Room.databaseBuilder(context, MemoraDatabase::class.java, MemoraDatabase.NAME).build()

    @Provides
    fun provideMemoryDao(database: MemoraDatabase): MemoryDao = database.memoryDao()

    @Provides
    fun provideReminderDao(database: MemoraDatabase): ReminderDao = database.reminderDao()
}
