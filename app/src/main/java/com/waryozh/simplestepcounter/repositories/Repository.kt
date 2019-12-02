package com.waryozh.simplestepcounter.repositories

import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
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
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        const val IS_RUNNING = "IS_RUNNING"

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        const val SHOULD_RUN = "SHOULD_RUN"

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        const val STEPS_ON_STOP = "STEPS_ON_STOP"

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
        today.observeForever(object : Observer<WalkDay> {
            override fun onChanged(day: WalkDay?) {
                if (day == null) {
                    runBlocking {
                        walkDao.insert(WalkDay())
                    }
                } else {
                    today.removeObserver(this)
                }
            }
        })
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
            // Step Counter sensor records all steps taken since the last device reboot,
            // but we want to show only the steps taken while our service was running.
            // When stopping the recording session, set STEPS_ON_STOP to the current number of steps.
            // It will be used to calculate the new offset when starting a new recording session.
            if (!isRunning && today.value != null) {
                putInt(STEPS_ON_STOP, today.value!!.steps)
            }
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

    private suspend fun upsertToday(localToday: WalkDay, preserveSteps: Boolean = false) {
        val currentDate = getCurrentDate()
        if (currentDate == localToday.date) {
            walkDao.update(localToday)
        } else {
            if (preserveSteps) {
                walkDao.insert(WalkDay(steps = localToday.steps, distance = localToday.distance, date = currentDate))
            } else {
                walkDao.insert(WalkDay(date = currentDate))
            }
        }
    }

    suspend fun setStepsTaken(steps: Int) {
        withContext(Dispatchers.IO) {
            val repoIsOutdated = today.value?.date != getCurrentDate()
            var correction = prefs.getInt(STEPS_TAKEN_CORRECTION, 0)
            if (correction == 0) {
                // Step Counter returns the number of steps taken by the user since the last reboot,
                // so we have to calculate the offset when starting a new step recording session.
                correction = steps
                setStepsCorrection(correction)
            } else {
                val stepsOnStop = prefs.getInt(STEPS_ON_STOP, 0)
                if (stepsOnStop == 0) {
                    if (repoIsOutdated) {
                        correction += today.value!!.steps
                        setStepsCorrection(correction)
                    }
                } else {
                    // Step Counter sensor might have detected some steps while our service was stopped,
                    // so to ignore those steps, we recalculate the offset using STEPS_ON_STOP,
                    // which is the number of steps recorded on service stop.
                    correction = if (repoIsOutdated) correction + today.value!!.steps else steps - stepsOnStop
                    with(prefs.edit()) {
                        putInt(STEPS_ON_STOP, 0)
                        putInt(STEPS_TAKEN_CORRECTION, correction)
                        apply()
                    }
                }
            }

            val actualSteps = steps - correction
            val distance = calculateDistance(actualSteps, getStepLength())
            upsertToday(today.value!!.copy(steps = actualSteps, distance = distance), preserveSteps = true)
        }
    }

    suspend fun resetStepCounter() {
        // When resetting the counter, set STEPS_TAKEN_CORRECTION to zero
        // so that a new session would be started on next sensor event.
        setStepsCorrection(0)
        upsertToday(today.value!!.copy(steps = 0, distance = 0))
    }

    fun getStepLength() = prefs.getInt(STEP_LENGTH, 0)

    // Step length is stored in centimeters
    suspend fun setStepLength(length: Int) {
        withContext(Dispatchers.IO) {
            with(prefs.edit()) {
                putInt(STEP_LENGTH, length)
                apply()
            }
            val distance = calculateDistance(today.value?.steps ?: 0, length)
            upsertToday(today.value!!.copy(distance = distance))
        }
        stepLengthListener?.invoke(length)
    }
}
