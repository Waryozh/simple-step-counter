package com.waryozh.simplestepcounter

import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.waryozh.simplestepcounter.database.WalkDatabaseDao
import com.waryozh.simplestepcounter.database.WalkDay
import com.waryozh.simplestepcounter.repositories.Repository
import com.waryozh.simplestepcounter.testing.TimeUnits
import com.waryozh.simplestepcounter.testing.add
import com.waryozh.simplestepcounter.testing.format
import com.waryozh.simplestepcounter.util.calculateDistance
import com.waryozh.simplestepcounter.util.getCurrentDate
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

class RepositoryTest {
    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: Repository
    private lateinit var today: WalkDay

    private val prefs = mockkClass(SharedPreferences::class, relaxed = true)
    private val prefsEditor = mockkClass(SharedPreferences.Editor::class, relaxed = true)
    private val dao = mockkClass(WalkDatabaseDao::class, relaxed = true)

    @Before
    fun setUp() {
        today = WalkDay(1, 1000, 700)
        every { dao.getToday() } returns MutableLiveData<WalkDay>(today)
        every { prefs.edit() } returns prefsEditor
        repository = Repository(prefs, dao)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun getServiceShouldRun() {
        val shouldRun = true
        every { prefs.getBoolean(Repository.SHOULD_RUN, any()) } returns shouldRun
        assertEquals(shouldRun, repository.getServiceShouldRun())
    }

    @Test
    fun setServiceShouldRun() {
        val shouldRun = true

        repository.setServiceShouldRun(shouldRun)

        verifySequence {
            prefsEditor.putBoolean(Repository.SHOULD_RUN, shouldRun)
            prefsEditor.apply()
        }
    }

    @Test
    fun getStepLength() {
        val stepLength = 42
        every { prefs.getInt(Repository.STEP_LENGTH, any()) } returns stepLength
        assertEquals(stepLength, repository.getStepLength())
    }

    @Test
    fun getServiceRunning() {
        val isRunning = true
        every { prefs.getBoolean(Repository.IS_RUNNING, any()) } returns isRunning
        assertEquals(isRunning, repository.getServiceRunning())
    }

    @Test
    fun setServiceRunning_running() {
        val isRunning = true

        repository.setServiceRunning(isRunning)

        verifySequence {
            prefsEditor.putBoolean(Repository.IS_RUNNING, isRunning)
            prefsEditor.putBoolean(Repository.SHOULD_RUN, false)
            prefsEditor.apply()
        }
    }

    @Test
    fun setServiceRunning_notRunningAndDatabaseNotEmpty() {
        val isRunning = false

        repository.setServiceRunning(isRunning)

        verifySequence {
            prefsEditor.putBoolean(Repository.IS_RUNNING, isRunning)
            prefsEditor.putBoolean(Repository.SHOULD_RUN, false)
            prefsEditor.putInt(Repository.STEPS_ON_STOP, today.steps)
            prefsEditor.apply()
        }
    }

    @Test
    fun setServiceRunning_notRunningAndEmptyDatabase() {
        val isRunning = false
        every { dao.getToday() } returns MutableLiveData<WalkDay>(null)

        // Here we recreate Repository after setUp to test the case when there are no records in the database
        repository = Repository(prefs, dao)
        repository.setServiceRunning(isRunning)

        verifySequence {
            prefsEditor.putBoolean(Repository.IS_RUNNING, isRunning)
            prefsEditor.putBoolean(Repository.SHOULD_RUN, false)
            prefsEditor.apply()
        }
    }

    @Test
    fun setStepsTaken_newSession_repoIsUpToDate() = runBlocking {
        val steps = 1000
        val stepLength = 84
        val correction = 0
        mockkStatic("com.waryozh.simplestepcounter.util.UtilKt")
        every { getCurrentDate() } returns today.date
        every { prefs.getInt(Repository.STEPS_TAKEN_CORRECTION, any()) } returns correction
        every { prefs.getInt(Repository.STEP_LENGTH, any()) } returns stepLength

        repository.setStepsTaken(steps)

        coVerifySequence {
            dao.getToday()
            prefs.getInt(Repository.STEPS_TAKEN_CORRECTION, 0)

            prefs.edit()
            prefsEditor.putInt(Repository.STEPS_TAKEN_CORRECTION, steps)
            prefsEditor.apply()

            prefs.getInt(Repository.STEP_LENGTH, 0)
            dao.update(today.copy(steps = 0, distance = 0))
        }
    }

    @Test
    fun setStepsTaken_newSession_repoIsOutdated() = runBlocking {
        val steps = 1000
        val stepLength = 84
        val correction = 0
        val newDate = Date().add(1, TimeUnits.DAY).format()
        mockkStatic("com.waryozh.simplestepcounter.util.UtilKt")
        every { getCurrentDate() } returns newDate
        every { prefs.getInt(Repository.STEPS_TAKEN_CORRECTION, any()) } returns correction
        every { prefs.getInt(Repository.STEP_LENGTH, any()) } returns stepLength

        repository.setStepsTaken(steps)

        coVerifySequence {
            dao.getToday()
            prefs.getInt(Repository.STEPS_TAKEN_CORRECTION, 0)

            prefs.edit()
            prefsEditor.putInt(Repository.STEPS_TAKEN_CORRECTION, steps)
            prefsEditor.apply()

            prefs.getInt(Repository.STEP_LENGTH, 0)
            dao.insert(WalkDay(date = newDate))
        }
    }

    @Test
    fun setStepsTaken_continuedSessionWithoutServicePause_repoIsUpToDate() = runBlocking {
        val correction = 200
        val steps = today.steps + correction + 100
        val stepLength = 84
        val distance = 300
        val stepsOnStop = 0
        mockkStatic("com.waryozh.simplestepcounter.util.UtilKt")
        every { getCurrentDate() } returns today.date
        every { calculateDistance(any(), any()) } returns distance
        every { prefs.getInt(Repository.STEPS_TAKEN_CORRECTION, any()) } returns correction
        every { prefs.getInt(Repository.STEPS_ON_STOP, any()) } returns stepsOnStop
        every { prefs.getInt(Repository.STEP_LENGTH, any()) } returns stepLength

        repository.setStepsTaken(steps)

        coVerifySequence {
            dao.getToday()
            prefs.getInt(Repository.STEPS_TAKEN_CORRECTION, 0)
            prefs.getInt(Repository.STEPS_ON_STOP, 0)
            prefs.getInt(Repository.STEP_LENGTH, 0)
            dao.update(today.copy(steps = steps - correction, distance = distance))
        }
    }

    @Test
    fun setStepsTaken_continuedSessionWithoutServicePause_repoIsOutdated() = runBlocking {
        val correction = 200
        val stepsDiff = 100
        val steps = today.steps + correction + stepsDiff
        val stepLength = 84
        val distance = 300
        val stepsOnStop = 0
        val newDate = Date().add(1, TimeUnits.DAY).format()
        mockkStatic("com.waryozh.simplestepcounter.util.UtilKt")
        every { getCurrentDate() } returns newDate
        every { calculateDistance(any(), any()) } returns distance
        every { prefs.getInt(Repository.STEPS_TAKEN_CORRECTION, any()) } returns correction
        every { prefs.getInt(Repository.STEPS_ON_STOP, any()) } returns stepsOnStop
        every { prefs.getInt(Repository.STEP_LENGTH, any()) } returns stepLength

        repository.setStepsTaken(steps)

        coVerifySequence {
            dao.getToday()
            prefs.getInt(Repository.STEPS_TAKEN_CORRECTION, 0)
            prefs.getInt(Repository.STEPS_ON_STOP, 0)

            prefs.edit()
            prefsEditor.putInt(Repository.STEPS_TAKEN_CORRECTION, correction + today.steps)
            prefsEditor.apply()

            prefs.getInt(Repository.STEP_LENGTH, 0)
            dao.insert(WalkDay(steps = stepsDiff, distance = distance, date = newDate))
        }
    }

    @Test
    fun setStepsTaken_stepsDetectedWhileServiceStopped_repoIsUpToDate() = runBlocking {
        val correction = 200
        val stepsOnStop = today.steps
        val stepsWhileStopped = 100
        val steps = today.steps + correction + stepsWhileStopped
        val stepLength = 84
        val distance = 300
        mockkStatic("com.waryozh.simplestepcounter.util.UtilKt")
        every { getCurrentDate() } returns today.date
        every { calculateDistance(any(), any()) } returns distance
        every { prefs.getInt(Repository.STEPS_TAKEN_CORRECTION, any()) } returns correction
        every { prefs.getInt(Repository.STEPS_ON_STOP, any()) } returns stepsOnStop
        every { prefs.getInt(Repository.STEP_LENGTH, any()) } returns stepLength

        repository.setStepsTaken(steps)

        coVerifySequence {
            dao.getToday()
            prefs.getInt(Repository.STEPS_TAKEN_CORRECTION, 0)
            prefs.getInt(Repository.STEPS_ON_STOP, 0)

            prefs.edit()
            prefsEditor.putInt(Repository.STEPS_ON_STOP, 0)
            prefsEditor.putInt(Repository.STEPS_TAKEN_CORRECTION, correction + stepsWhileStopped)
            prefsEditor.apply()

            prefs.getInt(Repository.STEP_LENGTH, 0)
            dao.update(today.copy(distance = distance))
        }
    }

    @Test
    fun setStepsTaken_stepsDetectedWhileServiceStopped_repoIsOutdated() = runBlocking {
        val correction = 200
        val stepsOnStop = today.steps
        val stepsWhileStopped = 100
        val steps = today.steps + correction + stepsWhileStopped
        val stepLength = 84
        val distance = 300
        val newDate = Date().add(1, TimeUnits.DAY).format()
        mockkStatic("com.waryozh.simplestepcounter.util.UtilKt")
        every { getCurrentDate() } returns newDate
        every { calculateDistance(any(), any()) } returns distance
        every { prefs.getInt(Repository.STEPS_TAKEN_CORRECTION, any()) } returns correction
        every { prefs.getInt(Repository.STEPS_ON_STOP, any()) } returns stepsOnStop
        every { prefs.getInt(Repository.STEP_LENGTH, any()) } returns stepLength

        repository.setStepsTaken(steps)

        coVerifySequence {
            dao.getToday()
            prefs.getInt(Repository.STEPS_TAKEN_CORRECTION, 0)
            prefs.getInt(Repository.STEPS_ON_STOP, 0)

            prefs.edit()
            prefsEditor.putInt(Repository.STEPS_ON_STOP, 0)
            prefsEditor.putInt(Repository.STEPS_TAKEN_CORRECTION, correction + today.steps)
            prefsEditor.apply()

            prefs.getInt(Repository.STEP_LENGTH, 0)
            dao.insert(WalkDay(steps = stepsWhileStopped, distance = distance, date = newDate))
        }
    }

    @Test
    fun resetStepCounter_repoIsUpToDate() = runBlocking {
        mockkStatic("com.waryozh.simplestepcounter.util.UtilKt")
        every { getCurrentDate() } returns today.date

        repository.resetStepCounter()

        coVerifySequence {
            dao.getToday()
            prefsEditor.putInt(Repository.STEPS_TAKEN_CORRECTION, 0)
            prefsEditor.apply()
            dao.update(today.copy(steps = 0, distance = 0))
        }
    }

    @Test
    fun resetStepCounter_repoIsOutdated() = runBlocking {
        val newDate = Date().add(1, TimeUnits.DAY).format()
        mockkStatic("com.waryozh.simplestepcounter.util.UtilKt")
        every { getCurrentDate() } returns newDate

        repository.resetStepCounter()

        coVerifySequence {
            dao.getToday()
            prefsEditor.putInt(Repository.STEPS_TAKEN_CORRECTION, 0)
            prefsEditor.apply()
            dao.insert(WalkDay(date = newDate))
        }
    }

    @Test
    fun setStepLength_repoIsUpToDate() = runBlocking {
        val stepLength = 42
        val distance = 300
        mockkStatic("com.waryozh.simplestepcounter.util.UtilKt")
        every { getCurrentDate() } returns today.date
        every { calculateDistance(any(), any()) } returns distance

        repository.setStepLength(stepLength)

        coVerifySequence {
            dao.getToday()
            prefsEditor.putInt(Repository.STEP_LENGTH, stepLength)
            prefsEditor.apply()
            dao.update(today.copy(distance = distance))
        }
    }

    @Test
    fun setStepLength_repoIsOutdated() = runBlocking {
        val stepLength = 42
        val newDate = Date().add(1, TimeUnits.DAY).format()
        mockkStatic("com.waryozh.simplestepcounter.util.UtilKt")
        every { getCurrentDate() } returns newDate

        repository.setStepLength(stepLength)

        coVerifySequence {
            dao.getToday()
            prefsEditor.putInt(Repository.STEP_LENGTH, stepLength)
            prefsEditor.apply()
            dao.insert(WalkDay(date = newDate))
        }
    }
}
