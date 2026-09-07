package com.meruvakirankumar.memora.model

enum class EventType {
    EXPIRATION,
    PAYMENT,
    RENEWAL,
    RETURN,
    GENERAL;

    fun label(): String = name.lowercase().replaceFirstChar { it.uppercase() }
}

enum class Confidence {
    HIGH,
    MEDIUM,
    LOW;

    fun label(): String = name.lowercase().replaceFirstChar { it.uppercase() }
}

enum class MemoryStatus {
    UPCOMING,
    DUE_TODAY,
    OVERDUE,
    COMPLETED;

    fun label(): String = when (this) {
        UPCOMING -> "Upcoming"
        DUE_TODAY -> "Due Today"
        OVERDUE -> "Overdue"
        COMPLETED -> "Completed"
    }
}

/** A confirmed, stored memory. */
data class Memory(
    val id: String,
    val title: String,
    val eventType: EventType,
    val dateIso: String,
    val confidence: Confidence,
    val sourceText: String,
    val createdAtIso: String,
    val completedAtIso: String? = null,
    val notificationId: Int? = null,
)

/** A memory candidate before the user confirms it. */
data class MemoryCandidate(
    val title: String,
    val eventType: EventType,
    val dateIso: String,
    val confidence: Confidence,
    val sourceText: String,
)
