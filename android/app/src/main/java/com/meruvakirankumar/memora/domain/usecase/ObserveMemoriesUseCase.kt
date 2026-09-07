package com.meruvakirankumar.memora.domain.usecase

import com.meruvakirankumar.memora.domain.model.MemoryWithReminder
import com.meruvakirankumar.memora.domain.repository.MemoryRepository
import com.meruvakirankumar.memora.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Streams every memory paired with its current reminder. */
class ObserveMemoriesUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val reminderRepository: ReminderRepository,
) {
    operator fun invoke(): Flow<List<MemoryWithReminder>> =
        combine(memoryRepository.observeAll(), reminderRepository.observeAll()) { memories, reminders ->
            val byMemory = reminders.groupBy { it.memoryId }
            memories.map { memory ->
                MemoryWithReminder(memory, byMemory[memory.id]?.firstOrNull())
            }
        }
}
