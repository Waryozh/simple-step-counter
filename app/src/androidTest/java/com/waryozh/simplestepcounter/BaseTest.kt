package com.waryozh.simplestepcounter

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.test.rule.ActivityTestRule
import com.waryozh.simplestepcounter.repositories.Repository
import org.junit.Before
import org.junit.Rule

abstract class BaseTest {
    companion object {
        const val STEPS_TAKEN = "STEPS_TAKEN"
        const val STEPS_TAKEN_CORRECTION = "STEPS_TAKEN_CORRECTION"
    }

    @Rule
    @JvmField
    val rule = ActivityTestRule(MainActivity::class.java)

    protected lateinit var applicationContext: Context
    protected lateinit var prefs: SharedPreferences

    protected val repository = Repository

    @Before
    fun initialize() {
        applicationContext = rule.activity.applicationContext
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
}
