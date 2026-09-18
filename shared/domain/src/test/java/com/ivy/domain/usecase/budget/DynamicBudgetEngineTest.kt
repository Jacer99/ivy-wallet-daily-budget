package com.ivy.domain.usecase.budget

import com.ivy.data.model.AllocationMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(TestParameterInjector::class)
class DynamicBudgetEngineTest {

    @Test
    fun `basic weekly period with no historical spending`() {
        val scheduler = ExpenseAllocationScheduler()
        val engine = DynamicBudgetEngine(scheduler)
        val period = BudgetPeriod(LocalDate.parse("2026-09-14"), LocalDate.parse("2026-09-20"), BudgetPeriodType.Weekly)
        
        val input = BudgetEngineInput(
            period = period,
            budgetLimit = 70000L,
            includedIncome = 0L,
            reservationsTotal = 0L,
            expenses = emptyList(),
            today = LocalDate.parse("2026-09-14")
        )

        val snapshot = engine.calculate(input)

        snapshot shouldBe BudgetSnapshot(
            periodStart = LocalDate.parse("2026-09-14"),
            periodEnd = LocalDate.parse("2026-09-20"),
            capacity = 70000L,
            periodSpentRaw = 0L,
            periodReserved = 0L,
            periodRemaining = 70000L,
            daysRemaining = 7,
            openingAllowance = 10000L,
            todayCharges = 0L,
            remainingAllowance = 10000L,
            tomorrowProjection = 10000L
        )
    }

    @Test
    fun `same day TODAY mode expense reduces remaining allowance but preserves opening`() {
        val scheduler = ExpenseAllocationScheduler()
        val engine = DynamicBudgetEngine(scheduler)
        val period = BudgetPeriod(LocalDate.parse("2026-09-14"), LocalDate.parse("2026-09-20"), BudgetPeriodType.Weekly)
        
        val input = BudgetEngineInput(
            period = period,
            budgetLimit = 70000L,
            includedIncome = 0L,
            reservationsTotal = 0L,
            expenses = listOf(
                BudgetExpenseInput(LocalDate.parse("2026-09-16"), 2000L, AllocationMode.TODAY)
            ),
            today = LocalDate.parse("2026-09-16")
        )

        val snapshot = engine.calculate(input)

        snapshot.daysRemaining shouldBe 5
        snapshot.openingAllowance shouldBe 14000L
        snapshot.todayCharges shouldBe 2000L
        snapshot.remainingAllowance shouldBe 12000L
    }

    @Test
    fun `WEEK mode expense spreads allocation properly across remaining period`() {
        val scheduler = ExpenseAllocationScheduler()
        val engine = DynamicBudgetEngine(scheduler)
        val period = BudgetPeriod(LocalDate.parse("2026-09-14"), LocalDate.parse("2026-09-20"), BudgetPeriodType.Weekly)
        
        val input = BudgetEngineInput(
            period = period,
            budgetLimit = 70000L,
            includedIncome = 0L,
            reservationsTotal = 0L,
            expenses = listOf(
                BudgetExpenseInput(LocalDate.parse("2026-09-16"), 5000L, AllocationMode.WEEK)
            ),
            today = LocalDate.parse("2026-09-16")
        )

        val snapshot = engine.calculate(input)

        snapshot.daysRemaining shouldBe 5
        snapshot.openingAllowance shouldBe 14000L
        snapshot.todayCharges shouldBe 1000L
        snapshot.remainingAllowance shouldBe 13000L
        snapshot.periodSpentRaw shouldBe 5000L
        snapshot.periodRemaining shouldBe 65000L
    }

    @Test
    fun `tomorrow projection reflects downstream impacts of future allocations`() {
        val scheduler = ExpenseAllocationScheduler()
        val engine = DynamicBudgetEngine(scheduler)
        val period = BudgetPeriod(LocalDate.parse("2026-09-14"), LocalDate.parse("2026-09-20"), BudgetPeriodType.Weekly)
        
        val input = BudgetEngineInput(
            period = period,
            budgetLimit = 70000L,
            includedIncome = 0L,
            reservationsTotal = 0L,
            expenses = listOf(
                BudgetExpenseInput(LocalDate.parse("2026-09-14"), 7000L, AllocationMode.WEEK)
            ),
            today = LocalDate.parse("2026-09-16")
        )

        val snapshot = engine.calculate(input)

        snapshot.daysRemaining shouldBe 5
        snapshot.openingAllowance shouldBe 13600L
        snapshot.todayCharges shouldBe 1000L
        snapshot.remainingAllowance shouldBe 12600L
        snapshot.tomorrowProjection shouldBe 12600L
    }

