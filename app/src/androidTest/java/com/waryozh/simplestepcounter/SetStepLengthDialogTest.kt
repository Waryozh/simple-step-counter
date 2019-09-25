package com.waryozh.simplestepcounter

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SetStepLengthDialogTest : MainActivityBaseTest() {
    companion object {
        private const val DEFAULT_STEPS = 2000
        private const val DEFAULT_STEP_LENGTH = 70
    }

    @Before
    fun initStepLengthDialogTest() {
        setStepsCorrection(1000)
        runBlocking {
            repository.setStepLength(DEFAULT_STEP_LENGTH)
            repository.setStepsTaken(DEFAULT_STEPS)
        }
        onView(withId(R.id.tv_steps_taken)).check(matches(withText("1000")))
        onView(withId(R.id.tv_distance_walked)).check(matches(withText("700")))

        Espresso.openActionBarOverflowOrOptionsMenu(applicationContext)
        onView(withText(R.string.menu_set_step_length)).perform(click())
        onView(withId(R.id.picker_step_length)).check(matches(withNumberPickerValue(DEFAULT_STEP_LENGTH)))
    }

    @Test
    fun cancelDialog() {
        onView(withText(R.string.cancel)).perform(click())
        onView(withId(R.id.tv_distance_walked)).check(matches(withText("700")))
        assertEquals(DEFAULT_STEP_LENGTH, repository.getStepLength())
    }

    @Test
    fun changeAndCancel() {
        onView(withId(R.id.picker_step_length)).perform(setValue(123))
        onView(withText(R.string.cancel)).perform(click())
        onView(withId(R.id.tv_distance_walked)).check(matches(withText("700")))
        assertEquals(DEFAULT_STEP_LENGTH, repository.getStepLength())
    }

    @Test
    fun setValidValue() {
        onView(withId(R.id.picker_step_length)).perform(setValue(123))
        onView(withText(R.string.ok)).perform(click())
        onView(withId(R.id.tv_distance_walked)).check(matches(withText("1230")))
        assertEquals(123, repository.getStepLength())
    }

    @Test
    fun saveInLandscapeMode() {
        rotateScreen(activityTestRule.activity, true)
        onView(withId(R.id.picker_step_length)).check(matches(withNumberPickerValue(DEFAULT_STEP_LENGTH)))

        onView(withId(R.id.picker_step_length)).perform(setValue(87))
        onView(withText(R.string.ok)).perform(click())

        rotateScreen(activityTestRule.activity, false)
        assertEquals(87, repository.getStepLength())
    }

    @Test
    fun saveAfterRotate() {
        rotateScreen(activityTestRule.activity, true)
        onView(withId(R.id.picker_step_length)).check(matches(withNumberPickerValue(DEFAULT_STEP_LENGTH)))

        onView(withId(R.id.picker_step_length)).perform(setValue(87))
        rotateScreen(activityTestRule.activity, false)
        onView(withId(R.id.picker_step_length)).check(matches(withNumberPickerValue(87)))
        onView(withText(R.string.ok)).perform(click())
        assertEquals(87, repository.getStepLength())
    }
}
