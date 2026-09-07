package com.meruvakirankumar.memora.platform.image

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.meruvakirankumar.memora.core.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** Stores captured images in the app-private cache and exposes them via FileProvider. */
@Singleton
class AndroidTempImageStore @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TempImageStore {

    private val authority: String get() = "${context.packageName}.fileprovider"

    private fun captureDir(): File =
        File(context.cacheDir, CAPTURE_DIR).apply { mkdirs() }

    override suspend fun newCaptureUri(): String = withContext(ioDispatcher) {
        val file = File(captureDir(), "capture_${UUID.randomUUID()}.jpg")
        FileProvider.getUriForFile(context, authority, file).toString()
    }

    override suspend fun importImage(sourceUri: String): String = withContext(ioDispatcher) {
        val target = File(captureDir(), "import_${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(Uri.parse(sourceUri)).use { input ->
            requireNotNull(input) { "Unable to read selected image" }
            target.outputStream().use { output -> input.copyTo(output) }
        }
        FileProvider.getUriForFile(context, authority, target).toString()
    }

    override suspend fun delete(uri: String) = withContext(ioDispatcher) {
        runCatching {
            val name = Uri.parse(uri).lastPathSegment ?: return@runCatching
            File(captureDir(), File(name).name).takeIf { it.exists() }?.delete()
        }
        Unit
    }

    override suspend fun clear() = withContext(ioDispatcher) {
        captureDir().listFiles()?.forEach { it.delete() }
        Unit
    }

    private companion object {
        const val CAPTURE_DIR = "memora_capture"
    }
}
