package com.waryozh.simplestepcounter

import android.content.Context
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import com.waryozh.simplestepcounter.database.WalkDatabase
import com.waryozh.simplestepcounter.database.WalkDatabaseDao
import com.waryozh.simplestepcounter.injection.AppModule
import com.waryozh.simplestepcounter.injection.DaggerTestAppComponent
import com.waryozh.simplestepcounter.injection.TestDatabaseModule
import com.waryozh.simplestepcounter.injection.TestPrefsModule
import com.waryozh.simplestepcounter.repositories.Repository.Companion.STEPS_TAKEN_CORRECTION
import com.waryozh.simplestepcounter.repositories.Repository.Companion.STEP_LENGTH
import com.waryozh.simplestepcounter.testing.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject

abstract class BaseTest {
    // Swaps the background executor used by the Architecture Components with a different one
    // which executes each task synchronously.
    // Needed to invoke LiveData.setValue from the tests.
    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    protected lateinit var applicationContext: Context

    @Inject
    lateinit var prefs: SharedPreferences

    @Inject
    lateinit var db: WalkDatabase

    @Inject
    lateinit var walkDao: WalkDatabaseDao

    protected val app: App = ApplicationProvider.getApplicationContext()

    protected val testAppComponentBuilder: DaggerTestAppComponent.Builder = DaggerTestAppComponent.builder()
        .appModule(AppModule(app))
        .prefsModule(TestPrefsModule())
        .databaseModule(TestDatabaseModule())

    @Before
    fun setUp() {
        testAppComponentBuilder.build().inject(this)

        // Since we are using fake temporary SharedPreferences for testing,
        // put some valid value into STEP_LENGTH before launching MainActivity
        // so that SetStepLengthDialog would not be shown.
        prefs.edit {
            putInt(STEP_LENGTH, 1)
        }
    }

    @After
    fun tearDown() {
        prefs.edit {
            clear()
        }
        db.close()
    }

    protected fun setPrefs(steps: Int, correction: Int) {
        prefs.edit {
            putInt(STEPS_TAKEN_CORRECTION, correction)
        }
        runBlocking {
            val today = walkDao.getToday().getOrAwaitValue()
            today.steps = steps
            walkDao.update(today)
        }
    }

    protected fun setStepsCorrection(correction: Int) {
        prefs.edit {
            putInt(STEPS_TAKEN_CORRECTION, correction)
        }
    }
}
