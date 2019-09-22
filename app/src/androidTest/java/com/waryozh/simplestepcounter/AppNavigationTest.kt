package com.waryozh.simplestepcounter

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.waryozh.simplestepcounter.ui.MainActivity
import com.waryozh.simplestepcounter.ui.StatsActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationTest {
    @Rule
    @JvmField
    val mainActivityIntentsTestRule = IntentsTestRule(MainActivity::class.java)

    @Test
    fun startStatsActivityAndPressBack() {
        // Since we're starting with clean preferences, the app will show SetStepLengthDialog,
        // so click OK to close the dialog.
        Espresso.onView(ViewMatchers.withText(R.string.ok)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.action_show_stats)).perform(ViewActions.click())
        // Check that StatsActivity was started via an Intent
        intended(hasComponent(StatsActivity::class.java.canonicalName))

        Espresso.pressBack()
        // It is recommended to check which activity/fragment is visible by checking the views on screen
        Espresso.onView(ViewMatchers.withId(R.id.tv_steps_taken)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}
