package com.waryozh.simplestepcounter.injection

import androidx.room.Room
import com.waryozh.simplestepcounter.App
import com.waryozh.simplestepcounter.database.WalkDatabase
import com.waryozh.simplestepcounter.database.WalkDatabaseDao
import com.waryozh.simplestepcounter.testing.OpenForTesting
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@OpenForTesting
@Module
class DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(application: App): WalkDatabase {
        return Room.databaseBuilder(
            application,
            WalkDatabase::class.java,
            "walk_history_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideWalkDao(db: WalkDatabase): WalkDatabaseDao {
        return db.walkDatabaseDao
    }
}