    @Test
    fun `active reservations reduce standard period remaining and daily opening pool`() {
        val scheduler = ExpenseAllocationScheduler()
        val engine = DynamicBudgetEngine(scheduler)
        val period = BudgetPeriod(LocalDate.parse("2026-09-14"), LocalDate.parse("2026-09-20"), BudgetPeriodType.Weekly)
        
        val input = BudgetEngineInput(
            period = period,
            budgetLimit = 45000L,
            includedIncome = 0L,
            reservationsTotal = 10000L,
            expenses = emptyList(),
            today = LocalDate.parse("2026-09-16")
        )

        val snapshot = engine.calculate(input)

        snapshot.periodRemaining shouldBe 35000L
        snapshot.daysRemaining shouldBe 5
        snapshot.openingAllowance shouldBe 7000L
        snapshot.todayCharges shouldBe 0L
        snapshot.remainingAllowance shouldBe 7000L
        snapshot.tomorrowProjection shouldBe 7000L
    }

    @Test
    fun `extreme overspending conditions produce exact negative values without filtering`() {
        val scheduler = ExpenseAllocationScheduler()
        val engine = DynamicBudgetEngine(scheduler)
        val period = BudgetPeriod(LocalDate.parse("2026-09-14"), LocalDate.parse("2026-09-20"), BudgetPeriodType.Weekly)
        
        val input = BudgetEngineInput(
            period = period,
            budgetLimit = 7000L,
            includedIncome = 0L,
            reservationsTotal = 0L,
            expenses = listOf(
                BudgetExpenseInput(LocalDate.parse("2026-09-16"), 20000L, AllocationMode.TODAY)
            ),
            today = LocalDate.parse("2026-09-16")
        )

        val snapshot = engine.calculate(input)

        snapshot.openingAllowance shouldBe 1400L
        snapshot.todayCharges shouldBe 20000L
        snapshot.remainingAllowance shouldBe -18600L
        snapshot.tomorrowProjection shouldBe -3250L
    }

    @Test
    fun `last calendar day of active period returns null tomorrow projection`() {
        val scheduler = ExpenseAllocationScheduler()
        val engine = DynamicBudgetEngine(scheduler)
        val period = BudgetPeriod(LocalDate.parse("2026-09-14"), LocalDate.parse("2026-09-20"), BudgetPeriodType.Weekly)
        
        val input = BudgetEngineInput(
            period = period,
            budgetLimit = 70000L,
            includedIncome = 0L,
            reservationsTotal = 0L,
            expenses = emptyList(),
            today = LocalDate.parse("2026-09-20")
        )

        val snapshot = engine.calculate(input)

        snapshot.daysRemaining shouldBe 1
        snapshot.openingAllowance shouldBe 70000L
        snapshot.todayCharges shouldBe 0L
        snapshot.remainingAllowance shouldBe 70000L
        snapshot.tomorrowProjection shouldBe null
    }

    @Test
    fun `historical expenses residing outside period boundaries are completely ignored`() {
        val scheduler = ExpenseAllocationScheduler()
        val engine = DynamicBudgetEngine(scheduler)
        val period = BudgetPeriod(LocalDate.parse("2026-09-14"), LocalDate.parse("2026-09-20"), BudgetPeriodType.Weekly)
        
        val input = BudgetEngineInput(
            period = period,
            budgetLimit = 70000L,
            includedIncome = 0L,
            reservationsTotal = 0L,
            expenses = listOf(
                BudgetExpenseInput(LocalDate.parse("2026-09-13"), 5000L, AllocationMode.TODAY),
                BudgetExpenseInput(LocalDate.parse("2026-09-16"), 1000L, AllocationMode.TODAY)
            ),
            today = LocalDate.parse("2026-09-16")
        )

        val snapshot = engine.calculate(input)

        snapshot.periodSpentRaw shouldBe 1000L
        snapshot.periodRemaining shouldBe 69000L
        snapshot.todayCharges shouldBe 1000L
        snapshot.openingAllowance shouldBe 14000L
        snapshot.remainingAllowance shouldBe 13000L
    }

    @Test
    fun `explicitly included income streams scale overall budget capacity metrics`() {
        val scheduler = ExpenseAllocationScheduler()
        val engine = DynamicBudgetEngine(scheduler)
        val period = BudgetPeriod(LocalDate.parse("2026-09-14"), LocalDate.parse("2026-09-20"), BudgetPeriodType.Weekly)
        
        val input = BudgetEngineInput(
            period = period,
            budgetLimit = 70000L,
            includedIncome = 30000L,
            reservationsTotal = 0L,
            expenses = emptyList(),
            today = LocalDate.parse("2026-09-14")
        )

        val snapshot = engine.calculate(input)

        snapshot.capacity shouldBe 100000L
        snapshot.daysRemaining shouldBe 7
        snapshot.openingAllowance shouldBe 14285L
        snapshot.tomorrowProjection shouldBe 14285L
        snapshot.periodRemaining shouldBe 100000L
    }

