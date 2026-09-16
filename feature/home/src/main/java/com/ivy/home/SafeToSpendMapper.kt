package com.ivy.home

import com.ivy.domain.usecase.budget.BudgetSnapshot

/**
 * Pure mapping from the domain budget snapshot to the UI state for the
 * SafeToSpendCard. No formatting, no string resolution, no side effects.
 *
 * @param hasBudget true when a non-zero budget limit is configured.
 * @param snapshot the raw domain snapshot from the engine.
 */
internal fun mapToSafeToSpendCardState(
    hasBudget: Boolean,
    snapshot: BudgetSnapshot,
): SafeToSpendCardState {
    if (!hasBudget) return SafeToSpendCardState.NoBudget

    val displayRemaining = if (snapshot.remainingAllowance < 0L) 0L
                           else snapshot.remainingAllowance

    val message: SafeToSpendMessage? = when {
        snapshot.remainingAllowance < 0L ->
            SafeToSpendMessage.OverToday(
                overByMinorUnits = -snapshot.remainingAllowance
            )
        snapshot.periodRemaining <= 0L ->
            SafeToSpendMessage.PeriodExhausted
        else -> null
    }

    return SafeToSpendCardState.Active(
        remainingAllowanceMinorUnits = displayRemaining,
        openingAllowanceMinorUnits = snapshot.openingAllowance,
        todayChargesMinorUnits = snapshot.todayCharges,
        tomorrowProjectionMinorUnits = snapshot.tomorrowProjection,
        message = message,
    )
}
