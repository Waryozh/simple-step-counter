package com.waryozh.simplestepcounter

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SetStepLengthDialogTest : BaseTest() {
    @Before
    fun initStepLengthDialogTest() {
        setPrefs(1000, 0)
        repository.setStepLength(70)
        repository.setStepsTaken(1000)
        Espresso.onView(ViewMatchers.withId(R.id.tv_steps_taken)).check(ViewAssertions.matches(ViewMatchers.withText("1000")))
        Espresso.onView(ViewMatchers.withId(R.id.tv_distance_walked)).check(ViewAssertions.matches(ViewMatchers.withText("700")))

        Espresso.openActionBarOverflowOrOptionsMenu(applicationContext)
        Espresso.onView(ViewMatchers.withText(R.string.set_step_length)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.picker_step_length)).check(ViewAssertions.matches(withNumberPickerValue(70)))
    }

    @Test
    fun setStepLengthDialog_Cancel() {
        Espresso.onView(ViewMatchers.withText(R.string.cancel)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.tv_distance_walked)).check(ViewAssertions.matches(ViewMatchers.withText("700")))
        Assert.assertEquals(70, repository.getStepLength())
    }

    @Test
    fun setStepLengthDialog_ChangeAndCancel() {
        Espresso.onView(ViewMatchers.withId(R.id.picker_step_length)).perform(setValue(123))
        Espresso.onView(ViewMatchers.withText(R.string.cancel)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.tv_distance_walked)).check(ViewAssertions.matches(ViewMatchers.withText("700")))
        Assert.assertEquals(70, repository.getStepLength())
    }

    @Test
    fun setStepLengthDialog_ValidLength() {
        Espresso.onView(ViewMatchers.withId(R.id.picker_step_length)).perform(setValue(123))
        Espresso.onView(ViewMatchers.withText(R.string.ok)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.tv_distance_walked)).check(ViewAssertions.matches(ViewMatchers.withText("1230")))
        Assert.assertEquals(123, repository.getStepLength())
    }

    @Test
    fun setStepLengthDialog_SaveInLandscapeMode() {
        rotateScreen(rule.activity, true)
        Espresso.onView(ViewMatchers.withId(R.id.picker_step_length)).check(ViewAssertions.matches(withNumberPickerValue(70)))

        Espresso.onView(ViewMatchers.withId(R.id.picker_step_length)).perform(setValue(87))
        Espresso.onView(ViewMatchers.withText(R.string.ok)).perform(ViewActions.click())

        rotateScreen(rule.activity, false)
        Assert.assertEquals(87, repository.getStepLength())
    }

    @Test
    fun setStepLengthDialog_SaveAfterRotate() {
        rotateScreen(rule.activity, true)
        Espresso.onView(ViewMatchers.withId(R.id.picker_step_length)).check(ViewAssertions.matches(withNumberPickerValue(70)))

        Espresso.onView(ViewMatchers.withId(R.id.picker_step_length)).perform(setValue(87))
        rotateScreen(rule.activity, false)
        Espresso.onView(ViewMatchers.withId(R.id.picker_step_length)).check(ViewAssertions.matches(withNumberPickerValue(87)))
        Espresso.onView(ViewMatchers.withText(R.string.ok)).perform(ViewActions.click())
        Assert.assertEquals(87, repository.getStepLength())
    }
}
