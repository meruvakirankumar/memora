package com.meruvakirankumar.memora.presentation.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meruvakirankumar.memora.core.error.AppResult
import com.meruvakirankumar.memora.domain.model.EventType
import com.meruvakirankumar.memora.domain.model.MemoryStatus
import com.meruvakirankumar.memora.domain.model.MemoryWithReminder
import com.meruvakirankumar.memora.domain.service.MemoryStatusCalculator
import com.meruvakirankumar.memora.domain.usecase.CompleteMemoryUseCase
import com.meruvakirankumar.memora.domain.usecase.DeleteMemoryUseCase
import com.meruvakirankumar.memora.domain.usecase.EditMemoryUseCase
import com.meruvakirankumar.memora.domain.usecase.ObserveMemoryUseCase
import com.meruvakirankumar.memora.domain.usecase.ReopenMemoryUseCase
import com.meruvakirankumar.memora.domain.usecase.SaveMemoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class MemoryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeMemory: ObserveMemoryUseCase,
    private val editMemory: EditMemoryUseCase,
    private val completeMemory: CompleteMemoryUseCase,
    private val reopenMemory: ReopenMemoryUseCase,
    private val deleteMemory: DeleteMemoryUseCase,
    private val statusCalculator: MemoryStatusCalculator,
) : ViewModel() {

    private val memoryId: String = requireNotNull(savedStateHandle["memoryId"])

    var title by mutableStateOf("")
        private set
    var eventType by mutableStateOf(EventType.EXPIRY)
        private set
    var dateText by mutableStateOf("")
        private set
    var reminderLeadDays by mutableStateOf(SaveMemoryUseCase.DEFAULT_LEAD_DAYS)
        private set
    var status by mutableStateOf<MemoryStatus?>(null)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var saving by mutableStateOf(false)
        private set
    var missing by mutableStateOf(false)
        private set

    private var hydrated = false

    init {
        viewModelScope.launch {
            observeMemory(memoryId).collect { detail -> onDetail(detail) }
        }
    }

    private fun onDetail(detail: MemoryWithReminder?) {
        if (detail == null) {
            missing = true
            return
        }
        status = statusCalculator.status(detail.memory, detail.reminder?.reminderStartDate)
        if (!hydrated) {
            title = detail.memory.title
            eventType = detail.memory.eventType
            dateText = detail.memory.eventDate.toString()
            reminderLeadDays = detail.reminder?.let {
                ChronoUnit.DAYS.between(it.reminderStartDate, detail.memory.eventDate).toInt()
            }?.coerceIn(SaveMemoryUseCase.MIN_LEAD_DAYS, SaveMemoryUseCase.MAX_LEAD_DAYS)
                ?: SaveMemoryUseCase.DEFAULT_LEAD_DAYS
            hydrated = true
        }
    }

    val isCompleted: Boolean get() = status == MemoryStatus.COMPLETED

    fun onTitleChange(value: String) { title = value; error = null }
    fun onEventTypeChange(value: EventType) { eventType = value }
    fun onDateChange(value: String) { dateText = value; error = null }
    fun onPickDate(date: LocalDate) { dateText = date.toString(); error = null }
    fun onReminderLeadDaysChange(value: Int) {
        reminderLeadDays = value.coerceIn(SaveMemoryUseCase.MIN_LEAD_DAYS, SaveMemoryUseCase.MAX_LEAD_DAYS)
    }

    fun save(onDone: () -> Unit) {
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
            when (editMemory(memoryId, title, eventType, date, reminderLeadDays)) {
                is AppResult.Success -> onDone()
                is AppResult.Failure -> {
                    error = "Could not save changes. Try again."
                    saving = false
                }
            }
        }
    }

    fun complete(onDone: () -> Unit) {
        viewModelScope.launch { completeMemory(memoryId); onDone() }
    }

    fun reopen(onDone: () -> Unit) {
        viewModelScope.launch { reopenMemory(memoryId); onDone() }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch { deleteMemory(memoryId); onDone() }
    }

    private fun parseDate(value: String): LocalDate? = try {
        LocalDate.parse(value.trim())
    } catch (e: DateTimeParseException) {
        null
    }
}
