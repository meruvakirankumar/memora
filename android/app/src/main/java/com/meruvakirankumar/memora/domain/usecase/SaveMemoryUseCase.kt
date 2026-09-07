package com.meruvakirankumar.memora.domain.usecase

import com.meruvakirankumar.memora.core.error.AppError
import com.meruvakirankumar.memora.core.error.AppResult
import com.meruvakirankumar.memora.core.error.asFailure
import com.meruvakirankumar.memora.core.error.asSuccess
import com.meruvakirankumar.memora.core.time.AppClock
import com.meruvakirankumar.memora.domain.model.EventType
import com.meruvakirankumar.memora.domain.model.Memory
import com.meruvakirankumar.memora.domain.model.MemoryStatus
import com.meruvakirankumar.memora.domain.repository.MemoryRepository
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

/**
 * Creates a durable memory from user-confirmed fields. This is the CONFIRM → REMEMBER
 * boundary: only confirmed data reaches the repository.
 */
class SaveMemoryUseCase @Inject constructor(
    private val repository: MemoryRepository,
    private val clock: AppClock,
) {
    suspend operator fun invoke(
        title: String,
        eventType: EventType,
        eventDate: LocalDate,
    ): AppResult<Unit> {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) {
            return AppError.NotUnderstood.asFailure()
        }

        return try {
            val now = clock.now()
            val memory = Memory(
                id = UUID.randomUUID().toString(),
                title = cleanTitle,
                eventType = eventType,
                eventDate = eventDate,
                timeZone = clock.zone(),
                status = MemoryStatus.UPCOMING,
                createdAt = now,
                updatedAt = now,
            )
            // Reminder is null until the reminder stage; the create path is already atomic-capable.
            repository.create(memory, reminder = null)
            Unit.asSuccess()
        } catch (e: Exception) {
            AppError.Storage(e).asFailure()
        }
    }
}
