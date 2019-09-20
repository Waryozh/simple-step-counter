package com.waryozh.simplestepcounter.viewmodels

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
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

    val walkDays: LiveData<List<WalkDay>> = repository.getAllDays()

    val noDataVisibility = MediatorLiveData<Int>()

    init {
        noDataVisibility.addSource(walkDays) { data ->
            noDataVisibility.value = if (data.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}
