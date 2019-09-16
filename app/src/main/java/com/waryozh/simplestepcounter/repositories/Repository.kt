package com.waryozh.simplestepcounter.repositories

import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.annotation.VisibleForTesting
import com.waryozh.simplestepcounter.App
import com.waryozh.simplestepcounter.database.WalkDatabase
import com.waryozh.simplestepcounter.database.WalkDatabaseDao
import com.waryozh.simplestepcounter.database.WalkDay
import com.waryozh.simplestepcounter.util.calculateDistance
import com.waryozh.simplestepcounter.util.getCurrentDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

object Repository {
    private const val IS_RUNNING = "IS_RUNNING"
    private const val SHOULD_RUN = "SHOULD_RUN"

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    const val STEPS_TAKEN_CORRECTION = "STEPS_TAKEN_CORRECTION"

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    const val STEP_LENGTH = "STEP_LENGTH"

    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(App.applicationContext())
    }

    private val walkDao: WalkDatabaseDao by lazy {
        WalkDatabase.getInstance(App.applicationContext()).walkDatabaseDao
    }

    private var today: WalkDay = runBlocking {
        var localToday = WalkDay()
        val dbToday = walkDao.getToday()
        if (dbToday == null) {
            localToday.dayId = walkDao.insert(localToday)
        } else {
            localToday = dbToday
        }
        localToday
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

    fun getStepsTaken() = today.steps

    suspend fun setStepsTaken(steps: Int) {
        withContext(Dispatchers.IO) {
            var correction = prefs.getInt(STEPS_TAKEN_CORRECTION, 0)
            if (correction == 0) {
                correction = steps
                with(prefs.edit()) {
                    // Step Counter returns the number of steps taken by the user since the last reboot,
                    // so we have to calculate the offset when starting a new step recording session.
                    putInt(STEPS_TAKEN_CORRECTION, correction)
                    apply()
                }
            }

            val actualSteps = steps - correction

            // This repository's init block guarantees that there already is a record for today in DB
            val dbToday = walkDao.getToday()!!

            val currentDate = getCurrentDate()

            // If Repository's today is actually today, update its values.
            // Otherwise, create a new day and insert it into DB.
            if (currentDate == today.date) {
                today.steps = actualSteps
                today.distance = calculateDistance(actualSteps, getStepLength())
                // Avoid unnecessary writes to DB when nothing changed
                if ((today.dayId == dbToday.dayId) && (today.steps != dbToday.steps || today.distance != dbToday.distance)) {
                    walkDao.update(today)
                }
            } else {
                // Update correction offset, because we are starting a new recording session
                with(prefs.edit()) {
                    putInt(STEPS_TAKEN_CORRECTION, steps)
                    apply()
                }
                today = WalkDay(date = currentDate)
                // Do not forget to update today's id as it is autogenerated by Room on insertion
                today.dayId = walkDao.insert(today)
            }
            stepsTakenListener?.invoke(actualSteps)
        }
    }

    suspend fun resetStepCounter() {
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
