package com.waryozh.simplestepcounter

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager.getDefaultSharedPreferencesName
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
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

    @Test
    fun cancelResetStepsDialog() {
        setPrefs(1000, 500)
        repository.setStepsTaken(1000)
        Espresso.onView(ViewMatchers.withId(R.id.tv_steps_taken)).check(ViewAssertions.matches(ViewMatchers.withText("500")))

        Espresso.openActionBarOverflowOrOptionsMenu(applicationContext)
        Espresso.onView(ViewMatchers.withText(R.string.reset_steps)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.cancel)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.tv_steps_taken)).check(ViewAssertions.matches(ViewMatchers.withText("500")))
    }

    @Test
    fun confirmResetStepsDialog() {
        setPrefs(1000, 500)
        repository.setStepsTaken(1000)
        Espresso.onView(ViewMatchers.withId(R.id.tv_steps_taken)).check(ViewAssertions.matches(ViewMatchers.withText("500")))

        Espresso.openActionBarOverflowOrOptionsMenu(applicationContext)
        Espresso.onView(ViewMatchers.withText(R.string.reset_steps)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.reset)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.tv_steps_taken)).check(ViewAssertions.matches(ViewMatchers.withText("0")))
    }

    private fun setPrefs(steps: Int, correction: Int) {
        with(prefs.edit()) {
            putInt(STEPS_TAKEN, steps)
            putInt(STEPS_TAKEN_CORRECTION, correction)
            apply()
        }
    }
}
