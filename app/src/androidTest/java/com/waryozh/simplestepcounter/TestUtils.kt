package com.waryozh.simplestepcounter

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import android.widget.NumberPicker
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun rotateScreen(activity: Activity, isLandscape: Boolean) {
    InstrumentationRegistry.getInstrumentation().runOnMainSync {
        activity.requestedOrientation = if (isLandscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    Thread.sleep(2000)
}

fun withNumberPickerValue(value: Int): Matcher<View> = object : TypeSafeMatcher<View>() {
    override fun describeTo(description: Description) {
        description.appendText("Value should be \"$value\"")
    }

    override fun matchesSafely(item: View): Boolean {
        return (item as NumberPicker).value == value
    }
}

fun setValue(value: Int): ViewAction = object : ViewAction {
    override fun getDescription(): String = "Set the NumberPicker value"

    override fun getConstraints(): Matcher<View> = ViewMatchers.isAssignableFrom(NumberPicker::class.java)

    override fun perform(uiController: UiController, view: View) {
        (view as NumberPicker).value = value
    }
}
