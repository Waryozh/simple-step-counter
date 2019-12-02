package com.waryozh.simplestepcounter

import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.waryozh.simplestepcounter.database.WalkDay
import com.waryozh.simplestepcounter.repositories.StatsRepository
import com.waryozh.simplestepcounter.testing.getOrAwaitValue
import com.waryozh.simplestepcounter.viewmodels.StatsViewModel
import io.mockk.every
import io.mockk.mockkClass
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StatsViewModelTest {
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: StatsViewModel

    private val repository = mockkClass(StatsRepository::class)

    @Test
    fun emptyDatabase() {
        val days = emptyList<WalkDay>()
        every { repository.getAllDays() } returns MutableLiveData<List<WalkDay>>(days)

        viewModel = StatsViewModel(repository)

        assertEquals(days, viewModel.walkDays.getOrAwaitValue())
        assertEquals(View.VISIBLE, viewModel.noDataVisibility.getOrAwaitValue())
    }

    @Test
    fun nonEmptyDatabase() {
        val days = listOf(
            WalkDay(dayId = 5, steps = 1000, distance = 400, date = "18.09.2019"),
            WalkDay(dayId = 4, steps = 321, distance = 12, date = "16.09.2019"),
            WalkDay(dayId = 1, steps = 1234, distance = 500, date = "12.09.2019")
        )
        every { repository.getAllDays() } returns MutableLiveData<List<WalkDay>>(days)

        viewModel = StatsViewModel(repository)

        assertEquals(days, viewModel.walkDays.getOrAwaitValue())
        assertEquals(View.GONE, viewModel.noDataVisibility.getOrAwaitValue())
    }
}
