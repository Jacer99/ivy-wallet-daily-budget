package com.ivy.domain.usecase.budget

import com.ivy.data.model.AllocationMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(TestParameterInjector::class)
class ExpenseAllocationSchedulerTest {

    @Test
    fun `TODAY mode schedules full amount on transaction date`() {
        val scheduler = ExpenseAllocationScheduler()
        val result = scheduler.schedule(
            amount = 9000L,
            transactionDate = LocalDate.parse("2026-09-18"),
            mode = AllocationMode.TODAY,
            periodEnd = LocalDate.parse("2026-09-20")
        )
        result shouldBe listOf(
            AllocationEntry(LocalDate.parse("2026-09-18"), 9000L)
        )
    }

    @Test
    fun `WEEK mode splits evenly on Friday normal week`() {
        val scheduler = ExpenseAllocationScheduler()
        val result = scheduler.schedule(
            amount = 9000L,
            transactionDate = LocalDate.parse("2026-09-18"), // Friday
            mode = AllocationMode.WEEK,
            periodEnd = LocalDate.parse("2026-09-20")   // Sunday
        )
        result shouldBe listOf(
            AllocationEntry(LocalDate.parse("2026-09-18"), 3000L),
            AllocationEntry(LocalDate.parse("2026-09-19"), 3000L),
            AllocationEntry(LocalDate.parse("2026-09-20"), 3000L)
        )
    }

    @Test
    fun `WEEK mode handles non-divisible splits with remainder increments`() {
        val scheduler = ExpenseAllocationScheduler()
        val result = scheduler.schedule(
            amount = 10000L,
            transactionDate = LocalDate.parse("2026-09-18"),
            mode = AllocationMode.WEEK,
            periodEnd = LocalDate.parse("2026-09-20")
        )
        result shouldBe listOf(
            AllocationEntry(LocalDate.parse("2026-09-18"), 3334L),
            AllocationEntry(LocalDate.parse("2026-09-19"), 3333L),
            AllocationEntry(LocalDate.parse("2026-09-20"), 3333L)
        )
    }

    @Test
    fun `WEEK mode clips split to periodEnd boundary`() {
        val scheduler = ExpenseAllocationScheduler()
        val result = scheduler.schedule(
            amount = 10000L,
            transactionDate = LocalDate.parse("2026-09-18"), // Friday
            mode = AllocationMode.WEEK,
            periodEnd = LocalDate.parse("2026-09-19")   // Saturday
        )
        result shouldBe listOf(
            AllocationEntry(LocalDate.parse("2026-09-18"), 5000L),
            AllocationEntry(LocalDate.parse("2026-09-19"), 5000L)
        )
    }

    @Test
    fun `WEEK mode functions as single day when transaction is on Sunday`() {
        val scheduler = ExpenseAllocationScheduler()
        val result = scheduler.schedule(
            amount = 9000L,
            transactionDate = LocalDate.parse("2026-09-20"), // Sunday
            mode = AllocationMode.WEEK,
            periodEnd = LocalDate.parse("2026-09-20")
        )
        result shouldBe listOf(
            AllocationEntry(LocalDate.parse("2026-09-20"), 9000L)
        )
    }

    @Test
    fun `MONTH mode splits evenly over month end range`() {
        val scheduler = ExpenseAllocationScheduler()
        val result = scheduler.schedule(
            amount = 10000L,
            transactionDate = LocalDate.parse("2026-09-28"),
            mode = AllocationMode.MONTH,
            periodEnd = LocalDate.parse("2026-09-30")
        )
        result shouldBe listOf(
            AllocationEntry(LocalDate.parse("2026-09-28"), 3334L),
            AllocationEntry(LocalDate.parse("2026-09-29"), 3333L),
            AllocationEntry(LocalDate.parse("2026-09-30"), 3333L)
        )
    }

    @Test
    fun `MONTH mode splits evenly over 4 day duration`() {
        val scheduler = ExpenseAllocationScheduler()
        val result = scheduler.schedule(
            amount = 10000L,
            transactionDate = LocalDate.parse("2026-01-28"),
            mode = AllocationMode.MONTH,
            periodEnd = LocalDate.parse("2026-01-31")
        )
        result shouldBe listOf(
            AllocationEntry(LocalDate.parse("2026-01-28"), 2500L),
            AllocationEntry(LocalDate.parse("2026-01-29"), 2500L),
            AllocationEntry(LocalDate.parse("2026-01-30"), 2500L),
            AllocationEntry(LocalDate.parse("2026-01-31"), 2500L)
        )
    }

    @Test
    fun `MONTH mode clips split when periodEnd arrives early`() {
        val scheduler = ExpenseAllocationScheduler()
        val result = scheduler.schedule(
            amount = 9000L,
            transactionDate = LocalDate.parse("2026-09-28"),
            mode = AllocationMode.MONTH,
            periodEnd = LocalDate.parse("2026-09-29")
        )
        result shouldBe listOf(
            AllocationEntry(LocalDate.parse("2026-09-28"), 4500L),
            AllocationEntry(LocalDate.parse("2026-09-29"), 4500L)
        )
    }

