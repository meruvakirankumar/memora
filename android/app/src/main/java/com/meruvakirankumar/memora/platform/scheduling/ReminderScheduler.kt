package com.meruvakirankumar.memora.platform.scheduling

import java.time.Instant

/**
 * Platform contract for scheduling reminder delivery at a future time.
 * Backed by AlarmManager / WorkManager in Stage 5/6.
 */
interface ReminderScheduler {
    fun schedule(reminderId: String, at: Instant)
    fun cancel(reminderId: String)
}
