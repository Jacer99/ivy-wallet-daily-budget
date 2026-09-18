package com.ivy.home

import com.ivy.domain.usecase.budget.BudgetPeriodType
import com.ivy.domain.usecase.budget.BudgetSnapshot
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.time.LocalDate
import org.junit.Test

class SafeToSpendMapperTest {

    private val anyDate = LocalDate.of(2026, 9, 16)

    private fun snapshot(
        capacity: Long,
        periodSpentRaw: Long,
        periodReserved: Long,
        periodRemaining: Long,
        daysRemaining: Int,
        openingAllowance: Long,
        todayCharges: Long,
        remainingAllowance: Long,
        tomorrowProjection: Long?,
    ) = BudgetSnapshot(
        periodStart = anyDate,
        periodEnd = anyDate.plusDays(6),
        capacity = capacity,
        periodSpentRaw = periodSpentRaw,
        periodReserved = periodReserved,
        periodRemaining = periodRemaining,
        daysRemaining = daysRemaining,
        openingAllowance = openingAllowance,
        todayCharges = todayCharges,
        remainingAllowance = remainingAllowance,
        tomorrowProjection = tomorrowProjection,
    )

    @Test
    fun `no budget maps to NoBudget`() {
        val result = mapToSafeToSpendCardState(
            hasBudget = false,
            snapshot = snapshot(0L, 0L, 0L, 0L, 7, 0L, 0L, 0L, null),
        )
        result shouldBe SafeToSpendCardState.NoBudget
    }

    @Test
    fun `positive remaining maps to Active with no message`() {
        val result = mapToSafeToSpendCardState(
            hasBudget = true,
            snapshot = snapshot(
                capacity = 450_000L,
                periodSpentRaw = 263_000L,
                periodReserved = 0L,
                periodRemaining = 187_000L,
                daysRemaining = 7,
                openingAllowance = 80_400L,
                todayCharges = 23_000L,
                remainingAllowance = 57_400L,
                tomorrowProjection = 62_000L
            ),
        ).shouldBeInstanceOf<SafeToSpendCardState.Active>()

        result.remainingAllowanceMinorUnits shouldBe 57_400L
        result.openingAllowanceMinorUnits shouldBe 80_400L
        result.todayChargesMinorUnits shouldBe 23_000L
        result.tomorrowProjectionMinorUnits shouldBe 62_000L
        result.message shouldBe null
    }

    @Test
    fun `negative remaining clamps hero to zero and emits OverToday`() {
        val result = mapToSafeToSpendCardState(
            hasBudget = true,
            snapshot = snapshot(
                capacity = 450_000L,
                periodSpentRaw = 263_000L,
                periodReserved = 0L,
                periodRemaining = 187_000L,
                daysRemaining = 7,
                openingAllowance = 80_400L,
                todayCharges = 95_000L,
                remainingAllowance = -14_600L,
                tomorrowProjection = 45_000L
            ),
        ).shouldBeInstanceOf<SafeToSpendCardState.Active>()

        result.remainingAllowanceMinorUnits shouldBe 0L
        val message = result.message.shouldBeInstanceOf<SafeToSpendMessage.OverToday>()
        message.overByMinorUnits shouldBe 14_600L
    }

    @Test
    fun `zero remaining with no period money maps to PeriodExhausted`() {
        val result = mapToSafeToSpendCardState(
            hasBudget = true,
            snapshot = snapshot(
                capacity = 450_000L,
                periodSpentRaw = 450_000L,
                periodReserved = 0L,
                periodRemaining = 0L,
                daysRemaining = 7,
                openingAllowance = 0L,
                todayCharges = 0L,
                remainingAllowance = 0L,
                tomorrowProjection = 0L
            ),
        ).shouldBeInstanceOf<SafeToSpendCardState.Active>()

        result.remainingAllowanceMinorUnits shouldBe 0L
        result.message shouldBe SafeToSpendMessage.PeriodExhausted
    }

    @Test
    fun `null tomorrow is passed through untouched`() {
        val result = mapToSafeToSpendCardState(
            hasBudget = true,
            snapshot = snapshot(
                capacity = 450_000L,
                periodSpentRaw = 263_000L,
                periodReserved = 0L,
                periodRemaining = 187_000L,
                daysRemaining = 7,
                openingAllowance = 80_400L,
                todayCharges = 23_000L,
                remainingAllowance = 57_400L,
                tomorrowProjection = null
            ),
        ).shouldBeInstanceOf<SafeToSpendCardState.Active>()

        result.tomorrowProjectionMinorUnits shouldBe null
    }

