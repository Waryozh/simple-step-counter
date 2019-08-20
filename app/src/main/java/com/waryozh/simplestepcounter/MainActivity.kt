package com.waryozh.simplestepcounter

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.waryozh.simplestepcounter.databinding.ActivityMainBinding
import com.waryozh.simplestepcounter.services.StepCounter
import com.waryozh.simplestepcounter.viewmodels.WalkViewModel
import com.waryozh.simplestepcounter.viewmodels.WalkViewModelFactory

class MainActivity : AppCompatActivity() {

    private val walkViewModel: WalkViewModel by lazy {
        ViewModelProviders.of(this, WalkViewModelFactory()).get(WalkViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.walkViewModel = walkViewModel
        binding.lifecycleOwner = this

        if (walkViewModel.shouldStartService.value == true) {
            startStepCounterService()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> walkViewModel.resetStepCounter()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startStepCounterService() {
        val intent = Intent(this, StepCounter::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    fun startStepCounter(view: View) {
        startStepCounterService()
    }

    fun stopStepCounter(view: View) {
        val intent = Intent(this, StepCounter::class.java)
        stopService(intent)
    }
}
