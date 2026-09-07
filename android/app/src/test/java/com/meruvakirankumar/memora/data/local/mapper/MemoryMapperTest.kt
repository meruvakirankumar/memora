package com.meruvakirankumar.memora.data.local.mapper

import com.meruvakirankumar.memora.domain.model.EventType
import com.meruvakirankumar.memora.domain.model.Memory
import com.meruvakirankumar.memora.domain.model.MemoryStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class MemoryMapperTest {

    @Test
    fun `entity to domain and back preserves values`() {
        val original = Memory(
            id = "mem-1",
            title = "Milk",
            eventType = EventType.EXPIRATION,
            eventDate = LocalDate.of(2027, 8, 31),
            timeZone = ZoneId.of("UTC"),
            status = MemoryStatus.UPCOMING,
            createdAt = Instant.parse("2026-09-07T10:15:30Z"),
            updatedAt = Instant.parse("2026-09-07T10:15:30Z"),
        )

        val roundTripped = original.toEntity().toDomain()

        assertEquals(original, roundTripped)
    }
}
