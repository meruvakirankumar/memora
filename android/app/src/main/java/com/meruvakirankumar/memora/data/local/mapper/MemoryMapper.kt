package com.meruvakirankumar.memora.data.local.mapper

import com.meruvakirankumar.memora.data.local.entity.MemoryEntity
import com.meruvakirankumar.memora.domain.model.EventType
import com.meruvakirankumar.memora.domain.model.Memory
import com.meruvakirankumar.memora.domain.model.MemoryStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun MemoryEntity.toDomain(): Memory = Memory(
    id = id,
    title = title,
    eventType = EventType.valueOf(eventType),
    eventDate = LocalDate.parse(eventDate),
    timeZone = ZoneId.of(timeZone),
    status = MemoryStatus.valueOf(status),
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
)

fun Memory.toEntity(): MemoryEntity = MemoryEntity(
    id = id,
    title = title,
    eventType = eventType.name,
    eventDate = eventDate.toString(),
    timeZone = timeZone.id,
    status = status.name,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
)
