package com.waryozh.simplestepcounter

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.rule.ActivityTestRule
import com.waryozh.simplestepcounter.repositories.Repository
import com.waryozh.simplestepcounter.ui.MainActivity
import org.junit.Before
import org.junit.Rule

abstract class BaseTest {
    companion object {
        const val STEPS_TAKEN = "STEPS_TAKEN"
        const val STEPS_TAKEN_CORRECTION = "STEPS_TAKEN_CORRECTION"
    }

    @Rule
    @JvmField
    val mainActivityTestRule = ActivityTestRule(MainActivity::class.java)

    // Swaps the background executor used by the Architecture Components with a different one
    // which executes each task synchronously.
    // Needed to invoke LiveData.setValue from the tests.
    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    protected lateinit var applicationContext: Context
    protected lateinit var prefs: SharedPreferences

    protected val repository = Repository

    @Before
    fun initialize() {
        applicationContext = mainActivityTestRule.activity.applicationContext
        prefs = applicationContext.getSharedPreferences(
            PreferenceManager.getDefaultSharedPreferencesName(applicationContext),
            Context.MODE_PRIVATE
        )
    }

    protected fun setPrefs(steps: Int, correction: Int) {
        with(prefs.edit()) {
            putInt(STEPS_TAKEN, steps)
            putInt(STEPS_TAKEN_CORRECTION, correction)
            apply()
        }
    }

    protected fun setStepsCorrection(correction: Int) {
        with(prefs.edit()) {
            putInt(STEPS_TAKEN_CORRECTION, correction)
            apply()
        }
    }
}
