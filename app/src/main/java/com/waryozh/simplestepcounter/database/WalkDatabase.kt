package com.waryozh.simplestepcounter.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WalkDay::class],
    version = 1,
    exportSchema = false
)
abstract class WalkDatabase : RoomDatabase() {
    abstract val walkDatabaseDao: WalkDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: WalkDatabase? = null

        fun getInstance(context: Context): WalkDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                WalkDatabase::class.java,
                "walk_history_database"
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}
