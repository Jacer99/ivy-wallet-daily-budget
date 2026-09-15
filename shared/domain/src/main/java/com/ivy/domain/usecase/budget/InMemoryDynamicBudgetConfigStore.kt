package com.ivy.domain.usecase.budget

import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory store used until the Room-backed implementation exists.
 * Survives across process lifetime only. Starts with DynamicBudgetConfig.default().
 */
@Deprecated("Superseded by RoomDynamicBudgetConfigStore. Retained as a lightweight real-object implementation for use-case tests. Will be removed in a later cleanup task.")
@Singleton
class InMemoryDynamicBudgetConfigStore @Inject constructor() : DynamicBudgetConfigStore {

    private val state = MutableStateFlow(DynamicBudgetConfig.default())

    override suspend fun load(): DynamicBudgetConfig = state.value

    override suspend fun save(config: DynamicBudgetConfig) {
        state.value = config
    }
}
