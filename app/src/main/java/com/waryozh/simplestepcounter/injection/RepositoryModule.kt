package com.waryozh.simplestepcounter.injection

import android.content.SharedPreferences
import com.waryozh.simplestepcounter.database.WalkDatabaseDao
import com.waryozh.simplestepcounter.repositories.Repository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RepositoryModule {
    @Provides
    @Singleton
    fun provideRepository(prefs: SharedPreferences, dao: WalkDatabaseDao): Repository {
        return Repository(prefs, dao)
    }
}
