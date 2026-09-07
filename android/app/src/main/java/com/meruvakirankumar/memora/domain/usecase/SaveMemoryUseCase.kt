package com.meruvakirankumar.memora.domain.usecase

import com.meruvakirankumar.memora.core.error.AppError
import com.meruvakirankumar.memora.core.error.AppResult
import com.meruvakirankumar.memora.core.error.asFailure
import com.meruvakirankumar.memora.core.error.asSuccess
import com.meruvakirankumar.memora.core.time.AppClock
import com.meruvakirankumar.memora.domain.model.EventType
import com.meruvakirankumar.memora.domain.model.Memory
import com.meruvakirankumar.memora.domain.model.MemoryStatus
import com.meruvakirankumar.memora.domain.model.Reminder
import com.meruvakirankumar.memora.domain.model.ReminderStatus
import com.meruvakirankumar.memora.domain.repository.MemoryRepository
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

/**
 * Creates a durable memory together with its initial reminder from user-confirmed fields.
 * This is the CONFIRM → REMEMBER boundary: only confirmed data reaches the repository,
 * and the memory + reminder are persisted in one atomic transaction.
 */
class SaveMemoryUseCase @Inject constructor(
    private val repository: MemoryRepository,
    private val clock: AppClock,
    private val scheduleReminder: ScheduleReminderUseCase,
) {
    suspend operator fun invoke(
        title: String,
        eventType: EventType,
        eventDate: LocalDate,
        reminderLeadDays: Int = DEFAULT_LEAD_DAYS,
    ): AppResult<Unit> {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) {
            return AppError.NotUnderstood.asFailure()
        }

        return try {
            val now = clock.now()
            val memoryId = UUID.randomUUID().toString()
            val memory = Memory(
                id = memoryId,
                title = cleanTitle,
                eventType = eventType,
                eventDate = eventDate,
                timeZone = clock.zone(),
                status = MemoryStatus.UPCOMING,
                createdAt = now,
                updatedAt = now,
            )
            val leadDays = reminderLeadDays.coerceIn(MIN_LEAD_DAYS, MAX_LEAD_DAYS)
            val reminder = Reminder(
                id = UUID.randomUUID().toString(),
                memoryId = memoryId,
                reminderStartDate = eventDate.minusDays(leadDays.toLong()),
                reminderTime = DEFAULT_REMINDER_TIME,
                status = ReminderStatus.SCHEDULED,
                completedAt = null,
            )
            repository.create(memory, reminder)
            // Scheduling is a separate concern from the persistence transaction.
            scheduleReminder(memoryId)
            Unit.asSuccess()
        } catch (e: Exception) {
            AppError.Storage(e).asFailure()
        }
    }

    companion object {
        const val MIN_LEAD_DAYS = 1
        const val MAX_LEAD_DAYS = 10
        const val DEFAULT_LEAD_DAYS = 1
        val DEFAULT_REMINDER_TIME: LocalTime = LocalTime.of(9, 0)
    }
}
