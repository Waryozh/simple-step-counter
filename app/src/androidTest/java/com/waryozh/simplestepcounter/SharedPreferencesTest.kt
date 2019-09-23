package com.waryozh.simplestepcounter

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.waryozh.simplestepcounter.repositories.Repository.Companion.STEPS_TAKEN_CORRECTION
import com.waryozh.simplestepcounter.testing.LiveDataTestUtil.getValue
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharedPreferencesTest : BaseTest() {
    @Test
    fun newStepSession() = runBlocking {
        setPrefs(0, 0)
        repository.setStepsTaken(1000)
        assertEquals(1000, prefs.getInt(STEPS_TAKEN_CORRECTION, -1))
        assertEquals(0, getValue(walkDao.getToday()).steps)
    }

    @Test
    fun updateStepsTaken() = runBlocking {
        setPrefs(1000, 500)
        repository.setStepsTaken(2000)
        assertEquals(500, prefs.getInt(STEPS_TAKEN_CORRECTION, -1))
        assertEquals(1500, getValue(walkDao.getToday()).steps)
    }

    @Test
    fun resetStepsAndStartNewSession() = runBlocking {
        setPrefs(1000, 500)
        repository.resetStepCounter()
        assertEquals(0, prefs.getInt(STEPS_TAKEN_CORRECTION, -1))
        assertEquals(0, getValue(walkDao.getToday()).steps)

        repository.setStepsTaken(2000)
        assertEquals(2000, prefs.getInt(STEPS_TAKEN_CORRECTION, -1))
        assertEquals(0, getValue(walkDao.getToday()).steps)
    }
}
