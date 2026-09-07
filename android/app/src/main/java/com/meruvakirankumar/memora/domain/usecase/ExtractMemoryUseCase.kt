package com.meruvakirankumar.memora.domain.usecase

import com.meruvakirankumar.memora.core.error.AppResult
import com.meruvakirankumar.memora.core.error.asSuccess
import com.meruvakirankumar.memora.domain.extraction.CapturedImageRef
import com.meruvakirankumar.memora.domain.extraction.MemoryExtractor
import com.meruvakirankumar.memora.domain.extraction.OcrTextNormalizer
import com.meruvakirankumar.memora.domain.extraction.TextExtractor
import com.meruvakirankumar.memora.domain.model.ExtractedMemoryCandidate
import javax.inject.Inject

/**
 * Runs the extraction pipeline: OCR -> normalize -> deterministic analysis -> candidate.
 * The result is a suggestion the user must confirm, never an auto-created memory.
 */
class ExtractMemoryUseCase @Inject constructor(
    private val textExtractor: TextExtractor,
    private val normalizer: OcrTextNormalizer,
    private val memoryExtractor: MemoryExtractor,
) {
    suspend operator fun invoke(imageUri: String): AppResult<ExtractedMemoryCandidate> =
        when (val ocr = textExtractor.extract(CapturedImageRef(imageUri))) {
            is AppResult.Success -> {
                val normalized = normalizer.normalize(ocr.value.rawText)
                memoryExtractor.extract(normalized, ocr.value.rawText).asSuccess()
            }
            is AppResult.Failure -> ocr
        }
}
