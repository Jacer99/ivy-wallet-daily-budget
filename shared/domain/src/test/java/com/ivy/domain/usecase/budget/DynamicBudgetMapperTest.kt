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

class DynamicBudgetMapperTest {

    private val resolver = BudgetPeriodResolver()
    private val mapper = DynamicBudgetMapper(resolver)
    private val zone = ZoneId.of("Africa/Tunis")
    private val today = LocalDate.of(2026, 9, 16) // Wednesday
    private val periodEnd = LocalDate.of(2026, 9, 20) // Sunday

    private fun atNoon(date: LocalDate): Instant =
        date.atTime(12, 0).atZone(zone).toInstant()

    private fun expense(
        id: TransactionId = TransactionId(UUID.randomUUID()),
        title: String? = "Test",
        category: CategoryId? = null,
        time: Instant,
        settled: Boolean = true,
        amount: Double = 100.0,
        asset: String = "TND",
        account: AccountId = AccountId(UUID.randomUUID()),
    ): Expense = Expense(
        id = id,
        title = title?.let { NotBlankTrimmedString.unsafe(it) },
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
    fun `empty transaction list maps to empty expenses`() {
        val config = DynamicBudgetConfig.default()
        val result = mapper.map(emptyList(), config, today, zone)

        result.expenses shouldBe emptyList()
        result.period.end shouldBe periodEnd
    }

    @Test
    fun `single TND expense on Tuesday maps to one entry with minor units`() {
        val config = DynamicBudgetConfig.default()
        val e = expense(time = atNoon(LocalDate.of(2026, 9, 15)), amount = 45.50)
        val result = mapper.map(listOf(e), config, today, zone)

        result.expenses shouldBe listOf(
            BudgetExpenseInput(
                transactionDate = LocalDate.of(2026, 9, 15),
                amount = 45500L,
                mode = AllocationMode.TODAY
            )
        )
    }

    @Test
    fun `expense with settled false is filtered out`() {
        val config = DynamicBudgetConfig.default()
        val e = expense(time = atNoon(LocalDate.of(2026, 9, 15)), settled = false)
        val result = mapper.map(listOf(e), config, today, zone)

        result.expenses shouldBe emptyList()
    }

    @Test
    fun `income is filtered out`() {
        val config = DynamicBudgetConfig.default()
        val i = Income(
            id = TransactionId(UUID.randomUUID()),
            title = null,
            description = null,
            category = null,
            time = atNoon(today),
            settled = true,
            metadata = TransactionMetadata(null, null, null, null),
            tags = emptyList(),
            value = PositiveValue(PositiveDouble.unsafe(100.0), AssetCode.unsafe("TND")),
            account = AccountId(UUID.randomUUID())
        )
        val result = mapper.map(listOf(i), config, today, zone)

        result.expenses shouldBe emptyList()
    }

    @Test
    fun `transfer is filtered out`() {
        val config = DynamicBudgetConfig.default()
        val t = Transfer(
            id = TransactionId(UUID.randomUUID()),
            title = null,
            description = null,
            category = null,
            time = atNoon(today),
            settled = true,
            metadata = TransactionMetadata(null, null, null, null),
            tags = emptyList(),
            fromAccount = AccountId(UUID.randomUUID()),
            fromValue = PositiveValue(PositiveDouble.unsafe(100.0), AssetCode.unsafe("TND")),
            toAccount = AccountId(UUID.randomUUID()),
            toValue = PositiveValue(PositiveDouble.unsafe(100.0), AssetCode.unsafe("TND"))
        )
        val result = mapper.map(listOf(t), config, today, zone)

        result.expenses shouldBe emptyList()
    }

    @Test
    fun `expense in wrong currency is filtered out`() {
        val config = DynamicBudgetConfig.default().copy(currencyCode = "TND")
        val e = expense(time = atNoon(today), asset = "USD")
        val result = mapper.map(listOf(e), config, today, zone)

        result.expenses shouldBe emptyList()
    }

    @Test
    fun `expense in matching currency case-insensitive match is included`() {
        val config = DynamicBudgetConfig.default().copy(currencyCode = "TND")
        val e = expense(time = atNoon(today), asset = "tnd")
        val result = mapper.map(listOf(e), config, today, zone)

        result.expenses.size shouldBe 1
    }

    @Test
    fun `empty includedAccountIds means all accounts eligible`() {
        val config = DynamicBudgetConfig.default().copy(includedAccountIds = emptySet())
        val e = expense(time = atNoon(today))
        val result = mapper.map(listOf(e), config, today, zone)

        result.expenses.size shouldBe 1
    }

    @Test
    fun `non-empty includedAccountIds filters correctly`() {
        val allowed = AccountId(UUID.randomUUID())
        val config = DynamicBudgetConfig.default().copy(includedAccountIds = setOf(allowed))
        val e1 = expense(time = atNoon(today), account = allowed)
        val e2 = expense(time = atNoon(today), account = AccountId(UUID.randomUUID()))
        
        val result = mapper.map(listOf(e1, e2), config, today, zone)

        result.expenses.size shouldBe 1
        result.expenses.single().amount shouldBe CurrencyMinorUnits.toMinorUnits(100.0, "TND")
    }

    @Test
    fun `empty includedCategoryIds means all categories eligible`() {
        val config = DynamicBudgetConfig.default().copy(includedCategoryIds = emptySet())
        val e = expense(time = atNoon(today), category = CategoryId(UUID.randomUUID()))
        val result = mapper.map(listOf(e), config, today, zone)

        result.expenses.size shouldBe 1
    }

    @Test
    fun `null category is always included even when includedCategoryIds is non-empty`() {
        val config = DynamicBudgetConfig.default().copy(includedCategoryIds = setOf(CategoryId(UUID.randomUUID())))
        val e = expense(time = atNoon(today), category = null)
        val result = mapper.map(listOf(e), config, today, zone)

        result.expenses.size shouldBe 1
    }

    @Test
    fun `non-null category not in includedCategoryIds is excluded`() {
        val catA = CategoryId(UUID.randomUUID())
        val catB = CategoryId(UUID.randomUUID())
        val config = DynamicBudgetConfig.default().copy(includedCategoryIds = setOf(catA))
        val e = expense(time = atNoon(today), category = catB)
        
        val result = mapper.map(listOf(e), config, today, zone)

        result.expenses.size shouldBe 0
    }

    @Test
    fun `timezone conversion maps local time at end of day to correct date`() {
        val time = LocalDate.of(2026, 9, 15).atTime(23, 30).atZone(zone).toInstant()
        val e = expense(time = time)
        val result = mapper.map(listOf(e), DynamicBudgetConfig.default(), today, zone)

        result.expenses.single().transactionDate shouldBe LocalDate.of(2026, 9, 15)
    }

    @Test
    fun `timezone conversion maps UTC time to correct local date`() {
        // 00:30 UTC on Sep 16 is 01:30 Tunis time on Sep 16
        val time = LocalDate.of(2026, 9, 16).atTime(0, 30).atZone(ZoneId.of("UTC")).toInstant()
        val e = expense(time = time)
        val result = mapper.map(listOf(e), DynamicBudgetConfig.default(), today, zone)

        result.expenses.single().transactionDate shouldBe LocalDate.of(2026, 9, 16)
    }

    @Test
    fun `budgetLimit is passed through to engine input`() {
        val config = DynamicBudgetConfig.default().copy(budgetLimitMinorUnits = 70000L)
        val result = mapper.map(emptyList(), config, today, zone)

        result.budgetLimit shouldBe 70000L
    }

    @Test
    fun `includedIncome and reservationsTotal are 0 in v1`() {
        val result = mapper.map(emptyList(), DynamicBudgetConfig.default(), today, zone)

        result.includedIncome shouldBe 0L
        result.reservationsTotal shouldBe 0L
    }

    @Test
    fun `monthly config resolves correct period`() {
        val config = DynamicBudgetConfig.default().copy(periodType = BudgetPeriodType.Monthly)
        val result = mapper.map(emptyList(), config, today, zone)

        result.period.start shouldBe LocalDate.of(2026, 9, 1)
        result.period.end shouldBe LocalDate.of(2026, 9, 30)
    }

    @Test
    fun `weekly config resolves expected dates`() {
        val result = mapper.map(emptyList(), DynamicBudgetConfig.default(), today, zone)

        result.period.start shouldBe LocalDate.of(2026, 9, 14)
        result.period.end shouldBe LocalDate.of(2026, 9, 20)
    }
}
