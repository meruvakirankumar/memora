package com.meruvakirankumar.memora.domain.repository

import com.meruvakirankumar.memora.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

/** Domain contract for reading and writing reminders. Implemented in the data layer. */
interface ReminderRepository {
    fun observeByMemory(memoryId: String): Flow<List<Reminder>>
    suspend fun getById(id: String): Reminder?
    suspend fun upsert(reminder: Reminder)
    suspend fun delete(id: String)
}
