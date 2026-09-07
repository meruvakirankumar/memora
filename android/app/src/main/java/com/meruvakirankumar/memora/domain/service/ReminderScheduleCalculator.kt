package com.meruvakirankumar.memora.domain.service

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Computes the reminder occurrences for a memory following the V1 contract:
 * a daily reminder from the reminder start date through the event date, then a
 * daily overdue reminder for up to [OVERDUE_DAYS] days after the event date.
 */
object ReminderScheduleCalculator {

    const val OVERDUE_DAYS = 7

    fun occurrences(reminderStartDate: LocalDate, eventDate: LocalDate): List<LocalDate> {
        val result = mutableListOf<LocalDate>()
        var day = if (reminderStartDate.isAfter(eventDate)) eventDate else reminderStartDate
        while (!day.isAfter(eventDate)) {
            result += day
            day = day.plusDays(1)
        }
        for (offset in 1..OVERDUE_DAYS) {
            result += eventDate.plusDays(offset.toLong())
        }
        return result
    }

    /** The first occurrence (at [reminderTime]) strictly after [now], or null if all have passed. */
    fun nextOccurrence(
        reminderStartDate: LocalDate,
        eventDate: LocalDate,
        reminderTime: LocalTime,
        now: LocalDateTime,
    ): LocalDateTime? = occurrences(reminderStartDate, eventDate)
        .map { it.atTime(reminderTime) }
        .firstOrNull { it.isAfter(now) }
}
