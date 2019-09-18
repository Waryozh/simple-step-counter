package com.waryozh.simplestepcounter.injection

import androidx.room.Room
import com.waryozh.simplestepcounter.App
import com.waryozh.simplestepcounter.database.WalkDatabase

class TestDatabaseModule : DatabaseModule() {
    override fun provideDatabase(application: App): WalkDatabase {
        return Room.inMemoryDatabaseBuilder(application, WalkDatabase::class.java)
            // Allow main thread queries, just for testing
            .allowMainThreadQueries()
            .build()
    }
}
