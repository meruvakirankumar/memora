package com.meruvakirankumar.memora.data.repository

import androidx.room.withTransaction
import com.meruvakirankumar.memora.core.di.IoDispatcher
import com.meruvakirankumar.memora.data.local.MemoraDatabase
import com.meruvakirankumar.memora.data.local.dao.MemoryDao
import com.meruvakirankumar.memora.data.local.dao.ReminderDao
import com.meruvakirankumar.memora.data.local.mapper.toDomain
import com.meruvakirankumar.memora.data.local.mapper.toEntity
import com.meruvakirankumar.memora.domain.model.Memory
import com.meruvakirankumar.memora.domain.model.Reminder
import com.meruvakirankumar.memora.domain.repository.MemoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MemoryRepositoryImpl @Inject constructor(
    private val database: MemoraDatabase,
    private val memoryDao: MemoryDao,
    private val reminderDao: ReminderDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : MemoryRepository {

    override fun observeAll(): Flow<List<Memory>> =
        memoryDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observe(id: String): Flow<Memory?> =
        memoryDao.observe(id).map { it?.toDomain() }

    override suspend fun getById(id: String): Memory? = withContext(ioDispatcher) {
        memoryDao.getById(id)?.toDomain()
    }

    override suspend fun getAll(): List<Memory> = withContext(ioDispatcher) {
        memoryDao.getAll().map { it.toDomain() }
    }

    override suspend fun create(memory: Memory, reminder: Reminder?) = withContext(ioDispatcher) {
        // One creation transaction: a failure never leaves a half-created memory/reminder pair.
        database.withTransaction {
            memoryDao.upsert(memory.toEntity())
            reminder?.let { reminderDao.upsert(it.toEntity()) }
        }
    }

    override suspend fun upsert(memory: Memory) = withContext(ioDispatcher) {
        memoryDao.upsert(memory.toEntity())
    }

    override suspend fun delete(id: String) = withContext(ioDispatcher) {
        memoryDao.deleteById(id)
    }
}
