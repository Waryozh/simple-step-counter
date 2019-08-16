package com.waryozh.simplestepcounter.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.waryozh.simplestepcounter.R
import com.waryozh.simplestepcounter.repositories.Repository

class StepCounter : Service(), SensorEventListener {
    companion object {
        private const val PRIMARY_CHANNEL_ID = "primary_notification_channel"
        private const val NOTIFICATION_ID = 1234
    }

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder

    private val repository = Repository

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepCounterSensor != null) {
            repository.setStepCounterAvailable(true)
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            repository.setStepCounterAvailable(false)
            stopSelf()
            return START_NOT_STICKY
        }

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(PRIMARY_CHANNEL_ID, "Step counter notification", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationBuilder = NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID).apply {
            setContentTitle(getString(R.string.steps_taken, repository.getStepsTaken()))
            setSmallIcon(R.drawable.ic_directions_walk_black_24dp)
            setOngoing(true)
            setAutoCancel(false)
            setOnlyAlertOnce(true)
            setVisibility(Notification.VISIBILITY_PRIVATE)
            priority = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                NotificationManager.IMPORTANCE_LOW
            } else {
                Notification.PRIORITY_LOW
            }
        }

        val notification = notificationBuilder.build()
        startForeground(NOTIFICATION_ID, notification)
        notificationManager.notify(NOTIFICATION_ID, notification)

        repository.setServiceRunning(true)

        return START_STICKY
    }

    override fun onDestroy() {
        repository.setServiceRunning(false)
        super.onDestroy()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        repository.setStepsTaken(event!!.values[0].toLong())

        notificationBuilder.setContentTitle(getString(R.string.steps_taken, repository.getStepsTaken()))
        val notification = notificationBuilder.build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}
