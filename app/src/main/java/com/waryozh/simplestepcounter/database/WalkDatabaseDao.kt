package com.waryozh.simplestepcounter.database

import androidx.room.*

@Dao
interface WalkDatabaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(day: WalkDay): Long

    @Update
    suspend fun update(day: WalkDay)

    @Query("SELECT * FROM daily_walk_data_table ORDER BY dayId DESC LIMIT 1")
    suspend fun getToday(): WalkDay?
}
