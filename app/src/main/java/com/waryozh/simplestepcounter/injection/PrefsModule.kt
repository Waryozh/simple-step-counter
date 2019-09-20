package com.waryozh.simplestepcounter.injection

import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.waryozh.simplestepcounter.App
import com.waryozh.simplestepcounter.testing.OpenForTesting
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@OpenForTesting
@Module
class PrefsModule {
    @Provides
    @Singleton
    fun providePrefs(application: App): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(application)
    }
}
