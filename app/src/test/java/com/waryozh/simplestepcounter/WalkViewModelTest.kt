package com.waryozh.simplestepcounter

import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.waryozh.simplestepcounter.database.WalkDay
import com.waryozh.simplestepcounter.repositories.Repository
import com.waryozh.simplestepcounter.testing.getOrAwaitValue
import com.waryozh.simplestepcounter.viewmodels.WalkViewModel
import io.mockk.every
import io.mockk.mockkClass
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class WalkViewModelTest {
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: WalkViewModel

    private val repository = mockkClass(Repository::class, relaxed = true)

    @Test
    fun stepsAndDistance_todayIsNull() {
        every { repository.today } returns MutableLiveData<WalkDay>(null)

        viewModel = WalkViewModel(repository)

        assertEquals(0, viewModel.stepsTaken.getOrAwaitValue())
        assertEquals(0, viewModel.distanceWalked.getOrAwaitValue())
    }

    @Test
    fun stepsAndDistance_todayNotNull() {
        val steps = 1000
        val distance = 650
        val today = WalkDay(steps = steps, distance = distance)
        every { repository.today } returns MutableLiveData<WalkDay>(today)

        viewModel = WalkViewModel(repository)

        assertEquals(steps, viewModel.stepsTaken.getOrAwaitValue())
        assertEquals(distance, viewModel.distanceWalked.getOrAwaitValue())
    }

    @Test
    fun stepLength() {
        val stepLength = 70
        every { repository.getStepLength() } returns stepLength

        viewModel = WalkViewModel(repository)

        assertEquals(stepLength, viewModel.stepLength.value)
    }

    @Test
    fun stepCounterNotAvailableVisibility() {
        viewModel = WalkViewModel(repository)
        assertEquals(View.GONE, viewModel.stepCounterNotAvailableVisibility.value)
    }

    @Test
    fun shouldStartService_serviceIsRunningAndShouldRun() {
        every { repository.getServiceRunning() } returns true
        every { repository.getServiceShouldRun() } returns true

        viewModel = WalkViewModel(repository)

        assertEquals(false, viewModel.shouldStartService.value)
    }

    @Test
    fun shouldStartService_serviceIsRunningAndShouldNotRun() {
        every { repository.getServiceRunning() } returns true
        every { repository.getServiceShouldRun() } returns false

        viewModel = WalkViewModel(repository)

        assertEquals(false, viewModel.shouldStartService.value)
    }

    @Test
    fun shouldStartService_serviceNotRunningAndShouldRun() {
        every { repository.getServiceRunning() } returns false
        every { repository.getServiceShouldRun() } returns true

        viewModel = WalkViewModel(repository)

        assertEquals(true, viewModel.shouldStartService.value)
    }

    @Test
    fun shouldStartService_serviceNotRunningAndShouldNotRun() {
        every { repository.getServiceRunning() } returns false
        every { repository.getServiceShouldRun() } returns false

        viewModel = WalkViewModel(repository)

        assertEquals(false, viewModel.shouldStartService.value)
    }

    @Test
    fun buttonsState_serviceIsRunning() {
        every { repository.getServiceRunning() } returns true

        viewModel = WalkViewModel(repository)

        assertEquals(false, viewModel.startButtonEnabled.value)
        assertEquals(true, viewModel.stopButtonEnabled.value)
    }

    @Test
    fun buttonsState_serviceNotRunning() {
        every { repository.getServiceRunning() } returns false

        viewModel = WalkViewModel(repository)

        assertEquals(true, viewModel.startButtonEnabled.value)
        assertEquals(false, viewModel.stopButtonEnabled.value)
    }
}
