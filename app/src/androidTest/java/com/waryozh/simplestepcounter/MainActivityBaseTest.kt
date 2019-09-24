package com.waryozh.simplestepcounter

import androidx.test.rule.ActivityTestRule
import com.waryozh.simplestepcounter.injection.RepositoryModule
import com.waryozh.simplestepcounter.repositories.Repository
import com.waryozh.simplestepcounter.ui.MainActivity
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject

abstract class MainActivityBaseTest : BaseTest() {
    @Rule
    @JvmField
    val activityTestRule = ActivityTestRule(MainActivity::class.java, false, false)

    @Inject
    lateinit var repository: Repository

    @Before
    fun setUpMainActivity() {
        val testAppComponent = testAppComponentBuilder
            .repositoryModule(RepositoryModule())
            .build()

        app.appComponent = testAppComponent
        testAppComponent.inject(this)

        activityTestRule.launchActivity(null)
        applicationContext = activityTestRule.activity.applicationContext
    }
}
