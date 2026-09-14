package com.ivy.domain.usecase.budget

import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * The single entry point that the ViewModel will call to obtain a safe-to-spend snapshot.
 *
 * Steps:
 * 1. Load the user's config from the store.
 * 2. Delegate to DynamicBudgetRepository.calculate().
 *
 * `today` and `zoneId` are passed in by the caller (ViewModel) rather than read
 * from a system clock here, so the use case remains deterministic and testable.
 */
class GetDynamicBudgetSnapshotUseCase @Inject constructor(
    private val store: DynamicBudgetConfigStore,
    private val repository: DynamicBudgetRepository,
) {

    suspend operator fun invoke(
        today: LocalDate,
        zoneId: ZoneId,
    ): BudgetSnapshot {
        val config = store.load()
        return repository.calculate(config, today, zoneId)
    }
}
