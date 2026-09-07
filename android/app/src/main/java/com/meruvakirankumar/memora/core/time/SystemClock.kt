package com.meruvakirankumar.memora.core.time

import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/** Real clock backed by the device time and default time zone. */
class SystemClock @Inject constructor() : AppClock {
    override fun now(): Instant = Instant.now()
    override fun zone(): ZoneId = ZoneId.systemDefault()
}
