package com.meruvakirankumar.memora.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val eventType: String,
    val eventDate: String,
    val timeZone: String,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long,
)
