package com.meruvakirankumar.memora.domain.extraction

import com.meruvakirankumar.memora.domain.model.Ambiguity
import com.meruvakirankumar.memora.domain.model.EventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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

    private fun raw() = "raw"
}
