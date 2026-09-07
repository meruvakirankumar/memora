package com.meruvakirankumar.memora.domain.scheduling

import java.time.Instant

/**
 * Domain contract for scheduling a reminder delivery for a memory. The Android
 * implementation (AlarmManager) lives in the platform layer.
 */
interface ReminderScheduler {
    fun schedule(memoryId: String, at: Instant)
    fun cancel(memoryId: String)
}
