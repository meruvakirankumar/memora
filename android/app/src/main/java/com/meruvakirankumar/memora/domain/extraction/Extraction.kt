package com.meruvakirankumar.memora.domain.extraction

import com.meruvakirankumar.memora.core.error.AppResult
import com.meruvakirankumar.memora.domain.model.ExtractedMemoryCandidate

/** Reference to a captured image the extraction pipeline can read. */
data class CapturedImageRef(val uri: String)

/** Raw OCR output. */
data class OcrResult(
    val rawText: String,
    val lines: List<String>,
)

/**
 * Turns a captured image into raw recognized text. Implemented by the platform
 * layer (Stage 3), never called directly by the UI.
 */
interface TextExtractor {
    suspend fun extract(image: CapturedImageRef): AppResult<OcrResult>
}

/** Cleans and normalizes raw OCR text before semantic analysis. */
interface OcrTextNormalizer {
    fun normalize(rawText: String): String
}

/**
 * Deterministic-first semantic analyzer that produces a candidate the user must
 * confirm. Must never invent a date: when uncertain it raises ambiguity instead.
 */
interface MemoryExtractor {
    fun extract(normalizedText: String, rawText: String): ExtractedMemoryCandidate
}
