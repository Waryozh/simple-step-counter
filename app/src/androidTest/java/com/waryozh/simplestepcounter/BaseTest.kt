package com.waryozh.simplestepcounter

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.rule.ActivityTestRule
import com.waryozh.simplestepcounter.database.WalkDatabase
import com.waryozh.simplestepcounter.database.WalkDatabaseDao
import com.waryozh.simplestepcounter.database.WalkDay
import com.waryozh.simplestepcounter.repositories.Repository
import com.waryozh.simplestepcounter.repositories.Repository.STEPS_TAKEN_CORRECTION
import com.waryozh.simplestepcounter.ui.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule

abstract class BaseTest {
    @Rule
    @JvmField
    val mainActivityTestRule = ActivityTestRule(MainActivity::class.java)

    // Swaps the background executor used by the Architecture Components with a different one
    // which executes each task synchronously.
    // Needed to invoke LiveData.setValue from the tests.
    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    protected lateinit var applicationContext: Context
    protected lateinit var prefs: SharedPreferences

    protected val repository = Repository

    private lateinit var db: WalkDatabase
    protected lateinit var walkDao: WalkDatabaseDao

    @Before
    fun initialize() {
        applicationContext = mainActivityTestRule.activity.applicationContext
        prefs = applicationContext.getSharedPreferences(
            PreferenceManager.getDefaultSharedPreferencesName(applicationContext),
            Context.MODE_PRIVATE
        )
        db = Room.inMemoryDatabaseBuilder(applicationContext, WalkDatabase::class.java)
            // Allow main thread queries, just for testing
            .allowMainThreadQueries()
            .build()
        walkDao = db.walkDatabaseDao
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
            walkDao.insert(WalkDay(steps = steps))
        }
    }

    protected fun setStepsCorrection(correction: Int) {
        with(prefs.edit()) {
            putInt(STEPS_TAKEN_CORRECTION, correction)
            apply()
        }
    }
}
