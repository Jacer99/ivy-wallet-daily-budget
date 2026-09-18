package com.ivy.home

import com.ivy.domain.usecase.budget.BudgetPeriodType
import com.ivy.domain.usecase.budget.BudgetSnapshot
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Pure mapping from the domain budget snapshot to the UI state for the
 * SafeToSpendCard. No formatting, no string resolution, no side effects.
 *
 * @param hasBudget true when a non-zero budget limit is configured.
 * @param snapshot the raw domain snapshot from the engine.
 * @param periodType the budget period type (Weekly, Monthly, Salary, Custom).
 * @param today current date, defaults to LocalDate.now().
 */
internal fun mapToSafeToSpendCardState(
    hasBudget: Boolean,
    snapshot: BudgetSnapshot,
    periodType: BudgetPeriodType? = null,
    today: LocalDate = LocalDate.now(),
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

    val paydayLabel: String? = if (periodType is BudgetPeriodType.Salary) {
        if (today == snapshot.periodStart) {
            "Payday today"
        } else {
            val nextPayday = snapshot.periodEnd.plusDays(1)
            val days = ChronoUnit.DAYS.between(today, nextPayday).toInt()
            when {
                days < 0 -> "Payday today"
                days == 0 -> "Payday today"
                days == 1 -> "Left until payday · 1 day"
                else -> "Left until payday · $days days"
            }
        }
    } else {
        null
    }

    val displayPeriodRemaining = if (snapshot.periodRemaining < 0L) 0L else snapshot.periodRemaining

    return SafeToSpendCardState.Active(
        remainingAllowanceMinorUnits = displayRemaining,
        openingAllowanceMinorUnits = snapshot.openingAllowance,
        todayChargesMinorUnits = snapshot.todayCharges,
        tomorrowProjectionMinorUnits = snapshot.tomorrowProjection,
        message = message,
        paydayLabel = paydayLabel,
        periodBudgetMinorUnits = snapshot.capacity,
        periodSpentMinorUnits = snapshot.periodSpentRaw,
        periodAvailableMinorUnits = displayPeriodRemaining,
        daysLeft = snapshot.daysRemaining,
    )
}
