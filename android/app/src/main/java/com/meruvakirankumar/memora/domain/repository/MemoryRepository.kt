package com.meruvakirankumar.memora.domain.repository

import com.meruvakirankumar.memora.domain.model.Memory
import com.meruvakirankumar.memora.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

/** Domain contract for reading and writing memories. Implemented in the data layer. */
interface MemoryRepository {
    fun observeAll(): Flow<List<Memory>>
    fun observe(id: String): Flow<Memory?>
    suspend fun getById(id: String): Memory?
    suspend fun getAll(): List<Memory>

    /** Atomically persists a memory together with its optional initial reminder. */
    suspend fun create(memory: Memory, reminder: Reminder?)
    suspend fun upsert(memory: Memory)
    suspend fun delete(id: String)
}
