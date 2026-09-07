package com.meruvakirankumar.memora.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.meruvakirankumar.memora.data.local.dao.MemoryDao
import com.meruvakirankumar.memora.data.local.dao.ReminderDao
import com.meruvakirankumar.memora.data.local.entity.MemoryEntity
import com.meruvakirankumar.memora.data.local.entity.ReminderEntity

@Database(
    entities = [MemoryEntity::class, ReminderEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class MemoraDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        const val NAME = "memora.db"
    }
}
