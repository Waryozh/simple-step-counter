package com.waryozh.simplestepcounter.injection

import com.waryozh.simplestepcounter.BaseTest
import com.waryozh.simplestepcounter.MainActivityBaseTest
import com.waryozh.simplestepcounter.StatsActivityBaseTest
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
interface TestAppComponent : AppComponent {
    fun inject(test: BaseTest)
    fun inject(test: MainActivityBaseTest)
    fun inject(test: StatsActivityBaseTest)
}
