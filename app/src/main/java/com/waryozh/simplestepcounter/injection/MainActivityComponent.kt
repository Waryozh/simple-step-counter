package com.waryozh.simplestepcounter.injection

import com.waryozh.simplestepcounter.ui.MainActivity
import dagger.Subcomponent

@Subcomponent(modules = [MainActivityComponent.Module::class])
interface MainActivityComponent {
    fun inject(activity: MainActivity)

    @dagger.Module
    class Module
}
