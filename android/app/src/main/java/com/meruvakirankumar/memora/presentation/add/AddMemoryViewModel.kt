package com.meruvakirankumar.memora.presentation.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meruvakirankumar.memora.core.error.AppResult
import com.meruvakirankumar.memora.domain.model.EventType
import com.meruvakirankumar.memora.domain.usecase.SaveMemoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject

@HiltViewModel
class AddMemoryViewModel @Inject constructor(
    private val saveMemory: SaveMemoryUseCase,
) : ViewModel() {

    var title by mutableStateOf("")
        private set
    var eventType by mutableStateOf(EventType.EXPIRATION)
        private set
    var dateText by mutableStateOf("")
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var saving by mutableStateOf(false)
        private set

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
            when (saveMemory(title, eventType, date)) {
                is AppResult.Success -> onSaved()
                is AppResult.Failure -> {
                    error = "Could not save. Check the details and try again."
                    saving = false
                }
            }
        }
    }

    private fun parseDate(value: String): LocalDate? = try {
        LocalDate.parse(value.trim())
    } catch (e: DateTimeParseException) {
        null
    }
}
