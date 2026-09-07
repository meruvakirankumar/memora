package com.meruvakirankumar.memora.platform.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.meruvakirankumar.memora.domain.model.MemoryStatus
import com.meruvakirankumar.memora.domain.repository.MemoryRepository
import com.meruvakirankumar.memora.domain.usecase.ScheduleReminderUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Reschedules the next reminder for every active memory after a device reboot. */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var memoryRepository: MemoryRepository
    @Inject lateinit var scheduleReminder: ScheduleReminderUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                memoryRepository.getAll()
                    .filter { it.status != MemoryStatus.COMPLETED }
                    .forEach { scheduleReminder(it.id) }
            } finally {
                pending.finish()
            }
        }
    }
}
