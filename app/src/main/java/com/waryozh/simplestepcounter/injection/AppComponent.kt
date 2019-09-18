package com.waryozh.simplestepcounter.injection

import com.waryozh.simplestepcounter.App
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        PrefsModule::class,
        DatabaseModule::class,
        RepositoryModule::class
    ]
)
interface AppComponent {
    fun inject(app: App)

    fun plus(module: MainActivityComponent.Module): MainActivityComponent

    fun plus(module: StepCounterServiceComponent.Module): StepCounterServiceComponent
}
