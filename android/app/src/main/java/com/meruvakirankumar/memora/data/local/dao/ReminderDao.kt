package com.meruvakirankumar.memora.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.meruvakirankumar.memora.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE memoryId = :memoryId ORDER BY reminderStartDate ASC")
    fun observeByMemory(memoryId: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: String): ReminderEntity?

    @Upsert
    suspend fun upsert(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: String)
}
