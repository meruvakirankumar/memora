package com.meruvakirankumar.memora.domain.extraction

import com.meruvakirankumar.memora.domain.model.Ambiguity
import com.meruvakirankumar.memora.domain.model.ConfidenceLevel
import com.meruvakirankumar.memora.domain.model.DateResolution
import com.meruvakirankumar.memora.domain.model.EventType
import com.meruvakirankumar.memora.domain.model.ExtractedMemoryCandidate
import java.time.LocalDate
import javax.inject.Inject

/**
 * Deterministic extractor: scans every date, classifies each by its nearby label,
 * selects the actionable one (never a manufacturing date, never fabricated), and
 * reports what the user must resolve (order, day, or choice).
 */
class DefaultMemoryExtractor @Inject constructor() : MemoryExtractor {

    override fun extract(normalizedText: String, rawText: String): ExtractedMemoryCandidate {
        val lines = normalizedText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val labeledDates = collectLabeledDates(lines)
        val selection = select(labeledDates)
        val eventType = selection.eventType ?: fallbackEventType(normalizedText.uppercase())
        val title = extractTitle(lines)

        val level = when {
            selection.confidence >= 0.7f -> ConfidenceLevel.HIGH
            selection.confidence >= 0.4f -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }

        return ExtractedMemoryCandidate(
            suggestedTitle = title,
            suggestedEventType = eventType.takeIf { it != EventType.GENERAL },
            suggestedEventDate = selection.date,
            confidence = selection.confidence,
            confidenceLevel = level,
            ambiguity = selection.ambiguity,
            resolutionRequired = selection.resolution,
            dateOptions = selection.options,
            hasExplicitDay = selection.hasExplicitDay,
            rawOcrText = rawText,
            normalizedText = normalizedText,
        )
    }

    private fun collectLabeledDates(lines: List<String>): List<LabeledDate> {
        val result = mutableListOf<LabeledDate>()
        for (line in lines) {
            val (role, eventType) = DateRoleClassifier.classify(line.uppercase())
            val permitBare = role in ACTIONABLE_ROLES
            DateTokenizer.tokens(line, permitBareMonthYear = permitBare).forEach { token ->
                result += LabeledDate(role, eventType, token)
            }
        }
        return result
    }

    private fun select(labeled: List<LabeledDate>): Selection {
        if (labeled.isEmpty()) return Selection.noDate()

        val manufactureDates = labeled
            .filter { it.role == DateRole.MANUFACTURE }
            .flatMap { it.token.distinctDates }

        for (role in ROLE_PRIORITY) {
            val forRole = labeled.filter { it.role == role }
            if (forRole.isNotEmpty()) return resolve(forRole, role, manufactureDates)
        }

        val unlabeled = labeled.filter { it.role == DateRole.UNLABELED }
        if (unlabeled.isNotEmpty()) return resolve(unlabeled, DateRole.UNLABELED, manufactureDates)

        // Only manufacturing dates present: nothing actionable to remember.
        return Selection.noDate()
    }

    private fun resolve(
        candidates: List<LabeledDate>,
        role: DateRole,
        manufactureDates: List<LocalDate>,
    ): Selection {
        val eventType = candidates.firstNotNullOfOrNull { it.eventType }
        val tokens = candidates.map { it.token }
        val distinctDates = tokens.flatMap { it.distinctDates }.distinct()

        if (distinctDates.size > 1) {
            // For an expiry, the actionable date is the latest — but only if every reading is unambiguous.
            val allSimple = tokens.all { !it.ambiguous && !it.monthOnly }
            if (role == DateRole.EXPIRY && allSimple) {
                return Selection.single(distinctDates.max(), hasExplicitDay = true, eventType, manufactureDates)
            }
            return Selection.choose(distinctDates.sorted(), eventType)
        }

        val token = tokens.first { it.distinctDates.isNotEmpty() }
        return when {
            token.ambiguous -> Selection.pickOrder(token.distinctDates, token.primary, eventType)
            token.monthOnly -> Selection.pickDay(token.primary, eventType)
            else -> Selection.single(token.primary, hasExplicitDay = true, eventType, manufactureDates)
        }
    }

