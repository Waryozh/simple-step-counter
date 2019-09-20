package com.waryozh.simplestepcounter.injection

import com.waryozh.simplestepcounter.ui.StatsActivity
import dagger.Subcomponent

@Subcomponent(modules = [StatsActivityComponent.Module::class])
interface StatsActivityComponent {
    fun inject(activity: StatsActivity)

    @dagger.Module
    class Module
}
