package com.meruvakirankumar.memora.data.repository

import com.meruvakirankumar.memora.core.di.IoDispatcher
import com.meruvakirankumar.memora.data.local.dao.MemoryDao
import com.meruvakirankumar.memora.data.local.mapper.toDomain
import com.meruvakirankumar.memora.data.local.mapper.toEntity
import com.meruvakirankumar.memora.domain.model.Memory
import com.meruvakirankumar.memora.domain.repository.MemoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MemoryRepositoryImpl @Inject constructor(
    private val dao: MemoryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : MemoryRepository {

    override fun observeAll(): Flow<List<Memory>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observe(id: String): Flow<Memory?> =
        dao.observe(id).map { it?.toDomain() }

    override suspend fun getById(id: String): Memory? = withContext(ioDispatcher) {
        dao.getById(id)?.toDomain()
    }

    override suspend fun upsert(memory: Memory) = withContext(ioDispatcher) {
        dao.upsert(memory.toEntity())
    }

    override suspend fun delete(id: String) = withContext(ioDispatcher) {
        dao.deleteById(id)
    }
}
