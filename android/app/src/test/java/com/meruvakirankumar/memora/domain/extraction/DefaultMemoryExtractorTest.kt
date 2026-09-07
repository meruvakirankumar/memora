package com.meruvakirankumar.memora.domain.extraction

import com.meruvakirankumar.memora.domain.model.Ambiguity
import com.meruvakirankumar.memora.domain.model.DateResolution
import com.meruvakirankumar.memora.domain.model.EventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DefaultMemoryExtractorTest {

    private val extractor = DefaultMemoryExtractor()

    @Test
    fun `picks the actionable expiry date over manufacturing date`() {
        val candidate = extractor.extract("MILK\nMFG 09/25\nBATCH 84921\nEXP 08/27", raw())

        assertEquals(EventType.EXPIRY, candidate.suggestedEventType)
        assertEquals(LocalDate.of(2027, 8, 31), candidate.suggestedEventDate)
        assertFalse(candidate.hasExplicitDay)
        assertEquals("Milk", candidate.suggestedTitle)
    }

    @Test
    fun `parses an explicit bill due date`() {
        val candidate = extractor.extract("ELECTRICITY BILL\nPAY BY 15/09/2026", raw())

        assertEquals(EventType.BILL_DUE, candidate.suggestedEventType)
        assertEquals(LocalDate.of(2026, 9, 15), candidate.suggestedEventDate)
    }

    @Test
    fun `never invents a date when none is present`() {
        val candidate = extractor.extract("JUST A LABEL", raw())

        assertNull(candidate.suggestedEventDate)
        assertEquals(Ambiguity.HIGH, candidate.ambiguity)
    }

    @Test
    fun `parses ISO date`() {
        val candidate = extractor.extract("VITAMINS\nUSE BY 2027-08-31", raw())

        assertEquals(EventType.USE_BY, candidate.suggestedEventType)
        assertEquals(LocalDate.of(2027, 8, 31), candidate.suggestedEventDate)
        assertTrue(candidate.hasExplicitDay)
        assertEquals(DateResolution.NONE, candidate.resolutionRequired)
    }

    @Test
    fun `parses textual day month year`() {
        val candidate = extractor.extract("PASSPORT\nVALID UNTIL 31 AUGUST 2030", raw())

        assertEquals(LocalDate.of(2030, 8, 31), candidate.suggestedEventDate)
        assertTrue(candidate.hasExplicitDay)
    }

    @Test
    fun `textual month-year asks for the exact day`() {
        val candidate = extractor.extract("CHEESE\nBEST BEFORE AUG 2027", raw())

        assertEquals(EventType.BEST_BEFORE, candidate.suggestedEventType)
        assertEquals(LocalDate.of(2027, 8, 31), candidate.suggestedEventDate)
        assertFalse(candidate.hasExplicitDay)
        assertEquals(DateResolution.PICK_DAY, candidate.resolutionRequired)
    }

    @Test
    fun `parses separatorless eight-digit day-first`() {
        val candidate = extractor.extract("EXP 31082027", raw())

        assertEquals(LocalDate.of(2027, 8, 31), candidate.suggestedEventDate)
        assertTrue(candidate.hasExplicitDay)
        assertEquals(DateResolution.NONE, candidate.resolutionRequired)
    }

    @Test
    fun `ambiguous day-month order asks the user to pick`() {
        val candidate = extractor.extract("EXP 05/06/27", raw())

        assertEquals(DateResolution.PICK_ORDER, candidate.resolutionRequired)
        assertTrue(candidate.dateOptions.contains(LocalDate.of(2027, 5, 6)))
        assertTrue(candidate.dateOptions.contains(LocalDate.of(2027, 6, 5)))
    }

    @Test
    fun `chooses expiry over manufacture with full dates`() {
        val candidate = extractor.extract("MFG 15/01/2025\nEXP 20/01/2027", raw())

        assertEquals(EventType.EXPIRY, candidate.suggestedEventType)
        assertEquals(LocalDate.of(2027, 1, 20), candidate.suggestedEventDate)
    }

    @Test
    fun `multiple unlabeled dates are not guessed`() {
        val candidate = extractor.extract("RECEIPT\n15/02/2026\n20/03/2026", raw())

        assertNull(candidate.suggestedEventDate)
        assertEquals(DateResolution.PICK_DATE, candidate.resolutionRequired)
        assertEquals(2, candidate.dateOptions.size)
    }

    private fun raw() = "raw"
}
