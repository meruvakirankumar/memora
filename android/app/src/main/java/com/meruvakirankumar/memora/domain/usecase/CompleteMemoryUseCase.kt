package com.meruvakirankumar.memora.domain.usecase

import com.meruvakirankumar.memora.core.error.AppError
import com.meruvakirankumar.memora.core.error.AppResult
import com.meruvakirankumar.memora.core.error.asFailure
import com.meruvakirankumar.memora.core.error.asSuccess
import com.meruvakirankumar.memora.core.time.AppClock
import com.meruvakirankumar.memora.domain.model.MemoryStatus
import com.meruvakirankumar.memora.domain.repository.MemoryRepository
import javax.inject.Inject

/** Marks a memory as handled so future reminders stop. */
class CompleteMemoryUseCase @Inject constructor(
    private val repository: MemoryRepository,
    private val clock: AppClock,
) {
    suspend operator fun invoke(memoryId: String): AppResult<Unit> {
        val existing = repository.getById(memoryId)
            ?: return AppError.Storage().asFailure()

        return try {
            repository.upsert(
                existing.copy(status = MemoryStatus.COMPLETED, updatedAt = clock.now()),
            )
            Unit.asSuccess()
        } catch (e: Exception) {
            AppError.Storage(e).asFailure()
        }
    }
}
