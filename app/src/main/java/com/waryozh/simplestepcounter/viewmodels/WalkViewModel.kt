package com.waryozh.simplestepcounter.viewmodels

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.waryozh.simplestepcounter.repositories.Repository

class WalkViewModel : ViewModel() {
    private val repository = Repository

    private var _stepsTaken = MutableLiveData<Long>()
    val stepsTaken: LiveData<Long>
        get() = _stepsTaken

    private var _stepCounterNotAvailableVisibility = MutableLiveData<Int>()
    val stepCounterNotAvailableVisibility: LiveData<Int>
        get() = _stepCounterNotAvailableVisibility

    private var _startButtonEnabled = MutableLiveData<Boolean>()
    val startButtonEnabled: LiveData<Boolean>
        get() = _startButtonEnabled

    private var _stopButtonEnabled = MutableLiveData<Boolean>()
    val stopButtonEnabled: LiveData<Boolean>
        get() = _stopButtonEnabled

    private var _serviceRunning = MutableLiveData<Boolean>()
    val serviceRunning: LiveData<Boolean>
        get() = _serviceRunning

    init {
        _stepsTaken.value = repository.getStepsTaken()
        _serviceRunning.value = repository.getServiceRunning()
        _stepCounterNotAvailableVisibility.value = View.GONE
        _startButtonEnabled.value = !(_serviceRunning.value ?: true)
        _stopButtonEnabled.value = _serviceRunning.value

        repository.setOnStepCounterAvailableListener { isAvailable ->
            _stepCounterNotAvailableVisibility.value = if (isAvailable) View.GONE else View.VISIBLE
            _startButtonEnabled.value = isAvailable && !(_serviceRunning.value ?: true)
            _stopButtonEnabled.value = isAvailable && (_serviceRunning.value ?: true)
        }

        repository.setOnStepCounterServiceRunningListener { isRunning ->
            _serviceRunning.value = isRunning
            _startButtonEnabled.value = (_stepCounterNotAvailableVisibility.value == View.GONE) && !isRunning
            _stopButtonEnabled.value = (_stepCounterNotAvailableVisibility.value == View.GONE) && isRunning
        }

        repository.setOnStepsTakenListener { steps ->
            _stepsTaken.value = steps
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.removeListeners()
    }
}
