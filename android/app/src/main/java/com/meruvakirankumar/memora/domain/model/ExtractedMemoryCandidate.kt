package com.meruvakirankumar.memora.domain.model

import java.time.LocalDate

/**
 * The output of the extraction pipeline. Deliberately NOT a [Memory]: it is a
 * suggestion the user must confirm. This separation protects the confirm boundary.
 */
data class ExtractedMemoryCandidate(
    val suggestedTitle: String?,
    val suggestedEventType: EventType?,
    val suggestedEventDate: LocalDate?,
    val confidence: Float,
    val confidenceLevel: ConfidenceLevel,
    val ambiguity: Ambiguity,
    val resolutionRequired: DateResolution,
    val dateOptions: List<LocalDate>,
    val hasExplicitDay: Boolean,
    val rawOcrText: String,
    val normalizedText: String,
)

/** What the user must resolve before the date is trustworthy. */
enum class DateResolution {
    /** Confident single date; prefilled. */
    NONE,

    /** Two readings (e.g. MM/DD vs DD/MM); user picks the correct one from [dateOptions]. */
    PICK_ORDER,

    /** Only a month/year was found; user must supply the exact day. */
    PICK_DAY,

    /** No confident date, or several competing dates; user picks or enters one. */
    PICK_DATE,
}

