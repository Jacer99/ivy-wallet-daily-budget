package com.ivy.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ivy.domain.usecase.budget.BudgetPeriodType
import com.ivy.domain.usecase.budget.DynamicBudgetConfig
import com.ivy.domain.usecase.budget.DynamicBudgetConfigStore
import com.ivy.navigation.Navigation
import com.ivy.ui.ComposeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.UUID
import javax.inject.Inject

private val DATE_REGEX = Regex("""^\d{2}/\d{2}/\d{4}$""")
private val DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu")
    .withResolverStyle(ResolverStyle.STRICT)

@Stable
@HiltViewModel
class DynamicBudgetConfigViewModel @Inject constructor(
    private val store: DynamicBudgetConfigStore,
    private val nav: Navigation,
) : ComposeViewModel<DynamicBudgetConfigState, DynamicBudgetConfigEvent>() {

    private var loading by mutableStateOf(true)
    private var amountInput by mutableStateOf("")
    private var amountError by mutableStateOf<String?>(null)
    private var periodType by mutableStateOf(PeriodTypeChoice.MONTHLY)
    private var salaryPaydayInput by mutableStateOf("")
    private var salaryPaydayError by mutableStateOf<String?>(null)
    private var customStartInput by mutableStateOf("")
    private var customStartError by mutableStateOf<String?>(null)
    private var customEndInput by mutableStateOf("")
    private var customEndError by mutableStateOf<String?>(null)
    private var saving by mutableStateOf(false)

    private var originalConfig: DynamicBudgetConfig? = null

    @Composable
    override fun uiState(): DynamicBudgetConfigState {
        LaunchedEffect(Unit) {
            load()
        }

        return DynamicBudgetConfigState(
            loading = loading,
            amountInput = amountInput,
            amountError = amountError,
            periodType = periodType,
            salaryPaydayInput = salaryPaydayInput,
            salaryPaydayError = salaryPaydayError,
            customStartInput = customStartInput,
            customStartError = customStartError,
            customEndInput = customEndInput,
            customEndError = customEndError,
            saving = saving,
            hasExistingBudget = (originalConfig?.budgetLimitMinorUnits ?: 0L) > 0L,
        )
    }

    override fun onEvent(event: DynamicBudgetConfigEvent) {
        viewModelScope.launch {
            when (event) {
                DynamicBudgetConfigEvent.Back -> nav.back()
                is DynamicBudgetConfigEvent.AmountChanged -> {
                    amountInput = event.raw
                    amountError = validateAmount(event.raw)
                }
                is DynamicBudgetConfigEvent.PeriodTypeChanged -> {
                    periodType = event.choice
                }
                is DynamicBudgetConfigEvent.SalaryPaydayChanged -> {
                    salaryPaydayInput = event.raw
                    salaryPaydayError = validatePayday(event.raw)
                }
                is DynamicBudgetConfigEvent.CustomStartChanged -> {
                    customStartInput = event.raw
                    customStartError = validateDate(event.raw)
                    customEndError = validateDateRange(
                        customStartInput,
                        customEndInput,
                    )
                }
                is DynamicBudgetConfigEvent.CustomEndChanged -> {
                    customEndInput = event.raw
                    customEndError = validateDate(event.raw)
                        ?: validateDateRange(customStartInput, customEndInput)
                }
                DynamicBudgetConfigEvent.Save -> save()
                DynamicBudgetConfigEvent.RemoveBudget -> removeBudget()
            }
        }
    }

    private suspend fun load() {
        loading = true
        try {
            val config = store.load()
            originalConfig = config

            if (config.budgetLimitMinorUnits > 0L) {
                amountInput = BigDecimal
                    .valueOf(config.budgetLimitMinorUnits, 3)
                    .stripTrailingZeros()
                    .toPlainString()
            }

            periodType = when (val t = config.periodType) {
                BudgetPeriodType.Weekly -> PeriodTypeChoice.WEEKLY
                BudgetPeriodType.Monthly -> PeriodTypeChoice.MONTHLY
                is BudgetPeriodType.Salary -> {
                    salaryPaydayInput = t.payday.toString()
                    PeriodTypeChoice.SALARY
                }
                is BudgetPeriodType.Custom -> {
                    customStartInput = t.start.format(DATE_FORMATTER)
                    customEndInput = t.end.format(DATE_FORMATTER)
                    PeriodTypeChoice.CUSTOM
                }
            }
        } finally {
            loading = false
        }
    }

    private suspend fun save() {
        amountError = validateAmount(amountInput)
        if (periodType == PeriodTypeChoice.SALARY) {
            salaryPaydayError = validatePayday(salaryPaydayInput)
        }
        if (periodType == PeriodTypeChoice.CUSTOM) {
            customStartError = validateDate(customStartInput)
            customEndError = validateDate(customEndInput)
                ?: validateDateRange(customStartInput, customEndInput)
        }

        if (!canSaveInternal()) return

        saving = true
        try {
            val minorUnits = parseMinorUnits(amountInput) ?: return
            val period = when (periodType) {
                PeriodTypeChoice.WEEKLY -> BudgetPeriodType.Weekly
                PeriodTypeChoice.MONTHLY -> BudgetPeriodType.Monthly
                PeriodTypeChoice.SALARY -> BudgetPeriodType.Salary(
                    payday = salaryPaydayInput.toInt(),
                )
                PeriodTypeChoice.CUSTOM -> BudgetPeriodType.Custom(
                    start = LocalDate.parse(customStartInput.trim(), DATE_FORMATTER),
                    end = LocalDate.parse(customEndInput.trim(), DATE_FORMATTER),
                )
            }

            val existing = originalConfig
            val config = DynamicBudgetConfig(
                id = existing?.id ?: UUID.randomUUID(),
                periodType = period,
                budgetLimitMinorUnits = minorUnits,
                currencyCode = "TND",
                includedAccountIds = existing?.includedAccountIds ?: emptySet(),
                includedCategoryIds = existing?.includedCategoryIds ?: emptySet(),
                includeIncomeInBudget = existing?.includeIncomeInBudget ?: false,
            )
            store.save(config)
            nav.back()
        } finally {
            saving = false
        }
    }

    private suspend fun removeBudget() {
        val existing = originalConfig ?: return
        saving = true
        try {
            store.save(existing.copy(budgetLimitMinorUnits = 0L))
            nav.back()
        } finally {
            saving = false
        }
    }

    private fun canSaveInternal(): Boolean =
        amountError == null &&
            salaryPaydayError == null &&
            customStartError == null &&
            customEndError == null &&
            amountInput.isNotBlank()

    // -------- validation --------

    private fun validateAmount(raw: String): String? {
        if (raw.isBlank()) return null
        return try {
            val bd = BigDecimal(raw.trim())
            when {
                bd <= BigDecimal.ZERO -> "Amount must be greater than zero"
                bd.scale() > 3 -> "TND supports at most 3 decimal places"
                else -> null
            }
        } catch (e: NumberFormatException) {
            "Enter a valid number"
        }
    }

    private fun validatePayday(raw: String): String? {
        if (raw.isBlank()) return "Required"
        val n = raw.trim().toIntOrNull() ?: return "Enter a number"
        return if (n in 1..31) null else "Must be between 1 and 31"
    }

    private fun validateDate(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return "Required"
        if (!DATE_REGEX.matches(trimmed)) return "Use format dd/MM/yyyy"
        return try {
            LocalDate.parse(trimmed, DATE_FORMATTER)
            null
        } catch (e: DateTimeParseException) {
            "Use format dd/MM/yyyy"
        }
    }

    private fun validateDateRange(start: String, end: String): String? {
        val sTrim = start.trim()
        val eTrim = end.trim()
        if (sTrim.isBlank() || eTrim.isBlank()) return null
        return try {
            val s = LocalDate.parse(sTrim, DATE_FORMATTER)
            val e = LocalDate.parse(eTrim, DATE_FORMATTER)
            if (s.isAfter(e)) "Start must be before end" else null
        } catch (e: DateTimeParseException) {
            null
        }
    }

    private fun parseMinorUnits(raw: String): Long? {
        return try {
            BigDecimal(raw.trim())
                .setScale(3, RoundingMode.HALF_UP)
                .movePointRight(3)
                .longValueExact()
        } catch (e: ArithmeticException) {
            null
        }
    }
}
