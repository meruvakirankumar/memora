package com.meruvakirankumar.memora.domain.extraction

import com.meruvakirankumar.memora.domain.model.EventType
import java.time.LocalDate

/** The role a detected date plays, used to identify the actionable one. */
enum class DateRole {
    MANUFACTURE,
    EXPIRY,
    BILL_DUE,
    RETURN_DEADLINE,
    WARRANTY,
    SUBSCRIPTION,
    DOCUMENT,
    UNLABELED,
}

/** One concrete reading of a date token. */
data class DateInterpretation(
    val date: LocalDate,
    val hasExplicitDay: Boolean,
)

/** A date found in the text, with all plausible interpretations (>1 means ambiguous). */
data class DateToken(
    val range: IntRange,
    val options: List<DateInterpretation>,
) {
    val distinctDates: List<LocalDate> get() = options.map { it.date }.distinct()
    val hasExplicitDay: Boolean get() = options.any { it.hasExplicitDay }
    val monthOnly: Boolean get() = options.none { it.hasExplicitDay }
    val ambiguous: Boolean get() = distinctDates.size > 1
    val primary: LocalDate get() = options.first().date
}

/** A date token together with the role/event implied by its nearby label. */
data class LabeledDate(
    val role: DateRole,
    val eventType: EventType?,
    val token: DateToken,
)
