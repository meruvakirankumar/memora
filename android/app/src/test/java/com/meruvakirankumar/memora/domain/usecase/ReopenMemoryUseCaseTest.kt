package com.meruvakirankumar.memora.domain.usecase

import com.meruvakirankumar.memora.core.error.AppResult
import com.meruvakirankumar.memora.core.time.FixedClock
import com.meruvakirankumar.memora.domain.model.EventType
import com.meruvakirankumar.memora.domain.model.Memory
import com.meruvakirankumar.memora.domain.model.MemoryStatus
import com.meruvakirankumar.memora.domain.model.Reminder
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
import java.time.ZoneId

class ReopenMemoryUseCaseTest {

    private val clock = FixedClock(Instant.parse("2026-09-07T09:00:00Z"), ZoneId.of("UTC"))

    private class FakeMemoryRepo(existing: Memory) : MemoryRepository {
        var memory: Memory? = existing
        override fun observeAll(): Flow<List<Memory>> = flowOf(emptyList())
        override fun observe(id: String): Flow<Memory?> = flowOf(memory)
        override suspend fun getById(id: String): Memory? = memory
        override suspend fun getAll(): List<Memory> = listOfNotNull(memory)
        override suspend fun create(memory: Memory, reminder: Reminder?) { this.memory = memory }
        override suspend fun upsert(memory: Memory) { this.memory = memory }
        override suspend fun delete(id: String) {}
    }

    private class NoopReminderRepo : ReminderRepository {
        override fun observeAll(): Flow<List<Reminder>> = flowOf(emptyList())
        override fun observeByMemory(memoryId: String): Flow<List<Reminder>> = flowOf(emptyList())
        override suspend fun getByMemory(memoryId: String): Reminder? = null
        override suspend fun getById(id: String): Reminder? = null
        override suspend fun upsert(reminder: Reminder) {}
        override suspend fun delete(id: String) {}
    }

    private class NoopScheduler : ReminderScheduler {
        override fun schedule(memoryId: String, at: Instant) {}
        override fun cancel(memoryId: String) {}
    }

    @Test
    fun `reopen moves a completed memory back to active`() = runTest {
        val completed = Memory(
            id = "m1",
            title = "Milk",
            eventType = EventType.EXPIRY,
            eventDate = LocalDate.of(2027, 8, 31),
            timeZone = ZoneId.of("UTC"),
            status = MemoryStatus.COMPLETED,
            createdAt = clock.now(),
            updatedAt = clock.now(),
        )
        val repo = FakeMemoryRepo(completed)
        val useCase = ReopenMemoryUseCase(
            repo,
            clock,
            ScheduleReminderUseCase(repo, NoopReminderRepo(), NoopScheduler(), clock),
        )

        val result = useCase("m1")

        assertTrue(result is AppResult.Success)
        assertEquals(MemoryStatus.UPCOMING, repo.memory?.status)
    }
}
