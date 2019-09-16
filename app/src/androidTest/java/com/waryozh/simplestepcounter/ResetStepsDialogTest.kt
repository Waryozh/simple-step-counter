package com.waryozh.simplestepcounter

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResetStepsDialogTest : BaseTest() {
    @Before
    fun initResetStepsDialogTest() {
        setStepsCorrection(1000)
        repository.setStepLength(70)
        runBlocking {
            repository.setStepsTaken(2000)
        }

        Espresso.onView(ViewMatchers.withId(R.id.tv_steps_taken)).check(ViewAssertions.matches(ViewMatchers.withText("1000")))
        Espresso.onView(ViewMatchers.withId(R.id.tv_distance_walked)).check(ViewAssertions.matches(ViewMatchers.withText("700")))

        Espresso.openActionBarOverflowOrOptionsMenu(applicationContext)
        Espresso.onView(ViewMatchers.withText(R.string.menu_reset_steps)).perform(ViewActions.click())
    }

    @Test
    fun cancelDialog() {
        Espresso.onView(ViewMatchers.withText(R.string.cancel)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.tv_steps_taken)).check(ViewAssertions.matches(ViewMatchers.withText("1000")))
        Espresso.onView(ViewMatchers.withId(R.id.tv_distance_walked)).check(ViewAssertions.matches(ViewMatchers.withText("700")))
        Assert.assertEquals(70, repository.getStepLength())
    }

    @Test
    fun confirmDialog() {
        Espresso.onView(ViewMatchers.withText(R.string.reset)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.tv_steps_taken)).check(ViewAssertions.matches(ViewMatchers.withText("0")))
        Espresso.onView(ViewMatchers.withId(R.id.tv_distance_walked)).check(ViewAssertions.matches(ViewMatchers.withText("0")))
        Assert.assertEquals(70, repository.getStepLength())
    }
}
