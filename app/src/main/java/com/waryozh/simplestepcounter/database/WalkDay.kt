package com.waryozh.simplestepcounter.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.waryozh.simplestepcounter.util.getCurrentDate

@Entity(tableName = "daily_walk_data_table")
data class WalkDay(
    @PrimaryKey(autoGenerate = true)
    var dayId: Long = 0L,
    var steps: Int = 0,
    var distance: Int = 0,
    val date: String = getCurrentDate()
)
