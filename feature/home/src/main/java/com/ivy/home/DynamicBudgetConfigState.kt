package com.ivy.home

import androidx.compose.runtime.Immutable

@Immutable
enum class PeriodTypeChoice(val displayLabel: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    SALARY("Salary cycle"),
    CUSTOM("Custom"),
}

@Immutable
data class DynamicBudgetConfigState(
    val loading: Boolean = true,
    val amountInput: String = "",
    val amountError: String? = null,
    val periodType: PeriodTypeChoice = PeriodTypeChoice.MONTHLY,
    val salaryPaydayInput: String = "",
    val salaryPaydayError: String? = null,
    val customStartInput: String = "",
    val customStartError: String? = null,
    val customEndInput: String = "",
    val customEndError: String? = null,
    val saving: Boolean = false,
    val hasExistingBudget: Boolean = false,
    val isBaseCurrencyTnd: Boolean = true,
) {
    val canSave: Boolean
        get() = !saving &&
            amountError == null &&
            salaryPaydayError == null &&
            customStartError == null &&
            customEndError == null &&
            amountInput.isNotBlank()
}
