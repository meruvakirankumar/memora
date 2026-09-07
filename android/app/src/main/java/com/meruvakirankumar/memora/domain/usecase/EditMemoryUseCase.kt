package com.meruvakirankumar.memora.domain.usecase

import com.meruvakirankumar.memora.core.error.AppError
import com.meruvakirankumar.memora.core.error.AppResult
import com.meruvakirankumar.memora.core.error.asFailure
import com.meruvakirankumar.memora.core.error.asSuccess
import com.meruvakirankumar.memora.core.time.AppClock
import com.meruvakirankumar.memora.domain.model.EventType
import com.meruvakirankumar.memora.domain.model.Reminder
import com.meruvakirankumar.memora.domain.model.ReminderStatus
import com.meruvakirankumar.memora.domain.repository.MemoryRepository
import com.meruvakirankumar.memora.domain.repository.ReminderRepository
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

/** Applies user edits to an existing memory and reschedules its reminder. */
class EditMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val reminderRepository: ReminderRepository,
    private val clock: AppClock,
    private val scheduleReminder: ScheduleReminderUseCase,
) {
    suspend operator fun invoke(
        memoryId: String,
        title: String,
        eventType: EventType,
        eventDate: LocalDate,
        reminderLeadDays: Int,
    ): AppResult<Unit> {
        val existing = memoryRepository.getById(memoryId) ?: return AppError.Storage().asFailure()

        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return AppError.NotUnderstood.asFailure()

        return try {
            val now = clock.now()
            val memory = existing.copy(
                title = cleanTitle,
                eventType = eventType,
                eventDate = eventDate,
                updatedAt = now,
            )

            val lead = reminderLeadDays.coerceIn(SaveMemoryUseCase.MIN_LEAD_DAYS, SaveMemoryUseCase.MAX_LEAD_DAYS)
            val existingReminder = reminderRepository.getByMemory(memoryId)
            val reminder = Reminder(
                id = existingReminder?.id ?: UUID.randomUUID().toString(),
                memoryId = memoryId,
                reminderStartDate = eventDate.minusDays(lead.toLong()),
                reminderTime = existingReminder?.reminderTime ?: LocalTime.of(9, 0),
                status = ReminderStatus.SCHEDULED,
                completedAt = null,
            )

            memoryRepository.create(memory, reminder)
            scheduleReminder(memoryId)
            Unit.asSuccess()
        } catch (e: Exception) {
            AppError.Storage(e).asFailure()
        }
    }
}
