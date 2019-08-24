package com.waryozh.simplestepcounter

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharedPreferencesTest : BaseTest() {
    @Test
    fun newStepSession() {
        setPrefs(0, 0)
        repository.setStepsTaken(1000)
        assertEquals(1000, prefs.getInt(STEPS_TAKEN_CORRECTION, -1))
        assertEquals(0, prefs.getInt(STEPS_TAKEN, -1))
    }

    @Test
    fun updateStepsTaken() {
        setPrefs(1000, 500)
        repository.setStepsTaken(2000)
        assertEquals(500, prefs.getInt(STEPS_TAKEN_CORRECTION, -1))
        assertEquals(1500, prefs.getInt(STEPS_TAKEN, -1))
    }

    @Test
    fun resetSteps() {
        setPrefs(1000, 500)
        repository.resetStepCounter()
        assertEquals(500, prefs.getInt(STEPS_TAKEN_CORRECTION, -1))
        assertEquals(0, prefs.getInt(STEPS_TAKEN, -1))
    }
}
