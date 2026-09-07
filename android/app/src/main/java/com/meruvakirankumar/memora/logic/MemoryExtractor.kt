package com.meruvakirankumar.memora.logic

import com.meruvakirankumar.memora.model.Confidence
import com.meruvakirankumar.memora.model.EventType
import com.meruvakirankumar.memora.model.MemoryCandidate
import java.time.LocalDate
import java.time.YearMonth

/**
 * Turns recognized text into a structured memory candidate.
 * Mirrors the Memora intent: understand, identify what matters, extract.
 */
object MemoryExtractor {

    private val LABELED_DATE = Regex(
        "(?:EXP|EXPIRES|EXPIRY|BEST BY|USE BY|DUE|PAY BY|RENEW(?:AL)?|RETURN BY)\\D*(\\d{1,2})[/-](\\d{1,2})(?:[/-](\\d{2,4}))?",
        RegexOption.IGNORE_CASE,
    )
    private val LOOSE_DATE = Regex("\\b(\\d{1,2})[/-](\\d{1,2})(?:[/-](\\d{2,4}))?\\b")
    private val INFORMATIONAL = Regex("\\b(MFG|BATCH|LOT|EXP|DUE|PAY|RENEW|RETURN)\\b", RegexOption.IGNORE_CASE)

    fun extract(text: String): MemoryCandidate {
        val normalized = text.trim()
        val lines = normalized.split(Regex("\\r?\\n")).map { it.trim() }.filter { it.isNotEmpty() }
        val eventType = detectEventType(normalized)
        val dateIso = extractActionDate(normalized)
        val title = extractTitle(lines, eventType)
        val hasExplicitDate = LABELED_DATE.containsMatchIn(normalized) || LOOSE_DATE.containsMatchIn(normalized)

        val confidence = when {
            eventType != EventType.GENERAL && hasExplicitDate -> Confidence.HIGH
            hasExplicitDate -> Confidence.MEDIUM
            else -> Confidence.LOW
        }

        return MemoryCandidate(
            title = title,
            eventType = eventType,
            dateIso = dateIso,
            confidence = confidence,
            sourceText = normalized,
        )
    }

    fun detectEventType(text: String): EventType {
        val upper = text.uppercase()
        return when {
            Regex("EXP|EXPIR|BEST BY|USE BY").containsMatchIn(upper) -> EventType.EXPIRATION
            Regex("DUE|PAY|BILL|INVOICE").containsMatchIn(upper) -> EventType.PAYMENT
            Regex("RENEW|SUBSCRIPTION|POLICY").containsMatchIn(upper) -> EventType.RENEWAL
            Regex("RETURN|EXCHANGE").containsMatchIn(upper) -> EventType.RETURN
            else -> EventType.GENERAL
        }
    }

    fun extractActionDate(text: String): String {
        LABELED_DATE.find(text)?.let { match ->
            return datePartsToIso(match.groupValues[1], match.groupValues[2], match.groupValues[3])
        }
        LOOSE_DATE.find(text)?.let { match ->
            return datePartsToIso(match.groupValues[1], match.groupValues[2], match.groupValues[3])
        }
        return LocalDate.now().plusDays(1).toString()
    }

    private fun datePartsToIso(first: String, second: String, yearPart: String): String {
        val month = first.toInt()
        val secondNumber = second.toInt()
        val currentYear = LocalDate.now().year
        val currentShortYear = currentYear % 100
        val hasYear = yearPart.isNotEmpty()
        val isLikelyMonthYear = !hasYear && secondNumber >= currentShortYear

        val year = when {
            hasYear -> normalizeYear(yearPart.toInt())
            isLikelyMonthYear -> normalizeYear(secondNumber)
            else -> currentYear
        }
        val day = if (hasYear || !isLikelyMonthYear) secondNumber else lastDayOfMonth(year, month)

        var date = safeDate(year, month, day)
        if (!hasYear && !isLikelyMonthYear && date.isBefore(LocalDate.now())) {
            date = date.plusYears(1)
        }
        return date.toString()
    }

    private fun normalizeYear(year: Int): Int = if (year < 100) 2000 + year else year

    private fun lastDayOfMonth(year: Int, month: Int): Int = YearMonth.of(year, month).lengthOfMonth()

    private fun safeDate(year: Int, month: Int, day: Int): LocalDate {
        val safeMonth = month.coerceIn(1, 12)
        val maxDay = YearMonth.of(year, safeMonth).lengthOfMonth()
        return LocalDate.of(year, safeMonth, day.coerceIn(1, maxDay))
    }

    private fun extractTitle(lines: List<String>, eventType: EventType): String {
        val candidateLine = lines.firstOrNull { !INFORMATIONAL.containsMatchIn(it) }
        if (candidateLine != null) {
            val cleaned = candidateLine.replace(Regex("[^a-zA-Z0-9 ]"), " ").trim()
            if (cleaned.isNotEmpty()) return toTitleCase(cleaned)
        }
        return "${eventType.label()} memory"
    }

    private fun toTitleCase(value: String): String =
        value.lowercase().split(" ").filter { it.isNotEmpty() }.joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
}
