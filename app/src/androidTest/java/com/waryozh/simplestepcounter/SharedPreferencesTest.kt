package com.waryozh.simplestepcounter

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.waryozh.simplestepcounter.repositories.Repository.Companion.STEPS_ON_STOP
import com.waryozh.simplestepcounter.repositories.Repository.Companion.STEPS_TAKEN_CORRECTION
import com.waryozh.simplestepcounter.testing.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharedPreferencesTest : MainActivityBaseTest() {
    @Test
    fun newStepSession() = runBlocking {
        setPrefs(0, 0)
        repository.setStepsTaken(1000)
        assertEquals(1000, prefs.getInt(STEPS_TAKEN_CORRECTION, -1))
        assertEquals(0, walkDao.getToday().getOrAwaitValue().steps)
    }

    @Test
    fun updateStepsTaken() = runBlocking {
        setPrefs(1000, 500)
        repository.setStepsTaken(2000)
        assertEquals(500, prefs.getInt(STEPS_TAKEN_CORRECTION, -1))
        assertEquals(1500, walkDao.getToday().getOrAwaitValue().steps)
        assertEquals(0, prefs.getInt(STEPS_ON_STOP, -1))

        // When the service is stopped, the number of steps taken should be recorded into a separate preference
        repository.setServiceRunning(false)
        assertEquals(1500, prefs.getInt(STEPS_ON_STOP, -1))

        // If Step Counter sensor detected steps while our service was stopped,
        // steps correction should be updated accordingly so the actual number of steps taken remains the same
        repository.setStepsTaken(2500)
        assertEquals(1000, prefs.getInt(STEPS_TAKEN_CORRECTION, -1))
        assertEquals(1500, walkDao.getToday().getOrAwaitValue().steps)
        assertEquals(0, prefs.getInt(STEPS_ON_STOP, -1))
    }

    @Test
    fun resetStepsAndStartNewSession() = runBlocking {
        setPrefs(1000, 500)
        repository.resetStepCounter()
        assertEquals(0, prefs.getInt(STEPS_TAKEN_CORRECTION, -1))
        assertEquals(0, walkDao.getToday().getOrAwaitValue().steps)

        repository.setStepsTaken(2000)
        assertEquals(2000, prefs.getInt(STEPS_TAKEN_CORRECTION, -1))
        assertEquals(0, walkDao.getToday().getOrAwaitValue().steps)
    }

//    @Test
//    fun stepsTakenWhenServiceStopped() = runBlocking {
//        setPrefs(1000, 500)
//        repository.setStepsTaken(2000)
//        assertEquals(500, prefs.getInt(STEPS_TAKEN_CORRECTION, -1))
//
//        repository.setServiceRunning(false)
//        assertEquals(1500, prefs.getInt(STEPS_ON_STOP, -1))
//    }
}
