package com.meruvakirankumar.memora.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meruvakirankumar.memora.domain.model.MemoryStatus
import com.meruvakirankumar.memora.domain.model.MemoryWithReminder
import com.meruvakirankumar.memora.domain.service.MemoryStatusCalculator
import com.meruvakirankumar.memora.domain.usecase.CompleteMemoryUseCase
import com.meruvakirankumar.memora.domain.usecase.DeleteMemoryUseCase
import com.meruvakirankumar.memora.domain.usecase.ObserveMemoriesUseCase
import com.meruvakirankumar.memora.domain.usecase.ReopenMemoryUseCase
import com.meruvakirankumar.memora.domain.usecase.RestoreMemoryUseCase
import com.meruvakirankumar.memora.presentation.common.displayLabel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeMemories: ObserveMemoriesUseCase,
    private val completeMemory: CompleteMemoryUseCase,
    private val deleteMemory: DeleteMemoryUseCase,
    private val reopenMemory: ReopenMemoryUseCase,
    private val restoreMemory: RestoreMemoryUseCase,
    private val statusCalculator: MemoryStatusCalculator,
) : ViewModel() {

    private val query = MutableStateFlow("")
    val searchQuery: StateFlow<String> = query.asStateFlow()

    private var source: List<MemoryWithReminder> = emptyList()

    private val messageChannel = Channel<HomeMessage>(Channel.BUFFERED)
    val messages: Flow<HomeMessage> = messageChannel.receiveAsFlow()

    val uiState: StateFlow<HomeUiState> =
        combine(observeMemories().onEach { source = it }, query) { items, q -> buildState(items, q) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun complete(id: String) {
        viewModelScope.launch {
            completeMemory(id)
            messageChannel.send(HomeMessage("Marked complete", "Undo") { reopen(id) })
        }
    }

    fun delete(id: String) {
        val entry = source.firstOrNull { it.memory.id == id }
        viewModelScope.launch {
            deleteMemory(id)
            messageChannel.send(
                HomeMessage("Memory deleted", "Undo") { entry?.let { restore(it) } },
            )
        }
    }

    private fun reopen(id: String) {
        viewModelScope.launch { reopenMemory(id) }
    }

    private fun restore(entry: MemoryWithReminder) {
        viewModelScope.launch { restoreMemory(entry.memory, entry.reminder) }
    }

    private fun buildState(items: List<MemoryWithReminder>, rawQuery: String): HomeUiState {
        val trimmedQuery = rawQuery.trim()
        val mapped = items.map { entry ->
            val memory = entry.memory
            MemoryUi(
                id = memory.id,
                title = memory.title,
                eventType = memory.eventType,
                eventDate = memory.eventDate,
                status = statusCalculator.status(memory, entry.reminder?.reminderStartDate),
            )
        }

        val filtered = if (trimmedQuery.isEmpty()) {
            mapped
        } else {
            mapped.filter { ui ->
                ui.title.contains(trimmedQuery, ignoreCase = true) ||
                    ui.eventType.displayLabel().contains(trimmedQuery, ignoreCase = true)
            }
        }

        val active = filtered
            .filter { it.status != MemoryStatus.COMPLETED }
            .sortedWith(compareBy({ statusCalculator.urgency(it.status) }, { it.eventDate }))
        val completed = filtered.filter { it.status == MemoryStatus.COMPLETED }

        return HomeUiState(
            active = active,
            completed = completed,
            overdue = active.count { it.status == MemoryStatus.OVERDUE },
            dueToday = active.count { it.status == MemoryStatus.DUE_TODAY },
            upcoming = active.count {
                it.status == MemoryStatus.UPCOMING || it.status == MemoryStatus.REMINDER_ACTIVE
            },
            query = trimmedQuery,
            hasAnyMemories = items.isNotEmpty(),
        )
    }
}
