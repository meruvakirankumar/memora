package com.meruvakirankumar.memora.domain.usecase

import com.meruvakirankumar.memora.domain.model.Memory
import com.meruvakirankumar.memora.domain.model.Reminder
import com.meruvakirankumar.memora.domain.repository.MemoryRepository
import javax.inject.Inject

/** Re-creates a memory (and its reminder) that was just deleted, for undo. */
class RestoreMemoryUseCase @Inject constructor(
    private val repository: MemoryRepository,
    private val scheduleReminder: ScheduleReminderUseCase,
) {
    suspend operator fun invoke(memory: Memory, reminder: Reminder?) {
        repository.create(memory, reminder)
        scheduleReminder(memory.id)
    }
}
