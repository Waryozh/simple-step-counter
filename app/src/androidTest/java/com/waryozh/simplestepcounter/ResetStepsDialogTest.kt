package com.waryozh.simplestepcounter

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResetStepsDialogTest : BaseTest() {
    @Test
    fun resetStepsDialog_Cancel() {
        initResetStepsDialogTest()

        Espresso.openActionBarOverflowOrOptionsMenu(applicationContext)
        Espresso.onView(ViewMatchers.withText(R.string.reset_steps)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.cancel)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.tv_steps_taken)).check(ViewAssertions.matches(ViewMatchers.withText("1000")))
        Espresso.onView(ViewMatchers.withId(R.id.tv_distance_walked)).check(ViewAssertions.matches(ViewMatchers.withText("700")))
        Assert.assertEquals(70, repository.getStepLength())
    }

    @Test
    fun resetStepsDialog_Confirm() {
        initResetStepsDialogTest()

        Espresso.openActionBarOverflowOrOptionsMenu(applicationContext)
        Espresso.onView(ViewMatchers.withText(R.string.reset_steps)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.reset)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.tv_steps_taken)).check(ViewAssertions.matches(ViewMatchers.withText("0")))
        Espresso.onView(ViewMatchers.withId(R.id.tv_distance_walked)).check(ViewAssertions.matches(ViewMatchers.withText("0")))
        Assert.assertEquals(70, repository.getStepLength())
    }

    private fun initResetStepsDialogTest() {
        setPrefs(1000, 0)
        repository.setStepLength(70)
        repository.setStepsTaken(1000)
        Espresso.onView(ViewMatchers.withId(R.id.tv_steps_taken)).check(ViewAssertions.matches(ViewMatchers.withText("1000")))
        Espresso.onView(ViewMatchers.withId(R.id.tv_distance_walked)).check(ViewAssertions.matches(ViewMatchers.withText("700")))
    }
}
