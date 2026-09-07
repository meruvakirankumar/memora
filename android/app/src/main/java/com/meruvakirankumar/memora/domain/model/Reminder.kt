package com.meruvakirankumar.memora.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * A reminder attached to a memory. A memory may have multiple reminders over
 * time, though V1 typically keeps one active reminder per memory.
 */
data class Reminder(
    val id: String,
    val memoryId: String,
    val reminderStartDate: LocalDate,
    val reminderTime: LocalTime,
    val status: ReminderStatus,
    val completedAt: Instant?,
)
