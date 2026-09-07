package com.meruvakirankumar.memora.data.local.mapper

import com.meruvakirankumar.memora.data.local.entity.ReminderEntity
import com.meruvakirankumar.memora.domain.model.Reminder
import com.meruvakirankumar.memora.domain.model.ReminderStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

fun ReminderEntity.toDomain(): Reminder = Reminder(
    id = id,
    memoryId = memoryId,
    reminderStartDate = LocalDate.parse(reminderStartDate),
    reminderTime = LocalTime.parse(reminderTime),
    status = ReminderStatus.valueOf(status),
    completedAt = completedAt?.let { Instant.ofEpochMilli(it) },
)

fun Reminder.toEntity(): ReminderEntity = ReminderEntity(
    id = id,
    memoryId = memoryId,
    reminderStartDate = reminderStartDate.toString(),
    reminderTime = reminderTime.toString(),
    status = status.name,
    completedAt = completedAt?.toEpochMilli(),
)