    @Test
    fun `MONTH allocation pattern bounds correctly within weekly period endpoints`() {
        val scheduler = ExpenseAllocationScheduler()
        val engine = DynamicBudgetEngine(scheduler)
        val period = BudgetPeriod(LocalDate.parse("2026-09-14"), LocalDate.parse("2026-09-20"), BudgetPeriodType.Weekly)
        
        val input = BudgetEngineInput(
            period = period,
            budgetLimit = 100000L,
            includedIncome = 0L,
            reservationsTotal = 0L,
            expenses = listOf(
                BudgetExpenseInput(LocalDate.parse("2026-09-19"), 3000L, AllocationMode.MONTH)
            ),
            today = LocalDate.parse("2026-09-19")
        )

        val snapshot = engine.calculate(input)

        snapshot.daysRemaining shouldBe 2
        snapshot.capacity shouldBe 100000L
        snapshot.openingAllowance shouldBe 50000L
        snapshot.todayCharges shouldBe 1500L
        snapshot.remainingAllowance shouldBe 48500L
        snapshot.tomorrowProjection shouldBe 48500L
    }

    @Test
    fun `tomorrow projection assumes today remaining safe allowance is fully utilized`() {
        val scheduler = ExpenseAllocationScheduler()
        val engine = DynamicBudgetEngine(scheduler)
        val period = BudgetPeriod(LocalDate.parse("2026-09-17"), LocalDate.parse("2026-09-20"), BudgetPeriodType.Weekly)

        val input = BudgetEngineInput(
            period = period,
            budgetLimit = 500000L,
            includedIncome = 0L,
            reservationsTotal = 0L,
            expenses = listOf(
                BudgetExpenseInput(LocalDate.parse("2026-09-17"), 200000L, AllocationMode.WEEK)
            ),
            today = LocalDate.parse("2026-09-17")
        )

        val snapshot = engine.calculate(input)

        snapshot.openingAllowance shouldBe 125000L
        snapshot.todayCharges shouldBe 50000L
        snapshot.remainingAllowance shouldBe 75000L
        snapshot.tomorrowProjection shouldBe 75000L
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws exception if budget limit is negative`() {
        val scheduler = ExpenseAllocationScheduler()
        val period = BudgetPeriod(LocalDate.parse("2026-09-14"), LocalDate.parse("2026-09-20"), BudgetPeriodType.Weekly)
        val input = BudgetEngineInput(period, -100L, 0L, 0L, emptyList(), LocalDate.parse("2026-09-14"))
        DynamicBudgetEngine(scheduler).calculate(input)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws exception if included income is negative`() {
        val scheduler = ExpenseAllocationScheduler()
        val period = BudgetPeriod(LocalDate.parse("2026-09-14"), LocalDate.parse("2026-09-20"), BudgetPeriodType.Weekly)
        val input = BudgetEngineInput(period, 1000L, -50L, 0L, emptyList(), LocalDate.parse("2026-09-14"))
        DynamicBudgetEngine(scheduler).calculate(input)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws exception if reservations total is negative`() {
        val scheduler = ExpenseAllocationScheduler()
        val period = BudgetPeriod(LocalDate.parse("2026-09-14"), LocalDate.parse("2026-09-20"), BudgetPeriodType.Weekly)
        val input = BudgetEngineInput(period, 1000L, 0L, -200L, emptyList(), LocalDate.parse("2026-09-14"))
        DynamicBudgetEngine(scheduler).calculate(input)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws exception if any individual expense amount is non-positive`() {
        val scheduler = ExpenseAllocationScheduler()
        val period = BudgetPeriod(LocalDate.parse("2026-09-14"), LocalDate.parse("2026-09-20"), BudgetPeriodType.Weekly)
        val input = BudgetEngineInput(
            period = period,
            budgetLimit = 1000L,
            includedIncome = 0L,
            reservationsTotal = 0L,
            expenses = listOf(BudgetExpenseInput(LocalDate.parse("2026-09-15"), 0L, AllocationMode.TODAY)),
            today = LocalDate.parse("2026-09-14")
        )
        DynamicBudgetEngine(scheduler).calculate(input)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws exception if checking calendar date after period end`() {
        val scheduler = ExpenseAllocationScheduler()
        val period = BudgetPeriod(LocalDate.parse("2026-09-14"), LocalDate.parse("2026-09-20"), BudgetPeriodType.Weekly)
        val input = BudgetEngineInput(period, 1000L, 0L, 0L, emptyList(), LocalDate.parse("2026-09-25"))
        DynamicBudgetEngine(scheduler).calculate(input)
    }
}
