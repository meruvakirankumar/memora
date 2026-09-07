package com.meruvakirankumar.memora.domain.extraction

import java.time.LocalDate
import java.time.YearMonth

/**
 * Finds every date in a single line across many formats: ISO, textual months,
 * numeric slash/dot/dash, month-only, and separatorless. Ambiguous readings are
 * preserved as multiple options rather than being guessed.
 */
internal object DateTokenizer {

    fun tokens(line: String, permitBareMonthYear: Boolean): List<DateToken> {
        val upper = line.uppercase()
        val found = mutableListOf<DateToken>()
        val consumed = mutableListOf<IntRange>()

        val matchers = buildList {
            add(isoMatcher)
            add(textualDayMonthYear)
            add(textualMonthDayYear)
            add(textualMonthYear)
            add(numericFullDate)
            add(numericMonthYear)
            add(separatorless8)
            add(separatorless6)
            if (permitBareMonthYear) add(separatorless4)
        }

        for (matcher in matchers) {
            for (match in matcher.regex.findAll(upper)) {
                val range = match.range
                if (consumed.any { it.overlaps(range) }) continue
                val options = matcher.parse(match) ?: continue
                if (options.isEmpty()) continue
                found += DateToken(range, options)
                consumed += range
            }
        }

        return found.sortedBy { it.range.first }
    }

    private class Matcher(val regex: Regex, val parse: (MatchResult) -> List<DateInterpretation>?)

    private fun IntRange.overlaps(other: IntRange) = first <= other.last && other.first <= last

    // MARK: matchers

    private val isoMatcher = Matcher(Regex("\\b(\\d{4})[-/.](\\d{1,2})[-/.](\\d{1,2})\\b")) { m ->
        val (y, mo, d) = m.destructured
        makeDay(y.toInt(), mo.toInt(), d.toInt())
    }

    private val textualDayMonthYear = Matcher(
        Regex("\\b(\\d{1,2})(?:ST|ND|RD|TH)?[ .\\-]+$MONTHS[ .,\\-]+(\\d{2,4})\\b"),
    ) { m ->
        val day = m.groupValues[1].toInt()
        val month = monthToInt(m.groupValues[2]) ?: return@Matcher null
        makeDay(normalizeYear(m.groupValues[3].toInt()), month, day)
    }

    private val textualMonthDayYear = Matcher(
        Regex("\\b$MONTHS[ .\\-]+(\\d{1,2})(?:ST|ND|RD|TH)?[ .,\\-]+(\\d{2,4})\\b"),
    ) { m ->
        val month = monthToInt(m.groupValues[1]) ?: return@Matcher null
        val day = m.groupValues[2].toInt()
        makeDay(normalizeYear(m.groupValues[3].toInt()), month, day)
    }

    private val textualMonthYear = Matcher(
        Regex("\\b$MONTHS[ .,'/\\-]*(\\d{2,4})\\b"),
    ) { m ->
        val month = monthToInt(m.groupValues[1]) ?: return@Matcher null
        monthOnly(normalizeYear(m.groupValues[2].toInt()), month)
    }

    private val numericFullDate = Matcher(
        Regex("\\b(\\d{1,2})[/.\\-](\\d{1,2})[/.\\-](\\d{2,4})\\b"),
    ) { m ->
        val a = m.groupValues[1].toInt()
        val b = m.groupValues[2].toInt()
        val year = normalizeYear(m.groupValues[3].toInt())
        when {
            a in 1..31 && b in 1..12 && a > 12 -> makeDay(year, b, a)          // day/month
            a in 1..12 && b in 1..31 && b > 12 -> makeDay(year, a, b)          // month/day
            a in 1..12 && b in 1..12 -> {
                val monthDay = LocalDate.of(year, a, b)
                val dayMonth = LocalDate.of(year, b, a)
                if (monthDay == dayMonth) {
                    listOf(DateInterpretation(monthDay, hasExplicitDay = true))
                } else {
                    listOf(
                        DateInterpretation(monthDay, hasExplicitDay = true),
                        DateInterpretation(dayMonth, hasExplicitDay = true),
                    )
                }
            }
            else -> null
        }
    }

