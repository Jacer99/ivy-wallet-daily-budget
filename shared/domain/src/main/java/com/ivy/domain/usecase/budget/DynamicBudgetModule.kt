package com.ivy.domain.usecase.budget

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DynamicBudgetModule {

    @Binds
    @Singleton
    abstract fun bindDynamicBudgetConfigStore(
        impl: RoomDynamicBudgetConfigStore,
    ): DynamicBudgetConfigStore
}
