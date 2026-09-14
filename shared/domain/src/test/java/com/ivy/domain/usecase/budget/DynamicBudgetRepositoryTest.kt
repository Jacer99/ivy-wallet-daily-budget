package com.ivy.domain.usecase.budget

import com.ivy.data.model.AccountId
import com.ivy.data.model.CategoryId
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.PositiveValue
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionId
import com.ivy.data.model.TransactionMetadata
import com.ivy.data.model.Transfer
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.data.model.primitive.PositiveDouble
import com.ivy.data.repository.TransactionRepository
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class DynamicBudgetRepositoryTest {

    private val zone = ZoneId.of("Africa/Tunis")
    private val resolver = BudgetPeriodResolver()
    private val scheduler = ExpenseAllocationScheduler()
    private val engine = DynamicBudgetEngine(scheduler)
    private val mapper = DynamicBudgetMapper(resolver)
    private val transactionRepo = mockk<TransactionRepository>()
    private val repository = DynamicBudgetRepository(
        transactionRepository = transactionRepo,
        resolver = resolver,
        mapper = mapper,
        engine = engine,
    )

    private fun atNoon(date: LocalDate): Instant =
        date.atTime(12, 0).atZone(zone).toInstant()

    private fun expense(
        time: Instant,
        amount: Double,
        account: AccountId = AccountId(UUID.randomUUID()),
        category: CategoryId? = null,
        asset: String = "TND",
        settled: Boolean = true,
    ): Expense = Expense(
        id = TransactionId(UUID.randomUUID()),
        title = NotBlankTrimmedString.unsafe("Test"),
        description = null,
        category = category,
        time = time,
        settled = settled,
        metadata = TransactionMetadata(
            recurringRuleId = null,
            paidForDateTime = null,
            loanId = null,
            loanRecordId = null,
        ),
        tags = emptyList(),
        value = PositiveValue(
            amount = PositiveDouble.unsafe(amount),
            asset = AssetCode.unsafe(asset),
        ),
        account = account,
    )

    @Test
    fun `returns empty-expense snapshot when findAllBetween returns emptyList`() = runTest {
        // given
        val config = DynamicBudgetConfig.default().copy(budgetLimitMinorUnits = 700_000L)
        val today = LocalDate.of(2026, 9, 14) // Mon
        coEvery { transactionRepo.findAllBetween(any(), any()) } returns emptyList()

        // when
        val snapshot = repository.calculate(config, today, zone)

        // then
        snapshot shouldBe BudgetSnapshot(
            periodStart = LocalDate.of(2026, 9, 14),
            periodEnd = LocalDate.of(2026, 9, 20),
            capacity = 700_000,
            periodSpentRaw = 0,
            periodReserved = 0,
            periodRemaining = 700_000,
            daysRemaining = 7,
            openingAllowance = 100_000,
            todayCharges = 0,
            remainingAllowance = 100_000,
            tomorrowProjection = 116_666
        )
    }

    @Test
    fun `reads transactions and returns the correct snapshot`() = runTest {
        // given
        val config = DynamicBudgetConfig.default().copy(budgetLimitMinorUnits = 700_000L)
        val today = LocalDate.of(2026, 9, 16) // Wed
        coEvery { transactionRepo.findAllBetween(any(), any()) } returns listOf(
            expense(atNoon(LocalDate.of(2026, 9, 14)), 100.00),
            expense(atNoon(LocalDate.of(2026, 9, 15)), 80.00),
            expense(atNoon(LocalDate.of(2026, 9, 16)), 50.00),
        )

        // when
        val snapshot = repository.calculate(config, today, zone)

        // then
        snapshot shouldBe BudgetSnapshot(
            periodStart = LocalDate.of(2026, 9, 14),
            periodEnd = LocalDate.of(2026, 9, 20),
            capacity = 700_000,
            periodSpentRaw = 230_000,
            periodReserved = 0,
            periodRemaining = 470_000,
            daysRemaining = 5,
            openingAllowance = 104_000,
            todayCharges = 50_000,
            remainingAllowance = 54_000,
            tomorrowProjection = 117_500
        )
    }

    @Test
    fun `passes the correct Instant range to the repository`() = runTest {
        // given
        val config = DynamicBudgetConfig.default()
        val today = LocalDate.of(2026, 9, 16) // Wed
        val startSlot = slot<Instant>()
        val endSlot = slot<Instant>()
        coEvery { transactionRepo.findAllBetween(capture(startSlot), capture(endSlot)) } returns emptyList()

        // when
        repository.calculate(config, today, zone)

        // then
        val expectedStart = LocalDate.of(2026, 9, 14).atStartOfDay(zone).toInstant()
        val expectedEnd = LocalDate.of(2026, 9, 20)
            .atTime(23, 59, 59, 999_999_999)
            .atZone(zone).toInstant()

        startSlot.captured shouldBe expectedStart
        endSlot.captured shouldBe expectedEnd
    }

    @Test
    fun `monthly config queries the monthly period`() = runTest {
        // given
        val config = DynamicBudgetConfig.default().copy(periodType = BudgetPeriodType.Monthly)
        val today = LocalDate.of(2026, 9, 16)
        val startSlot = slot<Instant>()
        val endSlot = slot<Instant>()
        coEvery { transactionRepo.findAllBetween(capture(startSlot), capture(endSlot)) } returns emptyList()

        // when
        repository.calculate(config, today, zone)

        // then
        val expectedStart = LocalDate.of(2026, 9, 1).atStartOfDay(zone).toInstant()
        val expectedEnd = LocalDate.of(2026, 9, 30)
            .atTime(23, 59, 59, 999_999_999)
            .atZone(zone).toInstant()

        startSlot.captured shouldBe expectedStart
        endSlot.captured shouldBe expectedEnd
    }

    @Test
    fun `income and transfer in the returned list are ignored`() = runTest {
        // given
        val config = DynamicBudgetConfig.default().copy(budgetLimitMinorUnits = 700_000L)
        val today = LocalDate.of(2026, 9, 16) // Wed
        coEvery { transactionRepo.findAllBetween(any(), any()) } returns listOf(
            expense(atNoon(LocalDate.of(2026, 9, 14)), 100.00),
            expense(atNoon(LocalDate.of(2026, 9, 15)), 80.00),
            expense(atNoon(LocalDate.of(2026, 9, 16)), 50.00),
            Income(
                id = TransactionId(UUID.randomUUID()),
                title = null,
                description = null,
                category = null,
                time = atNoon(LocalDate.of(2026, 9, 14)),
                settled = true,
                metadata = TransactionMetadata(null, null, null, null),
                tags = emptyList(),
                value = PositiveValue(PositiveDouble.unsafe(500.0), AssetCode.unsafe("TND")),
                account = AccountId(UUID.randomUUID())
            ),
            Transfer(
                id = TransactionId(UUID.randomUUID()),
                title = null,
                description = null,
                category = null,
                time = atNoon(LocalDate.of(2026, 9, 14)),
                settled = true,
                metadata = TransactionMetadata(null, null, null, null),
                tags = emptyList(),
                fromAccount = AccountId(UUID.randomUUID()),
                fromValue = PositiveValue(PositiveDouble.unsafe(300.0), AssetCode.unsafe("TND")),
                toAccount = AccountId(UUID.randomUUID()),
                toValue = PositiveValue(PositiveDouble.unsafe(300.0), AssetCode.unsafe("TND"))
            )
        )

        // when
        val snapshot = repository.calculate(config, today, zone)

        // then
        snapshot.periodSpentRaw shouldBe 230_000
    }

    @Test
    fun `unsettled expense excluded`() = runTest {
        // given
        val config = DynamicBudgetConfig.default().copy(budgetLimitMinorUnits = 700_000L)
        val today = LocalDate.of(2026, 9, 16) // Wed
        coEvery { transactionRepo.findAllBetween(any(), any()) } returns listOf(
            expense(atNoon(LocalDate.of(2026, 9, 14)), 100.00),
            expense(atNoon(LocalDate.of(2026, 9, 15)), 80.00, settled = false),
            expense(atNoon(LocalDate.of(2026, 9, 16)), 50.00),
        )

        // when
        val snapshot = repository.calculate(config, today, zone)

        // then
        snapshot.periodSpentRaw shouldBe 150_000
    }

    @Test
    fun `config with non-TND currency filters out TND expenses`() = runTest {
        // given
        val config = DynamicBudgetConfig.default().copy(currencyCode = "USD")
        val today = LocalDate.of(2026, 9, 16)
        coEvery { transactionRepo.findAllBetween(any(), any()) } returns listOf(
            expense(atNoon(LocalDate.of(2026, 9, 14)), 100.00, asset = "TND"),
        )

        // when
        val snapshot = repository.calculate(config, today, zone)

        // then
        snapshot.periodSpentRaw shouldBe 0
    }
}
