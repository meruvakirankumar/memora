package com.meruvakirankumar.memora.domain.model

/** A memory paired with its current reminder (V1: at most one active reminder). */
data class MemoryWithReminder(
    val memory: Memory,
    val reminder: Reminder?,
)
