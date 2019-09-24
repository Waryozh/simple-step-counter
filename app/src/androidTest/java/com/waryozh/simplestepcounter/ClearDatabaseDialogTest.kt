package com.waryozh.simplestepcounter

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.waryozh.simplestepcounter.database.WalkDay
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClearDatabaseDialogTest : StatsActivityBaseTest() {
    private val days = listOf(
        WalkDay(dayId = 5, steps = 1000, distance = 400, date = "18.09.2019"),
        WalkDay(dayId = 4, steps = 321, distance = 12, date = "16.09.2019"),
        WalkDay(dayId = 1, steps = 1234, distance = 500, date = "12.09.2019")
    )

    @Before
    fun initClearDatabaseDialogTest() {
        runBlocking {
            days.forEach {
                walkDao.insert(it)
            }
        }

        repository.getAllDays()

        Espresso.openActionBarOverflowOrOptionsMenu(applicationContext)
        onView(withText(R.string.menu_clear_database)).perform(click())
    }

    @Test
    fun cancelDialog() {
        onView(withText(R.string.cancel)).perform(click())

        // Check that No Data label is invisible
        onView(withId(R.id.tv_no_data)).check(matches(not(isDisplayed())))

        assertEquals(activityTestRule.activity.findViewById<RecyclerView>(R.id.rv_walkday_list).adapter?.itemCount, 3)

        // Check that StatsActivity still displays all the days inserted in initClearDatabaseDialogTest
        days.forEachIndexed { index, day ->
            onView(withId(R.id.rv_walkday_list)).check(matches(atPosition(index, allOf(
                hasDescendant(
                    allOf(
                        withId(R.id.tv_stats_date),
                        withText(day.date)
                    )
                ),
                hasDescendant(
                    allOf(
                        withId(R.id.tv_stats_item_steps),
                        withText(day.steps.toString())
                    )
                ),
                hasDescendant(
                    allOf(
                        withId(R.id.tv_stats_item_distance),
                        withText(day.distance.toString())
                    )
                )
            ))))
        }
    }

    @Test
    fun confirmDialog() {
        onView(withText(R.string.clear)).perform(click())

        // Check that No Data label is visible
        onView(withId(R.id.tv_no_data)).check(matches(isDisplayed()))

        // Check that StatsActivity displays no days
        assertEquals(activityTestRule.activity.findViewById<RecyclerView>(R.id.rv_walkday_list).adapter?.itemCount, 0)
    }
}
