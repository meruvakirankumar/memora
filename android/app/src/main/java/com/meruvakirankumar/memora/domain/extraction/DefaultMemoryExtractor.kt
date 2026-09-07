package com.meruvakirankumar.memora.domain.extraction

import com.meruvakirankumar.memora.domain.model.Ambiguity
import com.meruvakirankumar.memora.domain.model.ConfidenceLevel
import com.meruvakirankumar.memora.domain.model.DateAlternative
import com.meruvakirankumar.memora.domain.model.EventType
import com.meruvakirankumar.memora.domain.model.ExtractedMemoryCandidate
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * Deterministic, pattern-based extractor. Core rule: never invent an actionable date.
 * When a date is missing or the day is unknown, that uncertainty is reported (ambiguity /
 * hasExplicitDay), never silently fabricated.
 */
class DefaultMemoryExtractor @Inject constructor() : MemoryExtractor {

    override fun extract(normalizedText: String, rawText: String): ExtractedMemoryCandidate {
        val upper = normalizedText.uppercase()
        val lines = normalizedText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val upperLines = lines.map { it.uppercase() }

        val eventType = detectEventType(upper)
        val parsed = parseDate(upperLines)
        val title = extractTitle(lines)

        val confidence = scoreConfidence(eventType, parsed)
        val level = when {
            confidence >= 0.7f -> ConfidenceLevel.HIGH
            confidence >= 0.4f -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }
        val ambiguity = when {
            parsed.date == null -> Ambiguity.HIGH
            parsed.ambiguousOrder -> Ambiguity.MEDIUM
            !parsed.hasExplicitDay -> Ambiguity.LOW
            else -> Ambiguity.NONE
        }

        return ExtractedMemoryCandidate(
            suggestedTitle = title,
            suggestedEventType = eventType.takeIf { it != EventType.GENERAL },
            suggestedEventDate = parsed.date,
            confidence = confidence,
            confidenceLevel = level,
            ambiguity = ambiguity,
            alternatives = parsed.alternatives,
            hasExplicitDay = parsed.hasExplicitDay,
            rawOcrText = rawText,
            normalizedText = normalizedText,
        )
    }

    private fun detectEventType(upper: String): EventType = when {
        Regex("USE BY").containsMatchIn(upper) -> EventType.USE_BY
        Regex("BEST BEFORE|BEST BY|\\bBB\\b").containsMatchIn(upper) -> EventType.BEST_BEFORE
        Regex("\\bEXP|EXPIR").containsMatchIn(upper) -> EventType.EXPIRY
        Regex("PAY BY|AMOUNT DUE|\\bDUE\\b|\\bBILL\\b|INVOICE").containsMatchIn(upper) -> EventType.BILL_DUE
        Regex("RETURN|EXCHANGE").containsMatchIn(upper) -> EventType.RETURN_DEADLINE
        Regex("WARRANTY|GUARANTEE").containsMatchIn(upper) -> EventType.WARRANTY_END
        Regex("SUBSCRIPTION|RENEW").containsMatchIn(upper) -> EventType.SUBSCRIPTION_END
        Regex("PASSPORT|LICEN[SC]E|VALID (THRU|UNTIL|TILL)").containsMatchIn(upper) -> EventType.DOCUMENT_EXPIRY
        else -> EventType.GENERAL
    }

    private data class ParsedDate(
        val date: LocalDate?,
        val hasExplicitDay: Boolean,
        val ambiguousOrder: Boolean,
        val alternatives: List<DateAlternative>,
    )

    private fun parseDate(upperLines: List<String>): ParsedDate {
        // 1. Prefer a date on a line carrying an actionable label (EXP, DUE, USE BY...).
        upperLines.forEach { line ->
            if (actionableRegex.containsMatchIn(line)) {
                findDateInLine(line)?.let { return it }
            }
        }
        // 2. Otherwise consider dates on lines that are NOT manufacturing/batch info.
        val candidates = upperLines
            .filter { !nonActionableRegex.containsMatchIn(it) }
            .mapNotNull { findDateInLine(it) }
        val distinctDates = candidates.mapNotNull { it.date }.distinct()
        // A single clear date is usable; multiple or none stays unresolved (ask the user).
        return if (distinctDates.size == 1) {
            candidates.first { it.date == distinctDates.first() }
        } else {
            noDate()
        }
    }

