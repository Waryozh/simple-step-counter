package com.waryozh.simplestepcounter.viewmodels

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.waryozh.simplestepcounter.repositories.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class WalkViewModel @Inject constructor(private val repository: Repository) : ViewModel() {
    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val stepsTaken: LiveData<Int> = Transformations.map(repository.today) { it?.steps ?: 0 }
    val distanceWalked: LiveData<Int> = Transformations.map(repository.today) { it?.distance ?: 0 }

    private var _stepLength = MutableLiveData<Int>()
    val stepLength: LiveData<Int>
        get() = _stepLength

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

    private var _shouldStartService = MutableLiveData<Boolean>()
    val shouldStartService: LiveData<Boolean>
        get() = _shouldStartService

    init {
        _stepLength.value = repository.getStepLength()
        _serviceRunning.value = repository.getServiceRunning()
        _shouldStartService.value = (_serviceRunning.value == false) && repository.getServiceShouldRun()
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
            _shouldStartService.value = false
            _startButtonEnabled.value = (_stepCounterNotAvailableVisibility.value == View.GONE) && !isRunning
            _stopButtonEnabled.value = (_stepCounterNotAvailableVisibility.value == View.GONE) && isRunning
        }

        repository.setOnStepLengthListener { length ->
            _stepLength.value = length
        }
    }

    fun resetStepCounter() {
        viewModelScope.launch {
            repository.resetStepCounter()
        }
    }

    fun setStepLength(length: Int) {
        viewModelScope.launch {
            repository.setStepLength(length)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        repository.setServiceShouldRun(true)
        repository.removeListeners()
    }
}
