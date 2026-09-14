package com.ivy.domain.usecase.budget

import com.ivy.data.model.AccountId
import com.ivy.data.model.CategoryId
import java.util.UUID

/**
 * Domain model representing the user's dynamic budget configuration.
 *
 * @property id Unique identifier for this configuration.
 * @property periodType The type of budget period (Weekly, Monthly, Salary, Custom).
 * @property budgetLimitMinorUnits The spending limit in minor units (e.g. millimes for TND).
 * @property currencyCode The ISO 4217 currency code. This must match the currency of 
 * the transactions being counted. Cross-currency transactions are filtered out in v1.
 * @property includedAccountIds Set of accounts eligible for this budget. An EMPTY set 
 * means all accounts are eligible.
 * @property includedCategoryIds Set of categories eligible for this budget. An EMPTY set 
 * means all categories are eligible.
 * @property includeIncomeInBudget Whether to add included income transactions to the 
 * spending pool.
 */
data class DynamicBudgetConfig(
    val id: UUID,
    val periodType: BudgetPeriodType,
    val budgetLimitMinorUnits: Long,
    val currencyCode: String,
    val includedAccountIds: Set<AccountId>,
    val includedCategoryIds: Set<CategoryId>,
    val includeIncomeInBudget: Boolean
) {
    init {
        require(budgetLimitMinorUnits >= 0) { "budgetLimitMinorUnits must be >= 0" }
        require(currencyCode.isNotBlank()) { "currencyCode must not be blank" }
    }

    companion object {
        /**
         * Default config for a brand new user. Weekly period, zero budget,
         * empty inclusion sets (meaning "all accounts" and "all categories"),
         * income not included in budget.
         */
        fun default(): DynamicBudgetConfig {
            return DynamicBudgetConfig(
                id = UUID.randomUUID(),
                periodType = BudgetPeriodType.Weekly,
                budgetLimitMinorUnits = 0L,
                currencyCode = "TND",
                includedAccountIds = emptySet(),
                includedCategoryIds = emptySet(),
                includeIncomeInBudget = false
            )
        }
    }
}
