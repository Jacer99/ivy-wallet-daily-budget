package com.ivy.home

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
}
