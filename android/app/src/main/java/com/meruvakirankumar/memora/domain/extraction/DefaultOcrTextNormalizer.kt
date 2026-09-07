package com.meruvakirankumar.memora.domain.extraction

import javax.inject.Inject

/** Conservative normalization: trims lines, drops blanks, collapses inner whitespace. */
class DefaultOcrTextNormalizer @Inject constructor() : OcrTextNormalizer {
    override fun normalize(rawText: String): String =
        rawText
            .split(Regex("\\r?\\n"))
            .map { it.trim().replace(Regex("[ \\t]+"), " ") }
            .filter { it.isNotEmpty() }
            .joinToString("\n")
}
