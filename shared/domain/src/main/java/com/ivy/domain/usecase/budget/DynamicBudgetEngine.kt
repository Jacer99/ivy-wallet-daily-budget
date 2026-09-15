package com.ivy.domain.usecase.budget

import com.ivy.data.model.AllocationMode
import java.time.LocalDate
import javax.inject.Inject

data class BudgetExpenseInput(
    val transactionDate: LocalDate,
    val amount: Long,
    val mode: AllocationMode
)

data class BudgetEngineInput(
    val period: BudgetPeriod,
    val budgetLimit: Long,
    val includedIncome: Long,
    val reservationsTotal: Long,
    val expenses: List<BudgetExpenseInput>,
    val today: LocalDate
)

data class BudgetSnapshot(
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val capacity: Long,
    val periodSpentRaw: Long,
    val periodReserved: Long,
    val periodRemaining: Long,
    val daysRemaining: Int,
    val openingAllowance: Long,
    val todayCharges: Long,
    val remainingAllowance: Long,
    val tomorrowProjection: Long?
)

class DynamicBudgetEngine @Inject constructor(
    private val scheduler: ExpenseAllocationScheduler
) {
    fun calculate(input: BudgetEngineInput): BudgetSnapshot {
        if (input.budgetLimit < 0) throw IllegalArgumentException("budgetLimit cannot be negative")
        if (input.includedIncome < 0) throw IllegalArgumentException("includedIncome cannot be negative")
        if (input.reservationsTotal < 0) throw IllegalArgumentException("reservationsTotal cannot be negative")
        if (input.today.isAfter(input.period.end)) throw IllegalArgumentException("today cannot be after period end")

        for (expense in input.expenses) {
            if (expense.amount <= 0) {
                throw IllegalArgumentException("Expense amount must be greater than zero")
            }
        }

        val capacity = input.budgetLimit + input.includedIncome
        val periodReserved = input.reservationsTotal

        val startDay = if (input.today.isBefore(input.period.start)) input.period.start else input.period.start
        var daysRemaining = input.period.daysRemaining(input.today)
        if (daysRemaining < 1) {
            daysRemaining = 1
        }

        val eligibleExpenses = input.expenses.filter {
            val d = it.transactionDate
            !d.isBefore(input.period.start) && !d.isAfter(input.period.end)
        }

        val periodSpentRaw = eligibleExpenses.sumOf { it.amount }
        val periodRemaining = capacity - periodSpentRaw - periodReserved

        val allocationsMap = mutableMapOf<LocalDate, Long>()
        for (expense in eligibleExpenses) {
            val entries = scheduler.schedule(expense.amount, expense.transactionDate, expense.mode, input.period.end)
            for (entry in entries) {
                allocationsMap[entry.date] = (allocationsMap[entry.date] ?: 0L) + entry.amount
            }
        }

        var chargesBeforeToday = 0L
        for ((date, amt) in allocationsMap) {
            if (date.isBefore(input.today)) {
                chargesBeforeToday += amt
            }
        }

        val poolAtStartOfToday = capacity - periodReserved - chargesBeforeToday
        val openingAllowance = poolAtStartOfToday / daysRemaining

        val todayCharges = allocationsMap[input.today] ?: 0L
        val remainingAllowance = openingAllowance - todayCharges

        val tomorrowProjection = if (input.today == input.period.end) {
            null
        } else {
            val daysFromTomorrow = daysRemaining - 1
            if (daysFromTomorrow <= 0) {
                null
            } else {
                val chargesBeforeTomorrow = chargesBeforeToday + todayCharges
                val poolAtStartOfTomorrow = capacity - periodReserved - chargesBeforeTomorrow
                val tomorrowOpening = poolAtStartOfTomorrow / daysFromTomorrow
                val tomorrowCharges = allocationsMap[input.today.plusDays(1)] ?: 0L
                tomorrowOpening - tomorrowCharges
            }
        }

        return BudgetSnapshot(
            periodStart = input.period.start,
            periodEnd = input.period.end,
            capacity = capacity,
            periodSpentRaw = periodSpentRaw,
            periodReserved = periodReserved,
            periodRemaining = periodRemaining,
            daysRemaining = daysRemaining,
            openingAllowance = openingAllowance,
            todayCharges = todayCharges,
            remainingAllowance = remainingAllowance,
            tomorrowProjection = tomorrowProjection
        )
    }
}