    private fun findDateInLine(line: String): ParsedDate? {
        fullDateRegex.find(line)?.let { match ->
            return interpretFullDate(match.groupValues[1], match.groupValues[2], match.groupValues[3])
        }
        monthYearRegex.find(line)?.let { match ->
            return interpretMonthYear(match.groupValues[1], match.groupValues[2])
        }
        return null
    }

    private fun noDate() = ParsedDate(date = null, hasExplicitDay = false, ambiguousOrder = false, alternatives = emptyList())

    private fun interpretFullDate(a: String, b: String, c: String): ParsedDate {
        val first = a.toInt()
        val second = b.toInt()
        val year = normalizeYear(c.toInt())

        return when {
            first > 12 && second <= 12 -> {
                // Day/Month/Year
                ParsedDate(safeDate(year, second, first), hasExplicitDay = true, ambiguousOrder = false, alternatives = emptyList())
            }
            second > 12 && first <= 12 -> {
                // Month/Day/Year
                ParsedDate(safeDate(year, first, second), hasExplicitDay = true, ambiguousOrder = false, alternatives = emptyList())
            }
            first <= 12 && second <= 12 -> {
                // Ambiguous order: default Month/Day, offer Day/Month as an alternative.
                val primary = safeDate(year, first, second)
                val alternative = safeDate(year, second, first)
                ParsedDate(
                    date = primary,
                    hasExplicitDay = true,
                    ambiguousOrder = primary != alternative,
                    alternatives = if (primary != alternative) {
                        listOf(DateAlternative(alternative, null, 0.5f))
                    } else {
                        emptyList()
                    },
                )
            }
            else -> ParsedDate(date = null, hasExplicitDay = false, ambiguousOrder = false, alternatives = emptyList())
        }
    }

    private fun interpretMonthYear(monthPart: String, yearPart: String): ParsedDate {
        val month = monthPart.toInt()
        if (month !in 1..12) {
            return ParsedDate(date = null, hasExplicitDay = false, ambiguousOrder = false, alternatives = emptyList())
        }
        val year = normalizeYear(yearPart.toInt())
        // Month-only: represent as end of month but flag that the day is not explicit.
        val representative = YearMonth.of(year, month).atEndOfMonth()
        return ParsedDate(date = representative, hasExplicitDay = false, ambiguousOrder = false, alternatives = emptyList())
    }

    private fun scoreConfidence(eventType: EventType, parsed: ParsedDate): Float {
        var score = 0f
        if (eventType != EventType.GENERAL) score += 0.4f
        if (parsed.date != null) score += if (parsed.hasExplicitDay) 0.4f else 0.2f
        if (parsed.ambiguousOrder) score -= 0.1f
        return score.coerceIn(0f, 1f)
    }

    private fun normalizeYear(year: Int): Int = if (year < 100) 2000 + year else year

    private fun safeDate(year: Int, month: Int, day: Int): LocalDate {
        val safeMonth = month.coerceIn(1, 12)
        val maxDay = YearMonth.of(year, safeMonth).lengthOfMonth()
        return LocalDate.of(year, safeMonth, day.coerceIn(1, maxDay))
    }

    private fun extractTitle(lines: List<String>): String? {
        val line = lines.firstOrNull { !informationalRegex.containsMatchIn(it.uppercase()) } ?: return null
        val cleaned = line.replace(Regex("[^A-Za-z0-9 ]"), " ").trim().replace(Regex(" +"), " ")
        if (cleaned.isEmpty()) return null
        return cleaned.split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }

    private companion object {
        val fullDateRegex = Regex("\\b(\\d{1,2})[/.\\-](\\d{1,2})[/.\\-](\\d{2,4})\\b")
        val monthYearRegex = Regex("\\b(\\d{1,2})[/.\\-](\\d{2,4})\\b")
        val actionableRegex = Regex("EXP|EXPIR|BEST BEFORE|BEST BY|USE BY|\\bDUE\\b|PAY BY|RETURN|VALID (THRU|UNTIL|TILL)|WARRANTY|RENEW|SUBSCRIPTION")
        val nonActionableRegex = Regex("\\b(MFG|MANUFACTUR|PACKED|PKD|BATCH|LOT|SN|SERIAL)\\b")
        val informationalRegex = Regex("\\b(MFG|MANUFACTURED|BATCH|LOT|EXP|EXPIR|BEST|USE BY|DUE|PAY|BILL|INVOICE|RETURN|WARRANTY|SUBSCRIPTION|RENEW|VALID|BB)\\b")
    }
}
