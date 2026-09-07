package com.meruvakirankumar.memora.presentation.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meruvakirankumar.memora.core.error.AppResult
import com.meruvakirankumar.memora.domain.model.ConfidenceLevel
import com.meruvakirankumar.memora.domain.model.DateResolution
import com.meruvakirankumar.memora.domain.model.EventType
import com.meruvakirankumar.memora.domain.model.ExtractedMemoryCandidate
import com.meruvakirankumar.memora.domain.usecase.ExtractMemoryUseCase
import com.meruvakirankumar.memora.domain.usecase.SaveMemoryUseCase
import com.meruvakirankumar.memora.platform.image.TempImageStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject

@HiltViewModel
class AddMemoryViewModel @Inject constructor(
    private val saveMemory: SaveMemoryUseCase,
    private val extractMemory: ExtractMemoryUseCase,
    private val tempImageStore: TempImageStore,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /** Temporary captured image tied to this confirmation; deleted after save or cancel. */
    val imageUri: String? = savedStateHandle.get<String>("imageUri")?.takeIf { it.isNotBlank() }

    var title by mutableStateOf("")
        private set
    var eventType by mutableStateOf(EventType.EXPIRY)
        private set
    var dateText by mutableStateOf("")
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var saving by mutableStateOf(false)
        private set
    var extracting by mutableStateOf(false)
        private set
    var extractionNote by mutableStateOf<String?>(null)
        private set
    var dateResolution by mutableStateOf(DateResolution.NONE)
        private set
    var dateOptions by mutableStateOf<List<LocalDate>>(emptyList())
        private set
    var reminderLeadDays by mutableStateOf(SaveMemoryUseCase.DEFAULT_LEAD_DAYS)
        private set

    fun onReminderLeadDaysChange(value: Int) {
        reminderLeadDays = value.coerceIn(SaveMemoryUseCase.MIN_LEAD_DAYS, SaveMemoryUseCase.MAX_LEAD_DAYS)
    }

    /** User resolved an ambiguous/multi date by tapping one of the options. */
    fun onPickDate(date: LocalDate) {
        dateText = date.toString()
        dateResolution = DateResolution.NONE
        dateOptions = emptyList()
        error = null
    }

    init {
        if (imageUri != null) runExtraction(imageUri)
    }

    private fun runExtraction(uri: String) {
        extracting = true
        viewModelScope.launch {
            when (val result = extractMemory(uri)) {
                is AppResult.Success -> applyCandidate(result.value)
                is AppResult.Failure ->
                    extractionNote = "Memora couldn't read the image. Please enter the details manually."
            }
            extracting = false
        }
    }

    private fun applyCandidate(candidate: ExtractedMemoryCandidate) {
        candidate.suggestedTitle?.let { title = it }
        candidate.suggestedEventType?.let { eventType = it }
        candidate.suggestedEventDate?.let { dateText = it.toString() }
        dateResolution = candidate.resolutionRequired
        dateOptions = candidate.dateOptions
        extractionNote = noteFor(candidate)
    }

    private fun noteFor(candidate: ExtractedMemoryCandidate): String = when (candidate.resolutionRequired) {
        DateResolution.PICK_ORDER ->
            "This date could be read two ways — tap the correct one."
        DateResolution.PICK_DAY ->
            "Only a month was detected — set the exact day."
        DateResolution.PICK_DATE ->
            "Memora couldn't pin down the date. Pick or enter it."
        DateResolution.NONE ->
            if (candidate.confidenceLevel == ConfidenceLevel.HIGH) {
                "Memora is confident. Review and confirm."
            } else {
                "Please review the details before confirming."
            }
    }

    fun onTitleChange(value: String) {
        title = value
        error = null
    }

    fun onEventTypeChange(value: EventType) {
        eventType = value
    }

    fun onDateChange(value: String) {
        dateText = value
        error = null
    }

    fun save(onSaved: () -> Unit) {
        val date = parseDate(dateText)
        if (date == null) {
            error = "Use the format YYYY-MM-DD."
            return
        }
        if (title.isBlank()) {
            error = "Give this memory a title."
            return
        }

        saving = true
        viewModelScope.launch {
            when (saveMemory(title, eventType, date, reminderLeadDays)) {
                is AppResult.Success -> {
                    deleteCapturedImage()
                    onSaved()
                }
                is AppResult.Failure -> {
                    error = "Could not save. Check the details and try again."
                    saving = false
                }
            }
        }
    }

    /** Called when the user cancels; the captured temp image must not linger. */
    fun discard(onDone: () -> Unit) {
        viewModelScope.launch {
            deleteCapturedImage()
            onDone()
        }
    }

    private suspend fun deleteCapturedImage() {
        imageUri?.let { tempImageStore.delete(it) }
    }

    private fun parseDate(value: String): LocalDate? = try {
        LocalDate.parse(value.trim())
    } catch (e: DateTimeParseException) {
        null
    }
}
