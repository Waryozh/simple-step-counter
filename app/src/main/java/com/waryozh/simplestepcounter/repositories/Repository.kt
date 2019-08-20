package com.waryozh.simplestepcounter.repositories

import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.waryozh.simplestepcounter.App

object Repository {
    private const val IS_RUNNING = "IS_RUNNING"
    private const val SHOULD_RUN = "SHOULD_RUN"
    private const val STEPS_TAKEN = "STEPS_TAKEN"
    private const val STEPS_TAKEN_CORRECTION = "STEPS_TAKEN_CORRECTION"

    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(App.applicationContext())
    }

    private var stepCounterAvailableListener: ((Boolean) -> Unit)? = null
    private var stepCounterServiceRunningListener: ((Boolean) -> Unit)? = null
    private var stepsTakenListener: ((Long) -> Unit)? = null

    fun setStepCounterAvailable(isAvailable: Boolean) {
        stepCounterAvailableListener?.invoke(isAvailable)
    }

    fun setOnStepCounterAvailableListener(listener: (Boolean) -> Unit) {
        this.stepCounterAvailableListener = listener
    }

    fun setOnStepCounterServiceRunningListener(listener: (Boolean) -> Unit) {
        this.stepCounterServiceRunningListener = listener
    }

    fun setOnStepsTakenListener(listener: (Long) -> Unit) {
        this.stepsTakenListener = listener
    }

    fun removeListeners() {
        this.stepCounterAvailableListener = null
        this.stepCounterServiceRunningListener = null
        this.stepsTakenListener = null
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

    fun getStepsTaken() = prefs.getLong(STEPS_TAKEN, 0)

    fun setStepsTaken(steps: Long) {
        val stepsInPrefs = prefs.getLong(STEPS_TAKEN, 0)
        var correction = prefs.getLong(STEPS_TAKEN_CORRECTION, 0)
        with(prefs.edit()) {
            // Step Counter returns the number of steps taken by the user since the last reboot,
            // so we have to calculate the offset when starting a new step recording session.
            if (stepsInPrefs == 0L) {
                correction = steps
                putLong(STEPS_TAKEN_CORRECTION, steps)
            }
            putLong(STEPS_TAKEN, steps - correction)
            apply()
        }
        stepsTakenListener?.invoke(steps)
    }
}
