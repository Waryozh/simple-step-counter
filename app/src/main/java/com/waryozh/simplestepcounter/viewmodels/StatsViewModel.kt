package com.waryozh.simplestepcounter.viewmodels

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.waryozh.simplestepcounter.database.WalkDay
import com.waryozh.simplestepcounter.repositories.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatsViewModel @Inject constructor(private val repository: Repository) : ViewModel() {
    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _walkDays = MutableLiveData<List<WalkDay>>()
    val walkDays: LiveData<List<WalkDay>>
        get() = _walkDays

    val noDataVisibility = MediatorLiveData<Int>()

    init {
        viewModelScope.launch {
            _walkDays.value = repository.getAllDays()
        }

        noDataVisibility.addSource(walkDays) { data ->
            noDataVisibility.value = if (data.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