    @Test
    fun `non-salary period maps paydayLabel to null`() {
        val result = mapToSafeToSpendCardState(
            hasBudget = true,
            snapshot = snapshot(
                capacity = 450_000L,
                periodSpentRaw = 0L,
                periodReserved = 0L,
                periodRemaining = 450_000L,
                daysRemaining = 7,
                openingAllowance = 80_400L,
                todayCharges = 0L,
                remainingAllowance = 80_400L,
                tomorrowProjection = 80_400L
            ),
            periodType = BudgetPeriodType.Monthly,
        ).shouldBeInstanceOf<SafeToSpendCardState.Active>()

        result.paydayLabel shouldBe null
    }

    @Test
    fun `salary period with today as payday maps paydayLabel to Payday today`() {
        val today = LocalDate.of(2026, 9, 25)
        val snapshot = BudgetSnapshot(
            periodStart = LocalDate.of(2026, 9, 25),
            periodEnd = LocalDate.of(2026, 10, 24),
            capacity = 450_000L,
            periodSpentRaw = 0L,
            periodReserved = 0L,
            periodRemaining = 450_000L,
            daysRemaining = 30,
            openingAllowance = 15_000L,
            todayCharges = 0L,
            remainingAllowance = 15_000L,
            tomorrowProjection = 15_000L,
        )

        val result = mapToSafeToSpendCardState(
            hasBudget = true,
            snapshot = snapshot,
            periodType = BudgetPeriodType.Salary(payday = 25),
            today = today,
        ).shouldBeInstanceOf<SafeToSpendCardState.Active>()

        result.paydayLabel shouldBe "Payday today"
    }

    @Test
    fun `salary period with 1 day until payday maps paydayLabel to 1 day`() {
        val today = LocalDate.of(2026, 10, 24)
        val snapshot = BudgetSnapshot(
            periodStart = LocalDate.of(2026, 9, 25),
            periodEnd = LocalDate.of(2026, 10, 24),
            capacity = 450_000L,
            periodSpentRaw = 400_000L,
            periodReserved = 0L,
            periodRemaining = 50_000L,
            daysRemaining = 1,
            openingAllowance = 50_000L,
            todayCharges = 0L,
            remainingAllowance = 50_000L,
            tomorrowProjection = null,
        )

        val result = mapToSafeToSpendCardState(
            hasBudget = true,
            snapshot = snapshot,
            periodType = BudgetPeriodType.Salary(payday = 25),
            today = today,
        ).shouldBeInstanceOf<SafeToSpendCardState.Active>()

        result.paydayLabel shouldBe "Left until payday · 1 day"
    }

    @Test
    fun `salary period with multiple days until payday maps paydayLabel`() {
        val today = LocalDate.of(2026, 10, 20)
        val snapshot = BudgetSnapshot(
            periodStart = LocalDate.of(2026, 9, 25),
            periodEnd = LocalDate.of(2026, 10, 24),
            capacity = 450_000L,
            periodSpentRaw = 300_000L,
            periodReserved = 0L,
            periodRemaining = 150_000L,
            daysRemaining = 5,
            openingAllowance = 30_000L,
            todayCharges = 0L,
            remainingAllowance = 30_000L,
            tomorrowProjection = 30_000L,
        )

        val result = mapToSafeToSpendCardState(
            hasBudget = true,
            snapshot = snapshot,
            periodType = BudgetPeriodType.Salary(payday = 25),
            today = today,
        ).shouldBeInstanceOf<SafeToSpendCardState.Active>()

        result.paydayLabel shouldBe "Left until payday · 5 days"
    }

    @Test
    fun `period summary fields are mapped from snapshot`() {
        val result = mapToSafeToSpendCardState(
            hasBudget = true,
            snapshot = snapshot(
                capacity = 450_000L,
                periodSpentRaw = 163_000L,
                periodReserved = 0L,
                periodRemaining = 187_000L,
                daysRemaining = 5,
                openingAllowance = 80_400L,
                todayCharges = 23_000L,
                remainingAllowance = 57_400L,
                tomorrowProjection = 62_000L,
            ),
        ).shouldBeInstanceOf<SafeToSpendCardState.Active>()

        result.periodBudgetMinorUnits shouldBe 450_000L
        result.periodSpentMinorUnits shouldBe 163_000L
        result.periodAvailableMinorUnits shouldBe 187_000L
        result.daysLeft shouldBe 5
    }

    @Test
    fun `negative period remaining clamps available to zero`() {
        val result = mapToSafeToSpendCardState(
            hasBudget = true,
            snapshot = snapshot(
                capacity = 450_000L,
                periodSpentRaw = 500_000L,
                periodReserved = 0L,
                periodRemaining = -50_000L,
                daysRemaining = 3,
                openingAllowance = 0L,
                todayCharges = 50_000L,
                remainingAllowance = -50_000L,
                tomorrowProjection = 0L,
            ),
        ).shouldBeInstanceOf<SafeToSpendCardState.Active>()

        result.periodBudgetMinorUnits shouldBe 450_000L
        result.periodSpentMinorUnits shouldBe 500_000L
        result.periodAvailableMinorUnits shouldBe 0L
        result.daysLeft shouldBe 3
    }
}