    private val numericMonthYear = Matcher(
        Regex("\\b(\\d{1,2})[/.\\-](\\d{2,4})\\b"),
    ) { m ->
        val month = m.groupValues[1].toInt()
        if (month !in 1..12) return@Matcher null
        monthOnly(normalizeYear(m.groupValues[2].toInt()), month)
    }

    private val separatorless8 = Matcher(Regex("\\b(\\d{8})\\b")) { m ->
        val s = m.groupValues[1]
        val ddmmyyyy = tryDay(s.take(2).toInt().let { d -> Triple(s.substring(4).toInt(), s.substring(2, 4).toInt(), d) })
        val yyyymmdd = tryDay(Triple(s.take(4).toInt(), s.substring(4, 6).toInt(), s.substring(6).toInt()))
        val mmddyyyy = tryDay(Triple(s.substring(4).toInt(), s.take(2).toInt(), s.substring(2, 4).toInt()))
        distinctOptions(listOfNotNull(ddmmyyyy, yyyymmdd, mmddyyyy))
    }

    private val separatorless6 = Matcher(Regex("\\b(\\d{6})\\b")) { m ->
        val s = m.groupValues[1]
        val ddmmyy = tryDay(Triple(normalizeYear(s.substring(4).toInt()), s.substring(2, 4).toInt(), s.take(2).toInt()))
        val mmddyy = tryDay(Triple(normalizeYear(s.substring(4).toInt()), s.take(2).toInt(), s.substring(2, 4).toInt()))
        val yymmdd = tryDay(Triple(normalizeYear(s.take(2).toInt()), s.substring(2, 4).toInt(), s.substring(4).toInt()))
        distinctOptions(listOfNotNull(ddmmyy, mmddyy, yymmdd))
    }

    private val separatorless4 = Matcher(Regex("\\b(\\d{4})\\b")) { m ->
        val s = m.groupValues[1]
        val month = s.take(2).toInt()
        if (month !in 1..12) return@Matcher null
        monthOnly(normalizeYear(s.substring(2).toInt()), month)
    }

    // MARK: helpers

    private fun makeDay(year: Int, month: Int, day: Int): List<DateInterpretation>? {
        val date = safeDate(year, month, day) ?: return null
        return listOf(DateInterpretation(date, hasExplicitDay = true))
    }

    private fun monthOnly(year: Int, month: Int): List<DateInterpretation>? {
        if (month !in 1..12 || year !in 1900..2200) return null
        return listOf(DateInterpretation(YearMonth.of(year, month).atEndOfMonth(), hasExplicitDay = false))
    }

    private fun tryDay(ymd: Triple<Int, Int, Int>): DateInterpretation? {
        val date = safeDate(ymd.first, ymd.second, ymd.third) ?: return null
        return DateInterpretation(date, hasExplicitDay = true)
    }

    private fun distinctOptions(options: List<DateInterpretation>): List<DateInterpretation>? {
        val distinct = options.distinctBy { it.date }
        return distinct.ifEmpty { null }
    }

    private fun safeDate(year: Int, month: Int, day: Int): LocalDate? {
        if (year !in 1900..2200 || month !in 1..12 || day !in 1..31) return null
        return try {
            LocalDate.of(year, month, day)
        } catch (e: Exception) {
            null
        }
    }

    private fun normalizeYear(year: Int): Int = if (year < 100) 2000 + year else year

    private fun monthToInt(name: String): Int? = MONTH_INDEX[name.take(3).uppercase()]

    private const val MONTHS =
        "(JAN(?:UARY)?|FEB(?:RUARY)?|MAR(?:CH)?|APR(?:IL)?|MAY|JUN(?:E)?|JUL(?:Y)?|AUG(?:UST)?|SEP(?:T(?:EMBER)?)?|OCT(?:OBER)?|NOV(?:EMBER)?|DEC(?:EMBER)?)"

    private val MONTH_INDEX = mapOf(
        "JAN" to 1, "FEB" to 2, "MAR" to 3, "APR" to 4, "MAY" to 5, "JUN" to 6,
        "JUL" to 7, "AUG" to 8, "SEP" to 9, "OCT" to 10, "NOV" to 11, "DEC" to 12,
    )
}
