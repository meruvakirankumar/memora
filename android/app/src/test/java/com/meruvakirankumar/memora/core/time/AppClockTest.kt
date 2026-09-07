package com.meruvakirankumar.memora.core.time

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class AppClockTest {

    @Test
    fun `today derives from instant and zone`() {
        val clock = FixedClock(
            instant = Instant.parse("2026-09-07T23:30:00Z"),
            zone = ZoneId.of("UTC"),
        )

        assertEquals(LocalDate.of(2026, 9, 7), clock.today())
    }

    @Test
    fun `today respects time zone offset`() {
        val clock = FixedClock(
            instant = Instant.parse("2026-09-07T23:30:00Z"),
            zone = ZoneId.of("Asia/Kolkata"),
        )

        assertEquals(LocalDate.of(2026, 9, 8), clock.today())
    }
}
