package com.meruvakirankumar.memora.core.time

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Abstraction over the system clock so time-dependent logic (reminders,
 * overdue calculations) is deterministic and testable.
 */
interface AppClock {
    fun now(): Instant
    fun zone(): ZoneId
    fun today(): LocalDate = LocalDate.ofInstant(now(), zone())
}
