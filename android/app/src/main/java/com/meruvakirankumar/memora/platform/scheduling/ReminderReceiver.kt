package com.meruvakirankumar.memora.platform.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.meruvakirankumar.memora.domain.model.Memory
import com.meruvakirankumar.memora.domain.model.MemoryStatus
import com.meruvakirankumar.memora.domain.repository.MemoryRepository
import com.meruvakirankumar.memora.domain.repository.ReminderRepository
import com.meruvakirankumar.memora.domain.service.MemoryStatusCalculator
import com.meruvakirankumar.memora.domain.usecase.ScheduleReminderUseCase
import com.meruvakirankumar.memora.platform.notification.Notifier
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** Fires when a reminder is due: posts the notification and schedules the next occurrence. */
@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var memoryRepository: MemoryRepository
    @Inject lateinit var reminderRepository: ReminderRepository
    @Inject lateinit var statusCalculator: MemoryStatusCalculator
    @Inject lateinit var scheduleReminder: ScheduleReminderUseCase
    @Inject lateinit var notifier: Notifier

    override fun onReceive(context: Context, intent: Intent) {
        val memoryId = intent.getStringExtra(EXTRA_MEMORY_ID) ?: return
        val pending = goAsync()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val memory = memoryRepository.getById(memoryId) ?: return@launch
                if (memory.status == MemoryStatus.COMPLETED) return@launch

                val reminder = reminderRepository.getByMemory(memoryId)
                val status = statusCalculator.status(memory, reminder?.reminderStartDate)
                notifier.show(memoryId.hashCode(), memory.title, bodyFor(memory, status))

                // Chain the next occurrence (next daily / overdue reminder).
                scheduleReminder(memoryId)
            } finally {
                pending.finish()
            }
        }
    }

    private fun bodyFor(memory: Memory, status: MemoryStatus): String {
        val event = memory.eventType.name.lowercase().replace('_', ' ')
        return when (status) {
            MemoryStatus.OVERDUE -> "Overdue: $event was due ${formatDate(memory.eventDate)}."
            MemoryStatus.DUE_TODAY -> "Due today: $event."
            else -> "${event.replaceFirstChar { it.uppercase() }} on ${formatDate(memory.eventDate)}."
        }
    }

    private fun formatDate(date: LocalDate): String {
        val months = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
        )
        return "${months[date.monthValue - 1]} ${date.dayOfMonth}, ${date.year}"
    }

    companion object {
        const val ACTION_REMIND = "com.meruvakirankumar.memora.REMIND"
        const val EXTRA_MEMORY_ID = "memoryId"
    }
}
