package com.waryozh.simplestepcounter

import androidx.test.rule.ActivityTestRule
import com.waryozh.simplestepcounter.injection.RepositoryModule
import com.waryozh.simplestepcounter.repositories.StatsRepository
import com.waryozh.simplestepcounter.ui.StatsActivity
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject

abstract class StatsActivityBaseTest : BaseTest() {
    @Rule
    @JvmField
    val activityTestRule = ActivityTestRule(StatsActivity::class.java, false, false)

    @Inject
    lateinit var repository: StatsRepository

    @Before
    fun setUpStatsActivity() {
        val testAppComponent = testAppComponentBuilder
            .repositoryModule(RepositoryModule())
            .build()

        app.appComponent = testAppComponent
        testAppComponent.inject(this)

        activityTestRule.launchActivity(null)
        applicationContext = activityTestRule.activity.applicationContext
    }
}
