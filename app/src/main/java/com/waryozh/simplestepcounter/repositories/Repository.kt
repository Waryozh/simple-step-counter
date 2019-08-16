package com.waryozh.simplestepcounter.repositories

import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.waryozh.simplestepcounter.App

object Repository {
    private const val IS_RUNNING = "IS_RUNNING"
    private const val STEPS_TAKEN = "STEPS_TAKEN"

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
            apply()
        }
        stepCounterServiceRunningListener?.invoke(isRunning)
    }

    fun getStepsTaken() = prefs.getLong(STEPS_TAKEN, 0)

    fun setStepsTaken(steps: Long) {
        with(prefs.edit()) {
            putLong(STEPS_TAKEN, steps)
            apply()
        }
        stepsTakenListener?.invoke(steps)
    }
}
