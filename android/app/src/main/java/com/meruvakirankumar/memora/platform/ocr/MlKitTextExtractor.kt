package com.meruvakirankumar.memora.platform.ocr

import android.content.Context
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.meruvakirankumar.memora.core.di.IoDispatcher
import com.meruvakirankumar.memora.core.error.AppError
import com.meruvakirankumar.memora.core.error.AppResult
import com.meruvakirankumar.memora.core.error.asFailure
import com.meruvakirankumar.memora.core.error.asSuccess
import com.meruvakirankumar.memora.domain.extraction.CapturedImageRef
import com.meruvakirankumar.memora.domain.extraction.OcrResult
import com.meruvakirankumar.memora.domain.extraction.TextExtractor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** ML Kit on-device OCR. Isolated in the platform layer so the domain never sees ML Kit. */
class MlKitTextExtractor @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TextExtractor {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun extract(image: CapturedImageRef): AppResult<OcrResult> = withContext(ioDispatcher) {
        try {
            val input = InputImage.fromFilePath(context, Uri.parse(image.uri))
            val result = Tasks.await(recognizer.process(input))
            val lines = result.textBlocks.flatMap { block -> block.lines.map { it.text } }
            OcrResult(rawText = result.text, lines = lines).asSuccess()
        } catch (e: Exception) {
            AppError.Recognition(e).asFailure()
        }
    }
}
