package com.meruvakirankumar.memora.domain.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ReminderScheduleCalculatorTest {

    private val nineAm = LocalTime.of(9, 0)

    @Test
    fun `occurrences cover the lead window and overdue days`() {
        val start = LocalDate.of(2027, 8, 29)
        val event = LocalDate.of(2027, 8, 31)

        val occurrences = ReminderScheduleCalculator.occurrences(start, event)

        // 3 lead days (29, 30, 31) + 7 overdue days = 10
        assertEquals(10, occurrences.size)
        assertEquals(LocalDate.of(2027, 8, 29), occurrences.first())
        assertEquals(LocalDate.of(2027, 9, 7), occurrences.last())
    }

    @Test
    fun `next occurrence is the first strictly after now`() {
        val start = LocalDate.of(2027, 8, 29)
        val event = LocalDate.of(2027, 8, 31)
        val now = LocalDateTime.of(2027, 8, 29, 10, 0) // after the 29th 9am reminder

        val next = ReminderScheduleCalculator.nextOccurrence(start, event, nineAm, now)

        assertEquals(LocalDateTime.of(2027, 8, 30, 9, 0), next)
    }

    @Test
    fun `no occurrence once the overdue window has passed`() {
        val start = LocalDate.of(2027, 8, 29)
        val event = LocalDate.of(2027, 8, 31)
        val now = LocalDateTime.of(2027, 9, 8, 0, 0) // after the last overdue day

        val next = ReminderScheduleCalculator.nextOccurrence(start, event, nineAm, now)

        assertNull(next)
    }
}
