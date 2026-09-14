package com.ivy.domain.usecase.budget

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

sealed interface BudgetPeriodType {
    data object Weekly : BudgetPeriodType
    data object Monthly : BudgetPeriodType
    data class Salary(val payday: Int) : BudgetPeriodType
    data class Custom(val start: LocalDate, val end: LocalDate) : BudgetPeriodType
}

data class BudgetPeriod(
    val start: LocalDate,
    val end: LocalDate,
    val type: BudgetPeriodType
) {
    fun daysRemaining(today: LocalDate): Int {
        if (today.isAfter(end)) {
            return 0
        }
        val fromDate = if (today.isBefore(start)) start else today
        return (ChronoUnit.DAYS.between(fromDate, end) + 1).toInt()
    }
}

class BudgetPeriodResolver @Inject constructor() {

    fun resolve(type: BudgetPeriodType, today: LocalDate): BudgetPeriod {
        return when (type) {
            is BudgetPeriodType.Weekly -> {
                val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val end = start.plusDays(6)
                BudgetPeriod(start, end, type)
            }

            is BudgetPeriodType.Monthly -> {
                val start = today.with(TemporalAdjusters.firstDayOfMonth())
                val end = today.with(TemporalAdjusters.lastDayOfMonth())
                BudgetPeriod(start, end, type)
            }

            is BudgetPeriodType.Salary -> {
                if (type.payday !in 1..31) {
                    throw IllegalArgumentException("Payday must be between 1 and 31")
                }

                val lastDayCurrent = today.with(TemporalAdjusters.lastDayOfMonth())
                val targetDayCurrent = if (type.payday > lastDayCurrent.dayOfMonth) lastDayCurrent.dayOfMonth else type.payday
                val effectivePaydayCurrent = today.withDayOfMonth(targetDayCurrent)

                if (today >= effectivePaydayCurrent) {
                    val start = effectivePaydayCurrent
                    val nextMonth = today.plusMonths(1)
                    val lastDayNext = nextMonth.with(TemporalAdjusters.lastDayOfMonth())
                    val targetDayNext = if (type.payday > lastDayNext.dayOfMonth) lastDayNext.dayOfMonth else type.payday
                    val nextEffectivePayday = nextMonth.withDayOfMonth(targetDayNext)
                    val end = nextEffectivePayday.minusDays(1)
                    BudgetPeriod(start, end, type)
                } else {
                    val end = effectivePaydayCurrent.minusDays(1)
                    val prevMonth = today.minusMonths(1)
                    val lastDayPrev = prevMonth.with(TemporalAdjusters.lastDayOfMonth())
                    val targetDayPrev = if (type.payday > lastDayPrev.dayOfMonth) lastDayPrev.dayOfMonth else type.payday
                    val start = prevMonth.withDayOfMonth(targetDayPrev)
                    BudgetPeriod(start, end, type)
                }
            }

            is BudgetPeriodType.Custom -> {
                if (type.end.isBefore(type.start)) {
                    throw IllegalArgumentException("End date cannot be before start date")
                }
                BudgetPeriod(type.start, type.end, type)
            }
        }
    }
}
