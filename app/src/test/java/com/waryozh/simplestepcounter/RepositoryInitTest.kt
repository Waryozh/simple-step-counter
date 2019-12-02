package com.waryozh.simplestepcounter

import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.waryozh.simplestepcounter.database.WalkDatabaseDao
import com.waryozh.simplestepcounter.database.WalkDay
import com.waryozh.simplestepcounter.repositories.Repository
import io.mockk.*
import org.junit.Rule
import org.junit.Test

class RepositoryInitTest {
    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: Repository

    private val prefs = mockkClass(SharedPreferences::class)
    private val dao = mockkClass(WalkDatabaseDao::class, relaxed = true)

    @Test
    fun init_emptyDatabase() {
        val today = WalkDay()
        every { dao.getToday() } returns MutableLiveData<WalkDay>(null)

        repository = Repository(prefs, dao)

        coVerifySequence {
            dao.getToday()
            dao.insert(today)
        }
    }

    @Test
    fun init_nonEmptyDatabase() {
        val today = WalkDay()
        every { dao.getToday() } returns MutableLiveData<WalkDay>(today)

        repository = Repository(prefs, dao)

        coVerifySequence {
            dao.getToday()
        }
    }
}
