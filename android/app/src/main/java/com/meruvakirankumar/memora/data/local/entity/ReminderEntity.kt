package com.meruvakirankumar.memora.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = MemoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["memoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("memoryId")],
)
data class ReminderEntity(
    @PrimaryKey val id: String,
    val memoryId: String,
    val reminderStartDate: String,
    val reminderTime: String,
    val status: String,
    val completedAt: Long?,
)
