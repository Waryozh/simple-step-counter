package com.waryozh.simplestepcounter

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.waryozh.simplestepcounter.services.StepCounter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun startStepCounter(view: View) {
        val intent = Intent(this, StepCounter::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    fun stopStepCounter(view: View) {
        val intent = Intent(this, StepCounter::class.java)
        stopService(intent)
    }
}
