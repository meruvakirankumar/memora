package com.meruvakirankumar.memora.domain.service

import com.meruvakirankumar.memora.core.time.AppClock
import com.meruvakirankumar.memora.domain.model.Memory
import com.meruvakirankumar.memora.domain.model.MemoryStatus
import javax.inject.Inject

/**
 * Derives the live status of a memory from its event date and completion.
 * Completion is a stored fact; overdue/due-today/upcoming are computed from the clock.
 */
class MemoryStatusCalculator @Inject constructor(
    private val clock: AppClock,
) {
    fun status(memory: Memory): MemoryStatus {
        if (memory.status == MemoryStatus.COMPLETED) return MemoryStatus.COMPLETED

        val today = clock.today()
        return when {
            memory.eventDate.isBefore(today) -> MemoryStatus.OVERDUE
            memory.eventDate.isEqual(today) -> MemoryStatus.DUE_TODAY
            else -> MemoryStatus.UPCOMING
        }
    }

    fun urgency(status: MemoryStatus): Int = when (status) {
        MemoryStatus.OVERDUE -> 0
        MemoryStatus.DUE_TODAY -> 1
        MemoryStatus.REMINDER_ACTIVE -> 2
        MemoryStatus.UPCOMING -> 3
        MemoryStatus.COMPLETED -> 4
    }
}
