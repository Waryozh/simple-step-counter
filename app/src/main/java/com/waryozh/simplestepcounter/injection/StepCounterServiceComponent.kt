package com.waryozh.simplestepcounter.injection

import com.waryozh.simplestepcounter.services.StepCounter
import dagger.Subcomponent

@Subcomponent(modules = [StepCounterServiceComponent.Module::class])
interface StepCounterServiceComponent {
    fun inject(stepCounter: StepCounter)

    @dagger.Module
    class Module
}
