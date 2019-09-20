package com.waryozh.simplestepcounter.injection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.waryozh.simplestepcounter.viewmodels.StatsViewModel
import com.waryozh.simplestepcounter.viewmodels.WalkViewModel
import com.waryozh.simplestepcounter.viewmodels.WalkViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(WalkViewModel::class)
    abstract fun bindWalkViewModel(walkViewModel: WalkViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StatsViewModel::class)
    abstract fun bindStatsViewModel(walkViewModel: StatsViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: WalkViewModelFactory): ViewModelProvider.Factory
}
