package com.meruvakirankumar.memora.data.repository

import com.meruvakirankumar.memora.core.di.IoDispatcher
import com.meruvakirankumar.memora.data.local.dao.ReminderDao
import com.meruvakirankumar.memora.data.local.mapper.toDomain
import com.meruvakirankumar.memora.data.local.mapper.toEntity
import com.meruvakirankumar.memora.domain.model.Reminder
import com.meruvakirankumar.memora.domain.repository.ReminderRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val dao: ReminderDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ReminderRepository {

    override fun observeByMemory(memoryId: String): Flow<List<Reminder>> =
        dao.observeByMemory(memoryId).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): Reminder? = withContext(ioDispatcher) {
        dao.getById(id)?.toDomain()
    }

    override suspend fun upsert(reminder: Reminder) = withContext(ioDispatcher) {
        dao.upsert(reminder.toEntity())
    }

    override suspend fun delete(id: String) = withContext(ioDispatcher) {
        dao.deleteById(id)
    }
}
