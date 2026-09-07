package com.meruvakirankumar.memora.domain.model

/** The kind of actionable event a memory represents. */
enum class EventType {
    EXPIRATION,
    PAYMENT,
    RETURN,
    WARRANTY,
    RENEWAL,
    DOCUMENT_EXPIRY,
    GENERAL,
}

/** Lifecycle state of a memory. */
enum class MemoryStatus {
    UPCOMING,
    REMINDER_ACTIVE,
    DUE_TODAY,
    OVERDUE,
    COMPLETED,
}

/** Lifecycle state of a reminder attached to a memory. */
enum class ReminderStatus {
    SCHEDULED,
    ACTIVE,
    COMPLETED,
    CANCELLED,
}

/** Coarse confidence bucket derived from a numeric confidence score. */
enum class ConfidenceLevel {
    HIGH,
    MEDIUM,
    LOW,
}

/** How ambiguous an extraction result is; drives whether we must ask the user. */
enum class Ambiguity {
    NONE,
    LOW,
    MEDIUM,
    HIGH,
}
