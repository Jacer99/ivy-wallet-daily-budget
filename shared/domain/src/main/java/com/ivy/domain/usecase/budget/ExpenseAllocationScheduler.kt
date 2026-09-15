package com.ivy.domain.usecase.budget

import com.ivy.data.model.AllocationMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlin.math.min

data class AllocationEntry(
    val date: LocalDate,
    val amount: Long
)

class ExpenseAllocationScheduler @Inject constructor() {

    fun schedule(
        amount: Long,
        transactionDate: LocalDate,
        mode: AllocationMode,
        periodEnd: LocalDate
    ): List<AllocationEntry> {
        if (amount <= 0) {
            throw IllegalArgumentException("Amount must be greater than zero")
        }
        if (periodEnd.isBefore(transactionDate)) {
            throw IllegalArgumentException("Period end date cannot be before transaction date")
        }

        if (mode == AllocationMode.TODAY) {
            return listOf(AllocationEntry(transactionDate, amount))
        }

        val calendarEnd = when (mode) {
            AllocationMode.WEEK -> transactionDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            AllocationMode.MONTH -> transactionDate.with(TemporalAdjusters.lastDayOfMonth())
            else -> throw IllegalStateException("Unsupported allocation mode")
        }

        val effectiveEnd = if (calendarEnd.isBefore(periodEnd)) calendarEnd else periodEnd

        val dates = mutableListOf<LocalDate>()
        var curr = transactionDate
        while (!curr.isAfter(effectiveEnd)) {
            dates.add(curr)
            curr = curr.plusDays(1)
        }

        val n = dates.size
        if (n == 0) {
            throw IllegalStateException("Calculated date range is empty")
        }

        val base = amount / n
        val remainder = (amount % n).toInt()

        val entries = mutableListOf<AllocationEntry>()
        for (i in 0 until n) {
            var currentAmount = base
            if (i < remainder) {
                currentAmount += 1
            }
            if (currentAmount > 0) {
                entries.add(AllocationEntry(dates[i], currentAmount))
            }
        }

        return entries
    }
}
