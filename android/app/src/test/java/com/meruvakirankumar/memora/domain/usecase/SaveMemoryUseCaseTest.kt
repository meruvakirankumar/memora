package com.meruvakirankumar.memora.domain.usecase

import com.meruvakirankumar.memora.core.error.AppResult
import com.meruvakirankumar.memora.core.time.FixedClock
import com.meruvakirankumar.memora.domain.model.EventType
import com.meruvakirankumar.memora.domain.model.Memory
import com.meruvakirankumar.memora.domain.model.Reminder
import com.meruvakirankumar.memora.domain.model.ReminderStatus
import com.meruvakirankumar.memora.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class SaveMemoryUseCaseTest {

    private val clock = FixedClock(Instant.parse("2026-09-07T09:00:00Z"), ZoneId.of("UTC"))

    private class RecordingRepository : MemoryRepository {
        var memory: Memory? = null
        var reminder: Reminder? = null
        override fun observeAll(): Flow<List<Memory>> = flowOf(emptyList())
        override fun observe(id: String): Flow<Memory?> = flowOf(null)
        override suspend fun getById(id: String): Memory? = memory
        override suspend fun create(memory: Memory, reminder: Reminder?) {
            this.memory = memory
            this.reminder = reminder
        }
        override suspend fun upsert(memory: Memory) { this.memory = memory }
        override suspend fun delete(id: String) {}
    }

    @Test
    fun `creates memory with reminder offset by lead days`() = runTest {
        val repo = RecordingRepository()
        val useCase = SaveMemoryUseCase(repo, clock)

        val result = useCase("Milk", EventType.EXPIRY, LocalDate.of(2027, 8, 31), reminderLeadDays = 3)

        assertTrue(result is AppResult.Success)
        assertEquals(LocalDate.of(2027, 8, 28), repo.reminder?.reminderStartDate)
        assertEquals(LocalTime.of(9, 0), repo.reminder?.reminderTime)
        assertEquals(ReminderStatus.SCHEDULED, repo.reminder?.status)
        assertEquals(repo.memory?.id, repo.reminder?.memoryId)
    }

    @Test
    fun `defaults to one day before`() = runTest {
        val repo = RecordingRepository()
        val useCase = SaveMemoryUseCase(repo, clock)

        useCase("Milk", EventType.EXPIRY, LocalDate.of(2027, 8, 31))

        assertEquals(LocalDate.of(2027, 8, 30), repo.reminder?.reminderStartDate)
    }

    @Test
    fun `rejects a blank title`() = runTest {
        val repo = RecordingRepository()
        val useCase = SaveMemoryUseCase(repo, clock)

        val result = useCase("   ", EventType.EXPIRY, LocalDate.of(2027, 8, 31))

        assertTrue(result is AppResult.Failure)
        assertEquals(null, repo.memory)
    }
}
