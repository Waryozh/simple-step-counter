package com.waryozh.simplestepcounter.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface WalkDatabaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(day: WalkDay): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(day: WalkDay)

    @Query("DELETE FROM daily_walk_data_table")
    suspend fun deleteAllDays()

    @Query("SELECT * FROM daily_walk_data_table ORDER BY dayId DESC LIMIT 1")
    fun getToday(): LiveData<WalkDay>

    @Query("SELECT * FROM daily_walk_data_table ORDER BY dayId DESC")
    fun getAllDays(): LiveData<List<WalkDay>>
}
