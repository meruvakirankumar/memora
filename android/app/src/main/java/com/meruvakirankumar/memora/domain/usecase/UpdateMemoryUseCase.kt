package com.meruvakirankumar.memora.domain.usecase

import com.meruvakirankumar.memora.core.error.AppError
import com.meruvakirankumar.memora.core.error.AppResult
import com.meruvakirankumar.memora.core.error.asFailure
import com.meruvakirankumar.memora.core.error.asSuccess
import com.meruvakirankumar.memora.core.time.AppClock
import com.meruvakirankumar.memora.domain.model.Memory
import com.meruvakirankumar.memora.domain.repository.MemoryRepository
import javax.inject.Inject

/** Persists edits to an existing memory, refreshing its updated timestamp. */
class UpdateMemoryUseCase @Inject constructor(
    private val repository: MemoryRepository,
    private val clock: AppClock,
) {
    suspend operator fun invoke(memory: Memory): AppResult<Unit> = try {
        repository.upsert(memory.copy(updatedAt = clock.now()))
        Unit.asSuccess()
    } catch (e: Exception) {
        AppError.Storage(e).asFailure()
    }
}
