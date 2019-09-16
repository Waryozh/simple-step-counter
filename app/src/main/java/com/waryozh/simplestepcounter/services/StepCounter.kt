package com.waryozh.simplestepcounter.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.waryozh.simplestepcounter.ui.MainActivity
import com.waryozh.simplestepcounter.R
import com.waryozh.simplestepcounter.repositories.Repository
import kotlinx.coroutines.*

class StepCounter : Service(), SensorEventListener {
    companion object {
        private const val PRIMARY_CHANNEL_ID = "primary_notification_channel"
        private const val NOTIFICATION_ID = 1234
    }

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder

    private val repository = Repository

    private val stepCounterJob = Job()
    private val stepCounterScope = CoroutineScope(Dispatchers.Main + stepCounterJob)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(PRIMARY_CHANNEL_ID, getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_ID,
            Intent(this, MainActivity::class.java).apply { setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_SINGLE_TOP) },
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

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepCounterSensor != null) {
            repository.setStepCounterAvailable(true)
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
            startForeground(NOTIFICATION_ID, notifyWithTitle(getString(R.string.steps_taken, repository.getStepsTaken())))
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
        super.onDestroy()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        stepCounterScope.launch {
            repository.setStepsTaken(event!!.values[0].toInt())
        }
        notifyWithTitle(getString(R.string.steps_taken, repository.getStepsTaken()))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}
