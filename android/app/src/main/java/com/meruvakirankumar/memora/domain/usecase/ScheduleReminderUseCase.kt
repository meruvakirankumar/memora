package com.meruvakirankumar.memora.domain.usecase

import com.meruvakirankumar.memora.core.time.AppClock
import com.meruvakirankumar.memora.domain.model.MemoryStatus
import com.meruvakirankumar.memora.domain.repository.MemoryRepository
import com.meruvakirankumar.memora.domain.repository.ReminderRepository
import com.meruvakirankumar.memora.domain.scheduling.ReminderScheduler
import com.meruvakirankumar.memora.domain.service.ReminderScheduleCalculator
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * Computes and schedules the next reminder occurrence for a memory. Completed or
 * finished memories are unscheduled. Notification delivery itself is a platform concern.
 */
class ScheduleReminderUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val reminderRepository: ReminderRepository,
    private val scheduler: ReminderScheduler,
    private val clock: AppClock,
) {
    suspend operator fun invoke(memoryId: String) {
        val memory = memoryRepository.getById(memoryId)
        if (memory == null || memory.status == MemoryStatus.COMPLETED) {
            scheduler.cancel(memoryId)
            return
        }

        val reminder = reminderRepository.getByMemory(memoryId)
        val startDate = reminder?.reminderStartDate ?: memory.eventDate.minusDays(1)
        val time = reminder?.reminderTime ?: LocalTime.of(9, 0)
        val now = LocalDateTime.ofInstant(clock.now(), clock.zone())

        val next = ReminderScheduleCalculator.nextOccurrence(startDate, memory.eventDate, time, now)
        if (next != null) {
            scheduler.schedule(memoryId, next.atZone(clock.zone()).toInstant())
        } else {
            scheduler.cancel(memoryId)
        }
    }
}
