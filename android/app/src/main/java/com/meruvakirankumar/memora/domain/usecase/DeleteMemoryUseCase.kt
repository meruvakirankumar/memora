package com.meruvakirankumar.memora.domain.usecase

import com.meruvakirankumar.memora.domain.repository.MemoryRepository
import javax.inject.Inject

/** Permanently removes a memory. */
class DeleteMemoryUseCase @Inject constructor(
    private val repository: MemoryRepository,
) {
    suspend operator fun invoke(memoryId: String) = repository.delete(memoryId)
}
