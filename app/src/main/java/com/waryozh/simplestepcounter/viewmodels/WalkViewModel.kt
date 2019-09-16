package com.waryozh.simplestepcounter.viewmodels

import android.view.View
import androidx.lifecycle.*
import com.waryozh.simplestepcounter.repositories.Repository
import com.waryozh.simplestepcounter.util.calculateDistance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class WalkViewModel : ViewModel() {
    private val repository = Repository

    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _stepsTaken = MutableLiveData<Int>()
    val stepsTaken: LiveData<Int>
        get() = _stepsTaken

    private var _stepLength = MutableLiveData<Int>()
    val stepLength: LiveData<Int>
        get() = _stepLength

    val distanceWalked = MediatorLiveData<Int>()

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
        _stepsTaken.value = repository.getStepsTaken()
        _stepLength.value = repository.getStepLength()
        _serviceRunning.value = repository.getServiceRunning()
        _shouldStartService.value = (_serviceRunning.value == false) && repository.getServiceShouldRun()
        _stepCounterNotAvailableVisibility.value = View.GONE
        _startButtonEnabled.value = !(_serviceRunning.value ?: true)
        _stopButtonEnabled.value = _serviceRunning.value

        distanceWalked.addSource(stepsTaken) { steps ->
            distanceWalked.value = calculateDistance(steps, _stepLength.value ?: 0)
        }

        distanceWalked.addSource(stepLength) { length ->
            distanceWalked.value = calculateDistance(_stepsTaken.value ?: 0, length)
        }

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

        repository.setOnStepsTakenListener { steps ->
            _stepsTaken.postValue(steps)
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
        repository.setStepLength(length)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        repository.setServiceShouldRun(true)
        repository.removeListeners()
    }
}
