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
import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class DynamicBudgetPipelineTest {

    private val zone = ZoneId.of("Africa/Tunis")
    private val resolver = BudgetPeriodResolver()
    private val scheduler = ExpenseAllocationScheduler()
    private val engine = DynamicBudgetEngine(scheduler)
    private val mapper = DynamicBudgetMapper(resolver)

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

    private fun runPipeline(
        transactions: List<Transaction>,
        config: DynamicBudgetConfig,
        today: LocalDate,
    ): BudgetSnapshot {
        val input = mapper.map(transactions, config, today, zone)
        return engine.calculate(input)
    }

    @Test
    fun `weekly budget mid-week with three TODAY expenses full snapshot`() {
        val config = DynamicBudgetConfig.default().copy(budgetLimitMinorUnits = 700_000L)
        val today = LocalDate.of(2026, 9, 16) // Wed
        val expenses = listOf(
            expense(atNoon(LocalDate.of(2026, 9, 14)), 100.00),
            expense(atNoon(LocalDate.of(2026, 9, 15)), 80.00),
            expense(atNoon(LocalDate.of(2026, 9, 16)), 50.00)
        )

        val snapshot = runPipeline(expenses, config, today)

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
    fun `weekly budget Monday no expenses full snapshot`() {
        val config = DynamicBudgetConfig.default().copy(budgetLimitMinorUnits = 700_000L)
        val today = LocalDate.of(2026, 9, 14) // Mon
        val expenses = emptyList<Transaction>()

        val snapshot = runPipeline(expenses, config, today)

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
    fun `weekly budget Monday two expenses on the same day`() {
        val config = DynamicBudgetConfig.default().copy(budgetLimitMinorUnits = 700_000L)
        val today = LocalDate.of(2026, 9, 14) // Mon
        val expenses = listOf(
            expense(atNoon(LocalDate.of(2026, 9, 14)), 100.00),
            expense(atNoon(LocalDate.of(2026, 9, 14)), 200.00)
        )

        val snapshot = runPipeline(expenses, config, today)

        snapshot.periodSpentRaw shouldBe 300_000
        snapshot.daysRemaining shouldBe 7
        snapshot.openingAllowance shouldBe 100_000
        snapshot.todayCharges shouldBe 300_000
        snapshot.remainingAllowance shouldBe -200_000
        snapshot.tomorrowProjection shouldBe 66_666
    }

    @Test
    fun `weekly budget overspend in a single day`() {
        val config = DynamicBudgetConfig.default().copy(budgetLimitMinorUnits = 100_000L)
        val today = LocalDate.of(2026, 9, 16) // Wed
        val expenses = listOf(
            expense(atNoon(LocalDate.of(2026, 9, 16)), 150.00)
        )

        val snapshot = runPipeline(expenses, config, today)

        snapshot.capacity shouldBe 100_000
        snapshot.periodSpentRaw shouldBe 150_000
        snapshot.periodRemaining shouldBe -50_000
        snapshot.daysRemaining shouldBe 5
        snapshot.openingAllowance shouldBe 20_000
        snapshot.todayCharges shouldBe 150_000
        snapshot.remainingAllowance shouldBe -130_000
        snapshot.tomorrowProjection shouldBe -12_500
    }

    @Test
    fun `wrong currency expenses are excluded from the pipeline`() {
        val config = DynamicBudgetConfig.default().copy(budgetLimitMinorUnits = 700_000L, currencyCode = "TND")
        val today = LocalDate.of(2026, 9, 14) // Mon
        val expenses = listOf(
            expense(atNoon(LocalDate.of(2026, 9, 14)), 100.00, asset = "TND"),
            expense(atNoon(LocalDate.of(2026, 9, 14)), 200.00, asset = "USD")
        )

        val snapshot = runPipeline(expenses, config, today)

        snapshot.periodSpentRaw shouldBe 100_000
        snapshot.todayCharges shouldBe 100_000
        snapshot.remainingAllowance shouldBe 0
    }

    @Test
    fun `mixed transaction types only expenses count`() {
        val config = DynamicBudgetConfig.default().copy(budgetLimitMinorUnits = 700_000L)
        val today = LocalDate.of(2026, 9, 14) // Mon

        val transactions = listOf(
            expense(atNoon(LocalDate.of(2026, 9, 14)), 100.00),
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

        val snapshot = runPipeline(transactions, config, today)

        snapshot.periodSpentRaw shouldBe 100_000
        snapshot.todayCharges shouldBe 100_000
        snapshot.remainingAllowance shouldBe 0
    }

    @Test
    fun `unsettled expense is excluded`() {
        val config = DynamicBudgetConfig.default().copy(budgetLimitMinorUnits = 700_000L)
        val today = LocalDate.of(2026, 9, 14) // Mon
        val expenses = listOf(
            expense(atNoon(LocalDate.of(2026, 9, 14)), 100.00, settled = false)
        )

        val snapshot = runPipeline(expenses, config, today)

        snapshot.periodSpentRaw shouldBe 0
        snapshot.todayCharges shouldBe 0
        snapshot.remainingAllowance shouldBe 100_000
    }

    @Test
    fun `last day of period has null tomorrow projection`() {
        val config = DynamicBudgetConfig.default().copy(budgetLimitMinorUnits = 700_000L)
        val today = LocalDate.of(2026, 9, 20) // Sun
        val expenses = emptyList<Transaction>()

        val snapshot = runPipeline(expenses, config, today)

        snapshot.daysRemaining shouldBe 1
        snapshot.openingAllowance shouldBe 700_000
        snapshot.todayCharges shouldBe 0
        snapshot.remainingAllowance shouldBe 700_000
        snapshot.tomorrowProjection shouldBe null
    }

    @Test
    fun `monthly budget period through the full pipeline`() {
        val config = DynamicBudgetConfig.default().copy(
            periodType = BudgetPeriodType.Monthly,
            budgetLimitMinorUnits = 3_100_000L
        )
        val today = LocalDate.of(2026, 9, 16) // Wed
        val expenses = listOf(
            expense(atNoon(LocalDate.of(2026, 9, 1)), 100.00),
            expense(atNoon(LocalDate.of(2026, 9, 15)), 200.00),
            expense(atNoon(LocalDate.of(2026, 9, 16)), 50.00)
        )

        val snapshot = runPipeline(expenses, config, today)

        snapshot.capacity shouldBe 3_100_000
        snapshot.periodSpentRaw shouldBe 350_000
        snapshot.daysRemaining shouldBe 15
        snapshot.openingAllowance shouldBe 186_666
        snapshot.todayCharges shouldBe 50_000
        snapshot.remainingAllowance shouldBe 136_666
    }

    @Test
    fun `salary budget period through the full pipeline`() {
        val config = DynamicBudgetConfig.default().copy(
            periodType = BudgetPeriodType.Salary(payday = 25),
            budgetLimitMinorUnits = 1_000_000L
        )
        val today = LocalDate.of(2026, 9, 15) // Tue
        val expenses = listOf(
            expense(atNoon(LocalDate.of(2026, 8, 26)), 100.00),
            expense(atNoon(LocalDate.of(2026, 9, 10)), 200.00),
            expense(atNoon(LocalDate.of(2026, 9, 15)), 50.00)
        )

        val snapshot = runPipeline(expenses, config, today)

        snapshot.capacity shouldBe 1_000_000
        snapshot.periodSpentRaw shouldBe 350_000
        snapshot.daysRemaining shouldBe 10
        snapshot.openingAllowance shouldBe 70_000
        snapshot.todayCharges shouldBe 50_000
        snapshot.remainingAllowance shouldBe 20_000
    }
}
