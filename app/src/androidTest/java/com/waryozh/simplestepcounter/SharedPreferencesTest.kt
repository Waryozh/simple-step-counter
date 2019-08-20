package com.waryozh.simplestepcounter

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager.getDefaultSharedPreferencesName
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.waryozh.simplestepcounter.repositories.Repository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val STEPS_TAKEN = "STEPS_TAKEN"
private const val STEPS_TAKEN_CORRECTION = "STEPS_TAKEN_CORRECTION"

@RunWith(AndroidJUnit4::class)
class SharedPreferencesTest {
    @Rule
    @JvmField
    val rule = ActivityTestRule(MainActivity::class.java)

    private lateinit var applicationContext: Context
    private lateinit var prefs: SharedPreferences

    private val repository = Repository

    @Before
    fun initialize() {
        applicationContext = rule.activity.applicationContext
        prefs = applicationContext.getSharedPreferences(
            getDefaultSharedPreferencesName(applicationContext),
            Context.MODE_PRIVATE
        )
    }

    @Test
    fun newStepSession() {
        setPrefs(0, 0)
        repository.setStepsTaken(1000)
        assertEquals(1000, prefs.getLong(STEPS_TAKEN_CORRECTION, -1))
        assertEquals(0, prefs.getLong(STEPS_TAKEN, -1))
    }

    @Test
    fun updateStepsTaken() {
        setPrefs(1000, 500)
        repository.setStepsTaken(2000)
        assertEquals(500, prefs.getLong(STEPS_TAKEN_CORRECTION, -1))
        assertEquals(1500, prefs.getLong(STEPS_TAKEN, -1))
    }

    private fun setPrefs(steps: Long, correction: Long) {
        with(prefs.edit()) {
            putLong(STEPS_TAKEN, steps)
            putLong(STEPS_TAKEN_CORRECTION, correction)
            apply()
        }
    }
}
