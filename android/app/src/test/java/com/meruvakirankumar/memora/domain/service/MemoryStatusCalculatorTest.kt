package com.meruvakirankumar.memora.domain.service

import com.meruvakirankumar.memora.core.time.FixedClock
import com.meruvakirankumar.memora.domain.model.EventType
import com.meruvakirankumar.memora.domain.model.Memory
import com.meruvakirankumar.memora.domain.model.MemoryStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class MemoryStatusCalculatorTest {

    private val clock = FixedClock(
        instant = Instant.parse("2026-09-07T12:00:00Z"),
        zone = ZoneId.of("UTC"),
    )
    private val calculator = MemoryStatusCalculator(clock)

    @Test
    fun `past date is overdue`() {
        assertEquals(MemoryStatus.OVERDUE, calculator.status(memory(LocalDate.of(2026, 9, 6))))
    }

    @Test
    fun `today is due today`() {
        assertEquals(MemoryStatus.DUE_TODAY, calculator.status(memory(LocalDate.of(2026, 9, 7))))
    }

    @Test
    fun `future date is upcoming`() {
        assertEquals(MemoryStatus.UPCOMING, calculator.status(memory(LocalDate.of(2026, 9, 8))))
    }

    @Test
    fun `completed status overrides date`() {
        val completed = memory(LocalDate.of(2026, 9, 6), MemoryStatus.COMPLETED)
        assertEquals(MemoryStatus.COMPLETED, calculator.status(completed))
    }

    private fun memory(date: LocalDate, status: MemoryStatus = MemoryStatus.UPCOMING) = Memory(
        id = "mem",
        title = "Milk",
        eventType = EventType.EXPIRY,
        eventDate = date,
        timeZone = ZoneId.of("UTC"),
        status = status,
        createdAt = clock.now(),
        updatedAt = clock.now(),
    )
}
