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
    val alternatives: List<DateAlternative>,
    val hasExplicitDay: Boolean,
    val rawOcrText: String,
    val normalizedText: String,
)

/** An alternative interpretation the user can choose during confirmation. */
data class DateAlternative(
    val date: LocalDate,
    val eventType: EventType?,
    val confidence: Float,
)
