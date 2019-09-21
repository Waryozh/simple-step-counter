package com.waryozh.simplestepcounter.repositories

import com.waryozh.simplestepcounter.database.WalkDatabaseDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(private val walkDao: WalkDatabaseDao) {
    fun getAllDays() = walkDao.getAllDays()
}
