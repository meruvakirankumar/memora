package com.meruvakirankumar.memora.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import com.meruvakirankumar.memora.data.MemoryRepository
import com.meruvakirankumar.memora.logic.MemoryExtractor
import com.meruvakirankumar.memora.logic.MemoryStatusResolver
import com.meruvakirankumar.memora.model.EventType
import com.meruvakirankumar.memora.model.Memory
import com.meruvakirankumar.memora.model.MemoryCandidate
import com.meruvakirankumar.memora.model.MemoryStatus
import com.meruvakirankumar.memora.notifications.ReminderScheduler
import java.time.Instant

private const val SAMPLE_OCR = "MILK\nMFG 09/25\nBATCH 84921\nEXP 08/27"

class MemoraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MemoryRepository(application)
    private val reminderScheduler = ReminderScheduler(application)

    private val memories = mutableStateListOf<Memory>()

    var sourceText by mutableStateOf(SAMPLE_OCR)
        private set
    var candidate by mutableStateOf(MemoryExtractor.extract(SAMPLE_OCR))
        private set
    var imageBitmap by mutableStateOf<ImageBitmap?>(null)
        private set
    var dateError by mutableStateOf(false)
        private set

    init {
        memories.addAll(repository.load())
    }

    val activeMemories: List<Memory>
        get() = memories
            .filter { MemoryStatusResolver.statusFor(it) != MemoryStatus.COMPLETED }
            .sortedWith(
                compareBy(
                    { MemoryStatusResolver.urgency(MemoryStatusResolver.statusFor(it)) },
                    { it.dateIso },
                ),
            )

    val completedMemories: List<Memory>
        get() = memories.filter { MemoryStatusResolver.statusFor(it) == MemoryStatus.COMPLETED }

    fun summary(): Triple<Int, Int, Int> {
        var overdue = 0
        var dueToday = 0
        var upcoming = 0
        activeMemories.forEach {
            when (MemoryStatusResolver.statusFor(it)) {
                MemoryStatus.OVERDUE -> overdue++
                MemoryStatus.DUE_TODAY -> dueToday++
                MemoryStatus.UPCOMING -> upcoming++
                MemoryStatus.COMPLETED -> Unit
            }
        }
        return Triple(overdue, dueToday, upcoming)
    }

    fun onSourceTextChanged(value: String) {
        sourceText = value
    }

    fun onImageSelected(bitmap: ImageBitmap?) {
        imageBitmap = bitmap
        candidate = MemoryExtractor.extract(sourceText)
    }

    fun analyze() {
        candidate = MemoryExtractor.extract(sourceText)
    }

    fun updateTitle(value: String) {
        candidate = candidate.copy(title = value)
    }

    fun updateEventType(value: EventType) {
        candidate = candidate.copy(eventType = value)
    }

    fun updateDate(value: String) {
        candidate = candidate.copy(dateIso = value)
        dateError = false
    }

    fun candidateStatus(): MemoryStatus =
        if (MemoryStatusResolver.isValidIsoDate(candidate.dateIso)) {
            MemoryStatusResolver.statusForDate(candidate.dateIso)
        } else {
            MemoryStatus.UPCOMING
        }

    fun confirm(): Boolean {
        if (!MemoryStatusResolver.isValidIsoDate(candidate.dateIso)) {
            dateError = true
            return false
        }

        val notificationId = reminderScheduler.schedule(candidate)
        val memory = Memory(
            id = System.currentTimeMillis().toString(),
            title = candidate.title,
            eventType = candidate.eventType,
            dateIso = candidate.dateIso,
            confidence = candidate.confidence,
            sourceText = candidate.sourceText,
            createdAtIso = Instant.now().toString(),
            notificationId = notificationId,
        )
        memories.add(0, memory)
        persist()
        resetCapture()
        return true
    }

    fun complete(memoryId: String) {
        val index = memories.indexOfFirst { it.id == memoryId }
        if (index < 0) return
        val target = memories[index]
        target.notificationId?.let { reminderScheduler.cancel(it) }
        memories[index] = target.copy(completedAtIso = Instant.now().toString())
        persist()
    }

    fun delete(memoryId: String) {
        val target = memories.firstOrNull { it.id == memoryId } ?: return
        target.notificationId?.let { reminderScheduler.cancel(it) }
        memories.remove(target)
        persist()
    }

    private fun resetCapture() {
        sourceText = SAMPLE_OCR
        candidate = MemoryExtractor.extract(SAMPLE_OCR)
        imageBitmap = null
    }

    private fun persist() {
        repository.save(memories.toList())
    }
}
