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
class ResetStepsDialogTest : MainActivityBaseTest() {
    @Before
    fun initResetStepsDialogTest() {
        setStepsCorrection(1000)
        runBlocking {
            repository.setStepLength(70)
            repository.setStepsTaken(2000)
        }

        onView(withId(R.id.tv_steps_taken)).check(matches(withText("1000")))
        onView(withId(R.id.tv_distance_walked)).check(matches(withText("700")))

        Espresso.openActionBarOverflowOrOptionsMenu(applicationContext)
        onView(withText(R.string.menu_reset_steps)).perform(click())
    }

    @Test
    fun cancelDialog() {
        onView(withText(R.string.cancel)).perform(click())
        onView(withId(R.id.tv_steps_taken)).check(matches(withText("1000")))
        onView(withId(R.id.tv_distance_walked)).check(matches(withText("700")))
        assertEquals(70, repository.getStepLength())
    }

    @Test
    fun confirmDialog() {
        onView(withText(R.string.reset)).perform(click())
        onView(withId(R.id.tv_steps_taken)).check(matches(withText("0")))
        onView(withId(R.id.tv_distance_walked)).check(matches(withText("0")))
        assertEquals(70, repository.getStepLength())
    }
}
