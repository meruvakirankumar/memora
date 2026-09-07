package com.meruvakirankumar.memora.core.time

import java.time.Instant
import java.time.ZoneId

/** Deterministic clock for tests. */
class FixedClock(
    private val instant: Instant,
    private val zone: ZoneId = ZoneId.of("UTC"),
) : AppClock {
    override fun now(): Instant = instant
    override fun zone(): ZoneId = zone
}
