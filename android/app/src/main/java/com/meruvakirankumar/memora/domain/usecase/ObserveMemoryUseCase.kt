package com.meruvakirankumar.memora.domain.usecase

import com.meruvakirankumar.memora.domain.model.MemoryWithReminder
import com.meruvakirankumar.memora.domain.repository.MemoryRepository
import com.meruvakirankumar.memora.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Observes a single memory with its reminder, reactively. */
class ObserveMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val reminderRepository: ReminderRepository,
) {
    operator fun invoke(memoryId: String): Flow<MemoryWithReminder?> =
        combine(
            memoryRepository.observe(memoryId),
            reminderRepository.observeByMemory(memoryId),
        ) { memory, reminders ->
            memory?.let { MemoryWithReminder(it, reminders.firstOrNull()) }
        }
}
