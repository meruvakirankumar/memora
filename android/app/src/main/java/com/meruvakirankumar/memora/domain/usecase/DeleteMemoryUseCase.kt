package com.meruvakirankumar.memora.domain.usecase

import com.meruvakirankumar.memora.domain.repository.MemoryRepository
import com.meruvakirankumar.memora.domain.scheduling.ReminderScheduler
import javax.inject.Inject

/** Permanently removes a memory and cancels its reminders. */
class DeleteMemoryUseCase @Inject constructor(
    private val repository: MemoryRepository,
    private val scheduler: ReminderScheduler,
) {
    suspend operator fun invoke(memoryId: String) {
        scheduler.cancel(memoryId)
        repository.delete(memoryId)
    }
}
