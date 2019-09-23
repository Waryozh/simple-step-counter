package com.waryozh.simplestepcounter.repositories

import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import com.waryozh.simplestepcounter.database.WalkDatabaseDao
import com.waryozh.simplestepcounter.database.WalkDay
import com.waryozh.simplestepcounter.util.calculateDistance
import com.waryozh.simplestepcounter.util.getCurrentDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Repository @Inject constructor(
    private val prefs: SharedPreferences,
    private val walkDao: WalkDatabaseDao
) {
    companion object {
        private const val IS_RUNNING = "IS_RUNNING"
        private const val SHOULD_RUN = "SHOULD_RUN"

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        const val STEPS_TAKEN_CORRECTION = "STEPS_TAKEN_CORRECTION"

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        const val STEP_LENGTH = "STEP_LENGTH"
    }

    private var stepCounterAvailableListener: ((Boolean) -> Unit)? = null
    private var stepCounterServiceRunningListener: ((Boolean) -> Unit)? = null
    private var stepLengthListener: ((Int) -> Unit)? = null

    val today: LiveData<WalkDay> = walkDao.getToday()

    init {
        if (today.value == null) {
            runBlocking {
                walkDao.insert(WalkDay())
            }
        }
    }

    fun setOnStepCounterAvailableListener(listener: (Boolean) -> Unit) {
        this.stepCounterAvailableListener = listener
    }

    fun setOnStepCounterServiceRunningListener(listener: (Boolean) -> Unit) {
        this.stepCounterServiceRunningListener = listener
    }

    fun setOnStepLengthListener(listener: (Int) -> Unit) {
        this.stepLengthListener = listener
    }

    fun removeListeners() {
        this.stepCounterAvailableListener = null
        this.stepCounterServiceRunningListener = null
        this.stepLengthListener = null
    }

    fun setStepCounterAvailable(isAvailable: Boolean) {
        stepCounterAvailableListener?.invoke(isAvailable)
    }

    fun getServiceRunning() = prefs.getBoolean(IS_RUNNING, false)

    fun setServiceRunning(isRunning: Boolean) {
        with(prefs.edit()) {
            putBoolean(IS_RUNNING, isRunning)
            putBoolean(SHOULD_RUN, false)
            apply()
        }
        stepCounterServiceRunningListener?.invoke(isRunning)
    }

    fun getServiceShouldRun() = prefs.getBoolean(SHOULD_RUN, true)

    fun setServiceShouldRun(shouldRun: Boolean) {
        with(prefs.edit()) {
            putBoolean(SHOULD_RUN, shouldRun)
            apply()
        }
    }

    private fun setStepsCorrection(correction: Int) {
        with(prefs.edit()) {
            putInt(STEPS_TAKEN_CORRECTION, correction)
            apply()
        }
    }

    /**
     * If Repository's today is actually today, updates its values.
     * Otherwise, creates a new [WalkDay] and inserts it into DB.
     *
     * @param stepLength User's step length.
     *
     * @param steps Steps as reported by StepCounter sensor.
     * Used to update correction offset if starting a new recording session.
     * Default value means that correction offset should not be updated.
     *
     * @param actualSteps Steps adjusted by the value of [STEPS_TAKEN_CORRECTION] from the preferences.
     * Default value means that today's steps should not be updated.
     */
    private suspend fun upsertToday(stepLength: Int, steps: Int = -1, actualSteps: Int = -1) {
        val currentDate = getCurrentDate()
        if (currentDate == today.value?.date) {
            val localToday = today.value!!
            if (actualSteps != -1) {
                localToday.steps = actualSteps
            }
            localToday.distance = calculateDistance(localToday.steps, stepLength)
            walkDao.update(localToday)
        } else {
            if (steps != -1) {
                // Update correction offset, because we are starting a new recording session
                setStepsCorrection(steps)
            }
            walkDao.insert(WalkDay(date = currentDate))
        }
    }

    suspend fun setStepsTaken(steps: Int) {
        withContext(Dispatchers.IO) {
            var correction = prefs.getInt(STEPS_TAKEN_CORRECTION, 0)
            if (correction == 0) {
                // Step Counter returns the number of steps taken by the user since the last reboot,
                // so we have to calculate the offset when starting a new step recording session.
                correction = steps
                setStepsCorrection(correction)
            }
            upsertToday(getStepLength(), steps, steps - correction)
        }
    }

    suspend fun resetStepCounter() {
        // When resetting the counter, set STEPS_TAKEN_CORRECTION to zero
        // so that a new session would be started on next sensor event.
        setStepsCorrection(0)
        upsertToday(getStepLength(), 0, 0)
    }

    fun getStepLength() = prefs.getInt(STEP_LENGTH, 0)

    // Step length is stored in centimeters
    suspend fun setStepLength(length: Int) {
        withContext(Dispatchers.IO) {
            with(prefs.edit()) {
                putInt(STEP_LENGTH, length)
                apply()
            }
            upsertToday(length)
        }
        stepLengthListener?.invoke(length)
    }
}
