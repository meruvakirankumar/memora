package com.meruvakirankumar.memora.presentation.common

import androidx.compose.ui.graphics.Color
import com.meruvakirankumar.memora.domain.model.EventType
import com.meruvakirankumar.memora.domain.model.MemoryStatus
import java.time.LocalDate

private val MONTHS = arrayOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

fun formatDate(date: LocalDate): String =
    "${MONTHS[date.monthValue - 1]} ${date.dayOfMonth}, ${date.year}"

fun EventType.displayLabel(): String = when (this) {
    EventType.EXPIRATION -> "Expiration"
    EventType.PAYMENT -> "Payment"
    EventType.RETURN -> "Return"
    EventType.WARRANTY -> "Warranty"
    EventType.RENEWAL -> "Renewal"
    EventType.DOCUMENT_EXPIRY -> "Document"
    EventType.GENERAL -> "General"
}

fun MemoryStatus.displayLabel(): String = when (this) {
    MemoryStatus.UPCOMING -> "Upcoming"
    MemoryStatus.REMINDER_ACTIVE -> "Reminder"
    MemoryStatus.DUE_TODAY -> "Due Today"
    MemoryStatus.OVERDUE -> "Overdue"
    MemoryStatus.COMPLETED -> "Completed"
}

fun MemoryStatus.badgeColor(): Color = when (this) {
    MemoryStatus.UPCOMING -> Color(0xFFD9E9F5)
    MemoryStatus.REMINDER_ACTIVE -> Color(0xFFD9E9F5)
    MemoryStatus.DUE_TODAY -> Color(0xFFF3BE4E)
    MemoryStatus.OVERDUE -> Color(0xFFF2B7A5)
    MemoryStatus.COMPLETED -> Color(0xFFCDE5C8)
}
