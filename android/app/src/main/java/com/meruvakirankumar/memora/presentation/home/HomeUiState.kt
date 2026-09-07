package com.meruvakirankumar.memora.presentation.home

import com.meruvakirankumar.memora.domain.model.EventType
import com.meruvakirankumar.memora.domain.model.MemoryStatus
import java.time.LocalDate

data class MemoryUi(
    val id: String,
    val title: String,
    val eventType: EventType,
    val eventDate: LocalDate,
    val status: MemoryStatus,
)

data class HomeUiState(
    val active: List<MemoryUi> = emptyList(),
    val completed: List<MemoryUi> = emptyList(),
    val overdue: Int = 0,
    val dueToday: Int = 0,
    val upcoming: Int = 0,
) {
    val isEmpty: Boolean get() = active.isEmpty() && completed.isEmpty()
}
