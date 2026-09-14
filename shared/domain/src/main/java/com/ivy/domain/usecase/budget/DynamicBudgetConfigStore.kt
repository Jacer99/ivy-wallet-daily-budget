package com.ivy.domain.usecase.budget

/**
 * Source of truth for the user's dynamic budget configuration.
 *
 * v1: implemented by an in-memory store.
 * Future: implemented by a Room-backed store. Only the implementation
 * changes; this interface must remain stable.
 */
interface DynamicBudgetConfigStore {
    suspend fun load(): DynamicBudgetConfig
    suspend fun save(config: DynamicBudgetConfig)
}
