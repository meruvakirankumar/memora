package com.meruvakirankumar.memora.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * A durable, confirmed memory. This is the heart of Memora — not the image.
 * Created only after the user confirms an [ExtractedMemoryCandidate].
 */
data class Memory(
    val id: String,
    val title: String,
    val eventType: EventType,
    val eventDate: LocalDate,
    val timeZone: ZoneId,
    val status: MemoryStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
)
