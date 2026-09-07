package com.meruvakirankumar.memora.domain.usecase

import com.meruvakirankumar.memora.domain.model.Memory
import com.meruvakirankumar.memora.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams all stored memories. Status/sorting is derived in the presentation layer. */
class ObserveMemoriesUseCase @Inject constructor(
    private val repository: MemoryRepository,
) {
    operator fun invoke(): Flow<List<Memory>> = repository.observeAll()
}
