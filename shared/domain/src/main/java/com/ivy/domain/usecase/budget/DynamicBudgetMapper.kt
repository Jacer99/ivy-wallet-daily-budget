package com.ivy.domain.usecase.budget

import com.ivy.data.model.AllocationMode
import com.ivy.data.model.Expense
import com.ivy.data.model.Transaction
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Mapper that converts Ivy's transaction domain objects into a [BudgetEngineInput]
 * for the [DynamicBudgetEngine].
 *
 * v1 Limitations:
 * - Income inclusion is not yet implemented (defaults to 0L).
 * - Reservations are not yet implemented (defaults to 0L).
 * - Per-expense allocation mode is not yet implemented (defaults to TODAY).
 *
 * @property resolver Used to resolve the budget period boundaries based on configuration.
 */
class DynamicBudgetMapper @Inject constructor(
    private val resolver: BudgetPeriodResolver,
) {

    /**
     * Maps transactions to engine input based on user configuration and the current date.
     *
     * @param zoneId The user's local timezone used to determine the calendar date of transactions.
     */
    fun map(
        transactions: List<Transaction>,
        config: DynamicBudgetConfig,
        today: LocalDate,
        zoneId: ZoneId,
    ): BudgetEngineInput {
        val period = resolver.resolve(config.periodType, today)

        val budgetExpenses = transactions.filterIsInstance<Expense>()
            .filter { expense ->
                expense.settled &&
                    expense.value.asset.code.equals(config.currencyCode, ignoreCase = true) &&
                    (config.includedAccountIds.isEmpty() || expense.account in config.includedAccountIds) &&
                    (config.includedCategoryIds.isEmpty() || expense.category == null || expense.category in config.includedCategoryIds)
            }
            .map { expense ->
                BudgetExpenseInput(
                    transactionDate = expense.time.atZone(zoneId).toLocalDate(),
                    amount = CurrencyMinorUnits.toMinorUnits(expense.value.amount.value, config.currencyCode),
                    // TODO: v1 only - use a real allocationMode field from Transaction when added to schema
                    mode = AllocationMode.TODAY
                )
            }

        return BudgetEngineInput(
            period = period,
            budgetLimit = config.budgetLimitMinorUnits,
            // TODO: income inclusion comes later
            includedIncome = 0L,
            // TODO: reservations come later
            reservationsTotal = 0L,
            expenses = budgetExpenses,
            today = today
        )
    }
}
