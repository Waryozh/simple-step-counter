package com.waryozh.simplestepcounter

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.preference.PreferenceManager.getDefaultSharedPreferencesName
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.waryozh.simplestepcounter.repositories.Repository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val STEPS_TAKEN = "STEPS_TAKEN"
private const val STEPS_TAKEN_CORRECTION = "STEPS_TAKEN_CORRECTION"

// TODO: refactor into several separate test classes
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

    private fun initResetStepsDialogTest() {
        setPrefs(1000, 0)
        repository.setStepLength(70)
        repository.setStepsTaken(1000)
        Espresso.onView(ViewMatchers.withId(R.id.tv_steps_taken)).check(ViewAssertions.matches(ViewMatchers.withText("1000")))
        Espresso.onView(ViewMatchers.withId(R.id.tv_distance_walked)).check(ViewAssertions.matches(ViewMatchers.withText("700")))
    }

    @Test
    fun resetStepsDialog_Cancel() {
        initResetStepsDialogTest()

        Espresso.openActionBarOverflowOrOptionsMenu(applicationContext)
        Espresso.onView(ViewMatchers.withText(R.string.reset_steps)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.cancel)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.tv_steps_taken)).check(ViewAssertions.matches(ViewMatchers.withText("1000")))
        Espresso.onView(ViewMatchers.withId(R.id.tv_distance_walked)).check(ViewAssertions.matches(ViewMatchers.withText("700")))
        assertEquals(70, repository.getStepLength())
    }

    @Test
    fun resetStepsDialog_Confirm() {
        initResetStepsDialogTest()

        Espresso.openActionBarOverflowOrOptionsMenu(applicationContext)
        Espresso.onView(ViewMatchers.withText(R.string.reset_steps)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.reset)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.tv_steps_taken)).check(ViewAssertions.matches(ViewMatchers.withText("0")))
        Espresso.onView(ViewMatchers.withId(R.id.tv_distance_walked)).check(ViewAssertions.matches(ViewMatchers.withText("0")))
        assertEquals(70, repository.getStepLength())
    }

    @Test
    fun setStepLengthDialog_Cancel() {
        initStepLengthDialogTest()

        Espresso.onView(ViewMatchers.withText(R.string.cancel)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.tv_distance_walked)).check(ViewAssertions.matches(ViewMatchers.withText("700")))
        assertEquals(70, repository.getStepLength())
    }

    @Test
    fun setStepLengthDialog_ChangeAndCancel() {
        initStepLengthDialogTest()

        Espresso.onView(ViewMatchers.withId(R.id.et_step_length)).perform(ViewActions.replaceText("123"))
        Espresso.onView(ViewMatchers.withText(R.string.cancel)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.tv_distance_walked)).check(ViewAssertions.matches(ViewMatchers.withText("700")))
        assertEquals(70, repository.getStepLength())
    }

    @Test
    fun setStepLengthDialog_InvalidLength() {
        initStepLengthDialogTest()

        listOf("0", "-1", "1.5", "201", "1000", Int.MAX_VALUE.toString()).forEach {
            Espresso.onView(ViewMatchers.withId(R.id.et_step_length)).perform(ViewActions.replaceText(it))
            Espresso.onView(ViewMatchers.withText(R.string.ok)).perform(ViewActions.click())
            assertEquals(70, repository.getStepLength())
        }
    }

    @Test
    fun setStepLengthDialog_ValidLength() {
        initStepLengthDialogTest()

        Espresso.onView(ViewMatchers.withId(R.id.et_step_length)).perform(ViewActions.replaceText("123"))
        Espresso.onView(ViewMatchers.withText(R.string.ok)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.tv_distance_walked)).check(ViewAssertions.matches(ViewMatchers.withText("1230")))
        assertEquals(123, repository.getStepLength())
    }

    @Test
    fun setStepLengthDialog_SaveInLandscapeMode() {
        initStepLengthDialogTest()

        rotateScreen(rule.activity, true)
        Espresso.onView(ViewMatchers.withId(R.id.et_step_length)).check(ViewAssertions.matches(ViewMatchers.withText("70")))

        Espresso.onView(ViewMatchers.withId(R.id.et_step_length)).perform(ViewActions.replaceText("87"))
        Espresso.onView(ViewMatchers.withText(R.string.ok)).perform(ViewActions.click())

        rotateScreen(rule.activity, false)
        assertEquals(87, repository.getStepLength())
    }

    @Test
    fun setStepLengthDialog_SaveAfterRotate() {
        initStepLengthDialogTest()

        rotateScreen(rule.activity, true)
        Espresso.onView(ViewMatchers.withId(R.id.et_step_length)).check(ViewAssertions.matches(ViewMatchers.withText("70")))

        Espresso.onView(ViewMatchers.withId(R.id.et_step_length)).perform(ViewActions.replaceText("87"))
        rotateScreen(rule.activity, false)
        Espresso.onView(ViewMatchers.withId(R.id.et_step_length)).check(ViewAssertions.matches(ViewMatchers.withText("87")))
        Espresso.onView(ViewMatchers.withText(R.string.ok)).perform(ViewActions.click())
        assertEquals(87, repository.getStepLength())
    }

    private fun setPrefs(steps: Int, correction: Int) {
        with(prefs.edit()) {
            putInt(STEPS_TAKEN, steps)
            putInt(STEPS_TAKEN_CORRECTION, correction)
            apply()
        }
    }

    private fun initStepLengthDialogTest() {
        setPrefs(1000, 0)
        repository.setStepLength(70)
        repository.setStepsTaken(1000)
        Espresso.onView(ViewMatchers.withId(R.id.tv_steps_taken)).check(ViewAssertions.matches(ViewMatchers.withText("1000")))
        Espresso.onView(ViewMatchers.withId(R.id.tv_distance_walked)).check(ViewAssertions.matches(ViewMatchers.withText("700")))

        Espresso.openActionBarOverflowOrOptionsMenu(applicationContext)
        Espresso.onView(ViewMatchers.withText(R.string.set_step_length)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.et_step_length)).check(ViewAssertions.matches(ViewMatchers.withText("70")))
    }

    private fun rotateScreen(activity: Activity, isLandscape: Boolean) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            activity.requestedOrientation = if (isLandscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(2000)
    }
}
