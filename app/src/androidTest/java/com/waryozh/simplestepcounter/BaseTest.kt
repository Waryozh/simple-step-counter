package com.waryozh.simplestepcounter

import android.content.Context
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.waryozh.simplestepcounter.database.WalkDatabase
import com.waryozh.simplestepcounter.database.WalkDatabaseDao
import com.waryozh.simplestepcounter.injection.*
import com.waryozh.simplestepcounter.repositories.Repository
import com.waryozh.simplestepcounter.repositories.Repository.Companion.STEPS_TAKEN_CORRECTION
import com.waryozh.simplestepcounter.ui.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject

abstract class BaseTest {
    @Rule
    @JvmField
    val mainActivityTestRule = ActivityTestRule(MainActivity::class.java, false, false)

    // Swaps the background executor used by the Architecture Components with a different one
    // which executes each task synchronously.
    // Needed to invoke LiveData.setValue from the tests.
    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    protected lateinit var applicationContext: Context

    @Inject lateinit var prefs: SharedPreferences
    @Inject lateinit var db: WalkDatabase
    @Inject lateinit var walkDao: WalkDatabaseDao
    @Inject lateinit var repository: Repository

    @Before
    fun initialize() {
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as App
        val testAppComponent = DaggerTestAppComponent.builder()
            .appModule(AppModule(app))
            .prefsModule(PrefsModule())
            .databaseModule(TestDatabaseModule())
            .repositoryModule(RepositoryModule())
            .build()
        app.appComponent = testAppComponent
        testAppComponent.inject(this)
        mainActivityTestRule.launchActivity(null)
        applicationContext = mainActivityTestRule.activity.applicationContext
    }

    @After
    fun closeDb() {
        db.close()
    }

    protected fun setPrefs(steps: Int, correction: Int) {
        with(prefs.edit()) {
            putInt(STEPS_TAKEN_CORRECTION, correction)
            apply()
        }
        runBlocking {
            val today = walkDao.getToday()!!
            today.steps = steps
            walkDao.update(today)
        }
    }

    protected fun setStepsCorrection(correction: Int) {
        with(prefs.edit()) {
            putInt(STEPS_TAKEN_CORRECTION, correction)
            apply()
        }
    }
}
