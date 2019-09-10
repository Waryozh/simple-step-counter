package com.waryozh.simplestepcounter.repositories

import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.waryozh.simplestepcounter.App

object Repository {
    private const val IS_RUNNING = "IS_RUNNING"
    private const val SHOULD_RUN = "SHOULD_RUN"
    private const val STEPS_TAKEN = "STEPS_TAKEN"
    private const val STEPS_TAKEN_CORRECTION = "STEPS_TAKEN_CORRECTION"
    private const val STEP_LENGTH = "STEP_LENGTH"

    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(App.applicationContext())
    }

    private var stepCounterAvailableListener: ((Boolean) -> Unit)? = null
    private var stepCounterServiceRunningListener: ((Boolean) -> Unit)? = null
    private var stepsTakenListener: ((Int) -> Unit)? = null
    private var stepLengthListener: ((Int) -> Unit)? = null

    fun setOnStepCounterAvailableListener(listener: (Boolean) -> Unit) {
        this.stepCounterAvailableListener = listener
    }

    fun setOnStepCounterServiceRunningListener(listener: (Boolean) -> Unit) {
        this.stepCounterServiceRunningListener = listener
    }

    fun setOnStepsTakenListener(listener: (Int) -> Unit) {
        this.stepsTakenListener = listener
    }

    fun setOnStepLengthListener(listener: (Int) -> Unit) {
        this.stepLengthListener = listener
    }

    fun removeListeners() {
        this.stepCounterAvailableListener = null
        this.stepCounterServiceRunningListener = null
        this.stepsTakenListener = null
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

    fun getStepsTaken() = prefs.getInt(STEPS_TAKEN, 0)

    fun setStepsTaken(steps: Int) {
        var correction = prefs.getInt(STEPS_TAKEN_CORRECTION, 0)
        with(prefs.edit()) {
            // Step Counter returns the number of steps taken by the user since the last reboot,
            // so we have to calculate the offset when starting a new step recording session.
            if (correction == 0) {
                correction = steps
                putInt(STEPS_TAKEN_CORRECTION, correction)
            }
            putInt(STEPS_TAKEN, steps - correction)
            apply()
        }
        stepsTakenListener?.invoke(steps - correction)
    }

    fun resetStepCounter() {
        // When resetting the counter, set the STEPS_TAKEN to the current value of STEPS_TAKEN_CORRECTION.
        // That way the effective value of STEPS_TAKEN will become zero
        // and STEPS_TAKEN_CORRECTION will be properly updated when a new session is started.
        setStepsTaken(prefs.getInt(STEPS_TAKEN_CORRECTION, 0))
    }

    fun getStepLength() = prefs.getInt(STEP_LENGTH, 0)

    // Step length is stored in centimeters
    fun setStepLength(length: Int) {
        with(prefs.edit()) {
            putInt(STEP_LENGTH, length)
            apply()
        }
        stepLengthListener?.invoke(length)
    }
}
