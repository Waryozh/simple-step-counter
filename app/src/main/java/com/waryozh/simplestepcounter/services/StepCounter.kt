package com.waryozh.simplestepcounter.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import com.waryozh.simplestepcounter.App
import com.waryozh.simplestepcounter.R
import com.waryozh.simplestepcounter.injection.StepCounterServiceComponent
import com.waryozh.simplestepcounter.repositories.Repository
import com.waryozh.simplestepcounter.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class StepCounter : LifecycleService(), SensorEventListener {
    companion object {
        private const val PRIMARY_CHANNEL_ID = "primary_notification_channel"
        private const val NOTIFICATION_ID = 1234
    }

    @Inject lateinit var repository: Repository

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var sensorManager: SensorManager

    private val stepsTaken: LiveData<Int> by lazy {
        Transformations.map(repository.today) { it?.steps ?: 0 }
    }

    private val stepCounterJob = Job()
    private val stepCounterScope = CoroutineScope(Dispatchers.Main + stepCounterJob)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        (application as App).appComponent
            .plus(StepCounterServiceComponent.Module())
            .inject(this)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(PRIMARY_CHANNEL_ID, getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_ID,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        notificationBuilder = NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_directions_run_white_24dp)
            setContentIntent(contentPendingIntent)
            setOngoing(true)
            setAutoCancel(false)
            setOnlyAlertOnce(true)
            setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            priority = NotificationCompat.PRIORITY_LOW
            color = ContextCompat.getColor(this@StepCounter, R.color.color_activity_background)
            setColorized(true)
        }

        repository.setServiceRunning(true)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepCounterSensor != null) {
            stepsTaken.observe(this, Observer {
                notifyWithTitle(getString(R.string.steps_taken, stepsTaken.value))
            })
            repository.setStepCounterAvailable(true)
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
            startForeground(NOTIFICATION_ID, notifyWithTitle(getString(R.string.steps_taken, stepsTaken.value)))
        } else {
            repository.setStepCounterAvailable(false)
            startForeground(NOTIFICATION_ID, notifyWithTitle(getString(R.string.notification_step_counter_not_available)))
            stopSelf()
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    private fun notifyWithTitle(title: String): Notification {
        notificationBuilder.setContentTitle(title)
        val notification = notificationBuilder.build()
        notificationManager.notify(NOTIFICATION_ID, notification)
        return notification
    }

    override fun onDestroy() {
        repository.setServiceRunning(false)
        stepCounterJob.cancel()
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        stepCounterScope.launch {
            repository.setStepsTaken(event!!.values[0].toInt())
        }
        notifyWithTitle(getString(R.string.steps_taken, stepsTaken.value))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}
