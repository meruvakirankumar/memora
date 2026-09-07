package com.meruvakirankumar.memora.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.meruvakirankumar.memora.data.local.entity.MemoryEntity
import com.meruvakirankumar.memora.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MemoraDatabaseTest {

    private lateinit var database: MemoraDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, MemoraDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndReadMemory() = runTest {
        val memory = memoryEntity("m1")
        database.memoryDao().upsert(memory)

        val loaded = database.memoryDao().getById("m1")

        assertEquals(memory, loaded)
    }

    @Test
    fun deletingMemoryCascadesToReminder() = runTest {
        database.memoryDao().upsert(memoryEntity("m1"))
        database.reminderDao().upsert(reminderEntity("r1", "m1"))

        database.memoryDao().deleteById("m1")

        assertNull(database.reminderDao().getById("r1"))
        assertEquals(0, database.reminderDao().observeAll().first().size)
    }

    private fun memoryEntity(id: String) = MemoryEntity(
        id = id,
        title = "Milk",
        eventType = "EXPIRY",
        eventDate = "2027-08-31",
        timeZone = "UTC",
        status = "UPCOMING",
        createdAt = 1_000L,
        updatedAt = 1_000L,
    )

    private fun reminderEntity(id: String, memoryId: String) = ReminderEntity(
        id = id,
        memoryId = memoryId,
        reminderStartDate = "2027-08-30",
        reminderTime = "09:00",
        status = "SCHEDULED",
        completedAt = null,
    )
}
