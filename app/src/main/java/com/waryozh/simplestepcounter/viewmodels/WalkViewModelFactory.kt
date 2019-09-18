package com.waryozh.simplestepcounter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.waryozh.simplestepcounter.repositories.Repository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalkViewModelFactory @Inject constructor(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WalkViewModel::class.java)) {
            @Suppress("unchecked_cast")
            return WalkViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
