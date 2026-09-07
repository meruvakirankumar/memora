package com.meruvakirankumar.memora.domain.usecase

import com.meruvakirankumar.memora.core.error.AppResult
import com.meruvakirankumar.memora.core.time.FixedClock
import com.meruvakirankumar.memora.domain.model.EventType
import com.meruvakirankumar.memora.domain.model.Memory
import com.meruvakirankumar.memora.domain.model.MemoryStatus
import com.meruvakirankumar.memora.domain.model.Reminder
import com.meruvakirankumar.memora.domain.model.ReminderStatus
import com.meruvakirankumar.memora.domain.repository.MemoryRepository
import com.meruvakirankumar.memora.domain.repository.ReminderRepository
import com.meruvakirankumar.memora.domain.scheduling.ReminderScheduler
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

class EditMemoryUseCaseTest {

    private val clock = FixedClock(Instant.parse("2026-09-07T09:00:00Z"), ZoneId.of("UTC"))

    private class FakeMemoryRepo(existing: Memory) : MemoryRepository {
        var memory: Memory? = existing
        var reminder: Reminder? = null
        override fun observeAll(): Flow<List<Memory>> = flowOf(emptyList())
        override fun observe(id: String): Flow<Memory?> = flowOf(memory)
        override suspend fun getById(id: String): Memory? = memory
        override suspend fun getAll(): List<Memory> = listOfNotNull(memory)
        override suspend fun create(memory: Memory, reminder: Reminder?) {
            this.memory = memory
            this.reminder = reminder
        }
        override suspend fun upsert(memory: Memory) { this.memory = memory }
        override suspend fun delete(id: String) {}
    }

    private class FakeReminderRepo(private val existing: Reminder?) : ReminderRepository {
        override fun observeAll(): Flow<List<Reminder>> = flowOf(emptyList())
        override fun observeByMemory(memoryId: String): Flow<List<Reminder>> = flowOf(emptyList())
        override suspend fun getByMemory(memoryId: String): Reminder? = existing
        override suspend fun getById(id: String): Reminder? = existing
        override suspend fun upsert(reminder: Reminder) {}
        override suspend fun delete(id: String) {}
    }

    private class NoopScheduler : ReminderScheduler {
        override fun schedule(memoryId: String, at: Instant) {}
        override fun cancel(memoryId: String) {}
    }

    @Test
    fun `edit updates memory fields and recomputes the reminder start`() = runTest {
        val existing = Memory(
            id = "m1",
            title = "Milk",
            eventType = EventType.EXPIRY,
            eventDate = LocalDate.of(2027, 8, 31),
            timeZone = ZoneId.of("UTC"),
            status = MemoryStatus.UPCOMING,
            createdAt = clock.now(),
            updatedAt = clock.now(),
        )
        val existingReminder = Reminder(
            id = "r1",
            memoryId = "m1",
            reminderStartDate = LocalDate.of(2027, 8, 30),
            reminderTime = LocalTime.of(9, 0),
            status = ReminderStatus.SCHEDULED,
            completedAt = null,
        )
        val repo = FakeMemoryRepo(existing)
        val reminderRepo = FakeReminderRepo(existingReminder)
        val useCase = EditMemoryUseCase(repo, reminderRepo, clock, ScheduleReminderUseCase(repo, reminderRepo, NoopScheduler(), clock))

        val result = useCase("m1", "Fresh Milk", EventType.BILL_DUE, LocalDate.of(2027, 9, 15), reminderLeadDays = 5)

        assertTrue(result is AppResult.Success)
        assertEquals("Fresh Milk", repo.memory?.title)
        assertEquals(EventType.BILL_DUE, repo.memory?.eventType)
        assertEquals(LocalDate.of(2027, 9, 15), repo.memory?.eventDate)
        assertEquals(LocalDate.of(2027, 9, 10), repo.reminder?.reminderStartDate)
        assertEquals("r1", repo.reminder?.id)
    }
}
