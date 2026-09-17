package com.ivy.home

sealed interface DynamicBudgetConfigEvent {
    data object Back : DynamicBudgetConfigEvent

    data class AmountChanged(val raw: String) : DynamicBudgetConfigEvent

    data class PeriodTypeChanged(val choice: PeriodTypeChoice)
        : DynamicBudgetConfigEvent

    data class SalaryPaydayChanged(val raw: String)
        : DynamicBudgetConfigEvent

    data class CustomStartChanged(val raw: String)
        : DynamicBudgetConfigEvent

    data class CustomEndChanged(val raw: String)
        : DynamicBudgetConfigEvent

    data object Save : DynamicBudgetConfigEvent
}
