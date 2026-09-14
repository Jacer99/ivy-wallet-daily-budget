package com.ivy.domain.usecase.budget

import com.ivy.data.model.AccountId
import com.ivy.data.model.CategoryId
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test
import java.util.UUID

class DynamicBudgetConfigTest {

    @Test
    fun `default returns expected baseline`() {
        val config = DynamicBudgetConfig.default()
        
        config.id shouldNotBe null
        config.periodType shouldBe BudgetPeriodType.Weekly
        config.budgetLimitMinorUnits shouldBe 0L
        config.currencyCode shouldBe "TND"
        config.includedAccountIds.isEmpty() shouldBe true
        config.includedCategoryIds.isEmpty() shouldBe true
        config.includeIncomeInBudget shouldBe false
    }

    @Test
    fun `default returns distinct ids on successive calls`() {
        val a = DynamicBudgetConfig.default()
        val b = DynamicBudgetConfig.default()
        a.id shouldNotBe b.id
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructing with negative budget throws IllegalArgumentException`() {
        DynamicBudgetConfig(
            id = UUID.randomUUID(),
            periodType = BudgetPeriodType.Weekly,
            budgetLimitMinorUnits = -1L,
            currencyCode = "TND",
            includedAccountIds = emptySet(),
            includedCategoryIds = emptySet(),
            includeIncomeInBudget = false
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructing with blank currency code throws - empty`() {
        DynamicBudgetConfig(
            id = UUID.randomUUID(),
            periodType = BudgetPeriodType.Weekly,
            budgetLimitMinorUnits = 100L,
            currencyCode = "",
            includedAccountIds = emptySet(),
            includedCategoryIds = emptySet(),
            includeIncomeInBudget = false
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructing with blank currency code throws - blank`() {
        DynamicBudgetConfig(
            id = UUID.randomUUID(),
            periodType = BudgetPeriodType.Weekly,
            budgetLimitMinorUnits = 100L,
            currencyCode = "   ",
            includedAccountIds = emptySet(),
            includedCategoryIds = emptySet(),
            includeIncomeInBudget = false
        )
    }

    @Test
    fun `equality is structural`() {
        val id = UUID.randomUUID()
        val a = DynamicBudgetConfig(
            id = id,
            periodType = BudgetPeriodType.Weekly,
            budgetLimitMinorUnits = 100L,
            currencyCode = "TND",
            includedAccountIds = emptySet(),
            includedCategoryIds = emptySet(),
            includeIncomeInBudget = false
        )
        val b = DynamicBudgetConfig(
            id = id,
            periodType = BudgetPeriodType.Weekly,
            budgetLimitMinorUnits = 100L,
            currencyCode = "TND",
            includedAccountIds = emptySet(),
            includedCategoryIds = emptySet(),
            includeIncomeInBudget = false
        )
        val c = a.copy(includeIncomeInBudget = true)

        a shouldBe b
        a shouldNotBe c
    }

    @Test
    fun `empty inclusion set means all`() {
        val config = DynamicBudgetConfig.default()
        config.includedAccountIds.isEmpty() shouldBe true
        config.includedCategoryIds.isEmpty() shouldBe true
    }
}
