package com.meruvakirankumar.memora.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meruvakirankumar.memora.domain.model.MemoryStatus
import com.meruvakirankumar.memora.domain.model.MemoryWithReminder
import com.meruvakirankumar.memora.domain.service.MemoryStatusCalculator
import com.meruvakirankumar.memora.domain.usecase.CompleteMemoryUseCase
import com.meruvakirankumar.memora.domain.usecase.DeleteMemoryUseCase
import com.meruvakirankumar.memora.domain.usecase.ObserveMemoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeMemories: ObserveMemoriesUseCase,
    private val completeMemory: CompleteMemoryUseCase,
    private val deleteMemory: DeleteMemoryUseCase,
    private val statusCalculator: MemoryStatusCalculator,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = observeMemories()
        .map { items -> buildState(items) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun complete(id: String) {
        viewModelScope.launch { completeMemory(id) }
    }

    fun delete(id: String) {
        viewModelScope.launch { deleteMemory(id) }
    }

    private fun buildState(source: List<MemoryWithReminder>): HomeUiState {
        val items = source.map { entry ->
            val memory = entry.memory
            MemoryUi(
                id = memory.id,
                title = memory.title,
                eventType = memory.eventType,
                eventDate = memory.eventDate,
                status = statusCalculator.status(memory, entry.reminder?.reminderStartDate),
            )
        }

        val active = items
            .filter { it.status != MemoryStatus.COMPLETED }
            .sortedWith(compareBy({ statusCalculator.urgency(it.status) }, { it.eventDate }))
        val completed = items.filter { it.status == MemoryStatus.COMPLETED }

        return HomeUiState(
            active = active,
            completed = completed,
            overdue = active.count { it.status == MemoryStatus.OVERDUE },
            dueToday = active.count { it.status == MemoryStatus.DUE_TODAY },
            upcoming = active.count { it.status == MemoryStatus.UPCOMING || it.status == MemoryStatus.REMINDER_ACTIVE },
        )
    }
}
