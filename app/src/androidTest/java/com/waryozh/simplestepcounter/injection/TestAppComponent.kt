package com.waryozh.simplestepcounter.injection

import com.waryozh.simplestepcounter.BaseTest
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
}
