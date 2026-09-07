package com.meruvakirankumar.memora.presentation.capture

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meruvakirankumar.memora.platform.image.TempImageStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val tempImageStore: TempImageStore,
) : ViewModel() {

    var capturedUri by mutableStateOf<String?>(null)
        private set
    var busy by mutableStateOf(false)
        private set

    private var pendingCameraUri: String? = null

    fun prepareCameraTarget(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val uri = tempImageStore.newCaptureUri()
            pendingCameraUri = uri
            onReady(uri)
        }
    }

    fun onCameraResult(success: Boolean) {
        val pending = pendingCameraUri
        pendingCameraUri = null
        if (success && pending != null) {
            discardCurrent()
            capturedUri = pending
        } else if (pending != null) {
            viewModelScope.launch { tempImageStore.delete(pending) }
        }
    }

    fun onExternalPicked(sourceUri: String?) {
        if (sourceUri == null) return
        busy = true
        viewModelScope.launch {
            discardCurrent()
            capturedUri = tempImageStore.importImage(sourceUri)
            busy = false
        }
    }

    /** Deletes the current capture; call when the user leaves without continuing. */
    fun discard() {
        discardCurrent()
        capturedUri = null
    }

    private fun discardCurrent() {
        capturedUri?.let { uri -> viewModelScope.launch { tempImageStore.delete(uri) } }
    }
}
