package com.waryozh.simplestepcounter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.waryozh.simplestepcounter.database.WalkDatabase
import com.waryozh.simplestepcounter.database.WalkDatabaseDao
import com.waryozh.simplestepcounter.database.WalkDay
import com.waryozh.simplestepcounter.testing.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WalkDatabaseTest {
    // Swaps the background executor used by the Architecture Components with a different one
    // which executes each task synchronously.
    // Needed to work with LiveData from the tests.
    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: WalkDatabase
    private lateinit var walkDao: WalkDatabaseDao

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(getApplicationContext(), WalkDatabase::class.java)
            // Allow main thread queries, just for testing
            .allowMainThreadQueries()
            .build()
        walkDao = db.walkDatabaseDao
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetDay() = runBlocking {
        val day = WalkDay()
        day.dayId = walkDao.insert(day)

        val dbToday = walkDao.getToday().getOrAwaitValue()
        assertEquals(day, dbToday)
    }

    @Test
    fun insertDayReplacesOnConflict() = runBlocking {
        val day = WalkDay()
        day.dayId = walkDao.insert(day)

        val newDay = WalkDay(dayId = day.dayId, steps = 1000, distance = 700)
        walkDao.insert(newDay)

        val dbToday = walkDao.getToday().getOrAwaitValue()
        assertEquals(newDay, dbToday)
    }

    @Test
    fun updateDay() = runBlocking {
        val day = WalkDay()
        day.dayId = walkDao.insert(day)

        val newDay = WalkDay(dayId = day.dayId, steps = 1000, distance = 700)
        walkDao.update(newDay)

        val dbToday = walkDao.getToday().getOrAwaitValue()
        assertEquals(newDay, dbToday)
    }

    @Test
    fun getAllDaysThenDeleteAllDays() = runBlocking {
        val days = listOf(
            WalkDay(dayId = 5, steps = 1000, distance = 400, date = "18.09.2019"),
            WalkDay(dayId = 4, steps = 321, distance = 12, date = "16.09.2019"),
            WalkDay(dayId = 1, steps = 1234, distance = 500, date = "12.09.2019")
        )

        days.forEach {
            walkDao.insert(it)
        }

        var dbDays = walkDao.getAllDays().getOrAwaitValue()
        assertEquals(days, dbDays)

        walkDao.deleteAllDays()
        dbDays = walkDao.getAllDays().getOrAwaitValue()
        assertEquals(emptyList<WalkDay>(), dbDays)
    }
}
