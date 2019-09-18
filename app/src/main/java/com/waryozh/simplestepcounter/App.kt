package com.waryozh.simplestepcounter

import android.app.Application
import com.waryozh.simplestepcounter.injection.AppComponent
import com.waryozh.simplestepcounter.injection.AppModule
import com.waryozh.simplestepcounter.injection.DaggerAppComponent

class App : Application() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()

        appComponent.inject(this)
    }
}
