package com.waryozh.simplestepcounter.injection

import com.waryozh.simplestepcounter.repositories.Repository
import com.waryozh.simplestepcounter.ui.MainActivity
import com.waryozh.simplestepcounter.viewmodels.WalkViewModelFactory
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [MainActivityComponent.Module::class])
interface MainActivityComponent {
    fun inject(activity: MainActivity)

    @dagger.Module
    class Module {
        @Provides
        fun provideWalkViewModelFactory(repository: Repository): WalkViewModelFactory {
            return WalkViewModelFactory(repository)
        }
    }
}
