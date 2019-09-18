package com.waryozh.simplestepcounter.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WalkDay::class],
    version = 1,
    exportSchema = false
)
abstract class WalkDatabase : RoomDatabase() {
    abstract val walkDatabaseDao: WalkDatabaseDao
}
