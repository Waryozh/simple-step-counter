package com.waryozh.simplestepcounter.injection

import android.content.Context
import android.content.SharedPreferences
import com.waryozh.simplestepcounter.App

class TestPrefsModule: PrefsModule() {
    override fun providePrefs(application: App): SharedPreferences {
        return application.getSharedPreferences("TestSharedPrefs", Context.MODE_PRIVATE)
    }
}