    private fun fallbackEventType(upper: String): EventType =
        DateRoleClassifier.classify(upper).second ?: EventType.GENERAL

    private fun extractTitle(lines: List<String>): String? {
        val line = lines.firstOrNull { !INFORMATIONAL.containsMatchIn(it.uppercase()) } ?: return null
        val cleaned = line.replace(Regex("[^A-Za-z0-9 ]"), " ").trim().replace(Regex(" +"), " ")
        if (cleaned.isEmpty()) return null
        return cleaned.split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }

    /** Intermediate selection result before mapping to a candidate. */
    private data class Selection(
        val date: LocalDate?,
        val hasExplicitDay: Boolean,
        val eventType: EventType?,
        val resolution: DateResolution,
        val options: List<LocalDate>,
        val ambiguity: Ambiguity,
        val confidence: Float,
    ) {
        companion object {
            fun noDate() = Selection(
                date = null,
                hasExplicitDay = false,
                eventType = null,
                resolution = DateResolution.PICK_DATE,
                options = emptyList(),
                ambiguity = Ambiguity.HIGH,
                confidence = 0f,
            )

            fun single(
                date: LocalDate,
                hasExplicitDay: Boolean,
                eventType: EventType?,
                mfg: List<LocalDate>,
            ): Selection {
                val conflict = mfg.any { !it.isBefore(date) } // expiry must be after manufacture
                val base = 0.5f + if (eventType != null) 0.4f else 0f
                return Selection(
                    date = date,
                    hasExplicitDay = hasExplicitDay,
                    eventType = eventType,
                    resolution = DateResolution.NONE,
                    options = listOf(date),
                    ambiguity = if (conflict) Ambiguity.MEDIUM else Ambiguity.NONE,
                    confidence = (base - if (conflict) 0.3f else 0f).coerceIn(0f, 1f),
                )
            }

            fun pickOrder(options: List<LocalDate>, preview: LocalDate, eventType: EventType?) = Selection(
                date = preview,
                hasExplicitDay = true,
                eventType = eventType,
                resolution = DateResolution.PICK_ORDER,
                options = options,
                ambiguity = Ambiguity.MEDIUM,
                confidence = 0.45f + if (eventType != null) 0.1f else 0f,
            )

            fun pickDay(monthEnd: LocalDate, eventType: EventType?) = Selection(
                date = monthEnd,
                hasExplicitDay = false,
                eventType = eventType,
                resolution = DateResolution.PICK_DAY,
                options = listOf(monthEnd),
                ambiguity = Ambiguity.LOW,
                confidence = 0.5f + if (eventType != null) 0.1f else 0f,
            )

            fun choose(options: List<LocalDate>, eventType: EventType?) = Selection(
                date = null,
                hasExplicitDay = false,
                eventType = eventType,
                resolution = DateResolution.PICK_DATE,
                options = options,
                ambiguity = Ambiguity.HIGH,
                confidence = 0.3f,
            )
        }
    }

    private companion object {
        val ROLE_PRIORITY = listOf(
            DateRole.EXPIRY,
            DateRole.BILL_DUE,
            DateRole.RETURN_DEADLINE,
            DateRole.WARRANTY,
            DateRole.SUBSCRIPTION,
            DateRole.DOCUMENT,
        )
        val ACTIONABLE_ROLES = ROLE_PRIORITY.toSet()
        val INFORMATIONAL = Regex(
            "\\b(MFG|MANUFACTURED|BATCH|LOT|EXP|EXPIR|BEST|USE BY|DUE|PAY|BILL|INVOICE|RETURN|WARRANTY|SUBSCRIPTION|RENEW|VALID|BB)\\b",
        )
    }
}