    @Test
    fun `MONTH mode handles transactions on the final calendar day`() {
        val scheduler = ExpenseAllocationScheduler()
        val result = scheduler.schedule(
            amount = 500L,
            transactionDate = LocalDate.parse("2026-09-30"),
            mode = AllocationMode.MONTH,
            periodEnd = LocalDate.parse("2026-09-30")
        )
        result shouldBe listOf(
            AllocationEntry(LocalDate.parse("2026-09-30"), 500L)
        )
    }

    @Test
    fun `omits days receiving zero when amount is smaller than day count`() {
        val scheduler = ExpenseAllocationScheduler()
        val result = scheduler.schedule(
            amount = 2L,
            transactionDate = LocalDate.parse("2026-09-14"), // Monday
            mode = AllocationMode.WEEK,
            periodEnd = LocalDate.parse("2026-09-18")       // Friday
        )
        result shouldBe listOf(
            AllocationEntry(LocalDate.parse("2026-09-14"), 1L),
            AllocationEntry(LocalDate.parse("2026-09-15"), 1L)
        )
    }

    @Test
    fun `covers all days with unit values when remainder matches perfectly`() {
        val scheduler = ExpenseAllocationScheduler()
        val result = scheduler.schedule(
            amount = 5L,
            transactionDate = LocalDate.parse("2026-09-14"),
            mode = AllocationMode.WEEK,
            periodEnd = LocalDate.parse("2026-09-18")
        )
        result shouldBe listOf(
            AllocationEntry(LocalDate.parse("2026-09-14"), 1L),
            AllocationEntry(LocalDate.parse("2026-09-15"), 1L),
            AllocationEntry(LocalDate.parse("2026-09-16"), 1L),
            AllocationEntry(LocalDate.parse("2026-09-17"), 1L),
            AllocationEntry(LocalDate.parse("2026-09-18"), 1L)
        )
    }

    @Test
    fun `handles single minor unit by placing on the first day`() {
        val scheduler = ExpenseAllocationScheduler()
        val result = scheduler.schedule(
            amount = 1L,
            transactionDate = LocalDate.parse("2026-09-14"),
            mode = AllocationMode.WEEK,
            periodEnd = LocalDate.parse("2026-09-18")
        )
        result shouldBe listOf(
            AllocationEntry(LocalDate.parse("2026-09-14"), 1L)
        )
    }

    @Test
    fun `TODAY mode ignores extended periodEnd layout`() {
        val scheduler = ExpenseAllocationScheduler()
        val result = scheduler.schedule(
            amount = 1L,
            transactionDate = LocalDate.parse("2026-09-14"),
            mode = AllocationMode.TODAY,
            periodEnd = LocalDate.parse("2026-09-18")
        )
        result shouldBe listOf(
            AllocationEntry(LocalDate.parse("2026-09-14"), 1L)
        )
    }

    enum class SumVerificationCase(
        val amount: Long,
        val dateStr: String,
        val mode: AllocationMode,
        val periodEndStr: String
    ) {
        C1(9000L, "2026-09-18", AllocationMode.TODAY, "2026-09-20"),
        C2(9000L, "2026-09-18", AllocationMode.WEEK, "2026-09-20"),
        C3(10000L, "2026-09-18", AllocationMode.WEEK, "2026-09-20"),
        C4(10000L, "2026-09-18", AllocationMode.WEEK, "2026-09-19"),
        C5(9000L, "2026-09-20", AllocationMode.WEEK, "2026-09-20"),
        C6(10000L, "2026-09-28", AllocationMode.MONTH, "2026-09-30"),
        C7(10000L, "2026-01-28", AllocationMode.MONTH, "2026-01-31"),
        C8(9000L, "2026-09-28", AllocationMode.MONTH, "2026-09-29"),
        C9(500L, "2026-09-30", AllocationMode.MONTH, "2026-09-30"),
        C10(2L, "2026-09-14", AllocationMode.WEEK, "2026-09-18"),
        C11(5L, "2026-09-14", AllocationMode.WEEK, "2026-09-18"),
        C12(1L, "2026-09-14", AllocationMode.WEEK, "2026-09-18")
    }

    @Test
    fun `invariant sweep - total allocation sum matches exactly`(@TestParameter testCase: SumVerificationCase) {
        val scheduler = ExpenseAllocationScheduler()
        val result = scheduler.schedule(
            amount = testCase.amount,
            transactionDate = LocalDate.parse(testCase.dateStr),
            mode = testCase.mode,
            periodEnd = LocalDate.parse(testCase.periodEndStr)
        )
        val calculatedSum = result.sumOf { it.amount }
        calculatedSum shouldBe testCase.amount
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws exception if amount is zero`() {
        ExpenseAllocationScheduler().schedule(
            amount = 0L,
            transactionDate = LocalDate.parse("2026-09-14"),
            mode = AllocationMode.TODAY,
            periodEnd = LocalDate.parse("2026-09-18")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws exception if amount is negative`() {
        ExpenseAllocationScheduler().schedule(
            amount = -500L,
            transactionDate = LocalDate.parse("2026-09-14"),
            mode = AllocationMode.TODAY,
            periodEnd = LocalDate.parse("2026-09-18")
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws exception if periodEnd is before transactionDate`() {
        ExpenseAllocationScheduler().schedule(
            amount = 1000L,
            transactionDate = LocalDate.parse("2026-09-18"),
            mode = AllocationMode.TODAY,
            periodEnd = LocalDate.parse("2026-09-14")
        )
    }
}
