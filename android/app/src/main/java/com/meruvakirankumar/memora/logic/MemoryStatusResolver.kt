package com.meruvakirankumar.memora.logic

import com.meruvakirankumar.memora.model.Memory
import com.meruvakirankumar.memora.model.MemoryStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object MemoryStatusResolver {

    private val MONTHS = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
    )

    fun statusFor(memory: Memory): MemoryStatus {
        if (memory.completedAtIso != null) return MemoryStatus.COMPLETED
        return statusForDate(memory.dateIso)
    }

    fun statusForDate(dateIso: String): MemoryStatus {
        val today = LocalDate.now()
        val date = LocalDate.parse(dateIso)
        return when {
            date.isBefore(today) -> MemoryStatus.OVERDUE
            date.isEqual(today) -> MemoryStatus.DUE_TODAY
            else -> MemoryStatus.UPCOMING
        }
    }

    fun urgency(status: MemoryStatus): Int = when (status) {
        MemoryStatus.OVERDUE -> 0
        MemoryStatus.DUE_TODAY -> 1
        MemoryStatus.UPCOMING -> 2
        MemoryStatus.COMPLETED -> 3
    }

    fun isValidIsoDate(value: String): Boolean = try {
        LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
        true
    } catch (e: Exception) {
        false
    }

    fun formatDate(dateIso: String): String {
        val date = LocalDate.parse(dateIso)
        return "${MONTHS[date.monthValue - 1]} ${date.dayOfMonth}, ${date.year}"
    }
}
