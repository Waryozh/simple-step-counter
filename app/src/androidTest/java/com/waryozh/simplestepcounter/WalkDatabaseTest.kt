package com.waryozh.simplestepcounter

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.waryozh.simplestepcounter.database.WalkDatabase
import com.waryozh.simplestepcounter.database.WalkDatabaseDao
import com.waryozh.simplestepcounter.database.WalkDay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WalkDatabaseTest {
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

        val dbToday = walkDao.getToday()
        assertEquals(day, dbToday)
    }

    @Test
    fun insertDayReplacesOnConflict() = runBlocking {
        val day = WalkDay()
        day.dayId = walkDao.insert(day)

        val newDay = WalkDay(dayId = day.dayId, steps = 1000, distance = 700)
        walkDao.insert(newDay)

        val dbToday = walkDao.getToday()
        assertEquals(newDay, dbToday)
    }

    @Test
    fun updateDay() = runBlocking {
        val day = WalkDay()
        day.dayId = walkDao.insert(day)

        val newDay = WalkDay(dayId = day.dayId, steps = 1000, distance = 700)
        walkDao.update(newDay)

        val dbToday = walkDao.getToday()
        assertEquals(newDay, dbToday)
    }
}
