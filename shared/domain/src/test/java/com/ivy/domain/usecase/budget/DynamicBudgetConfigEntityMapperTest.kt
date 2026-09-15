package com.ivy.domain.usecase.budget

import com.ivy.data.db.entity.DynamicBudgetConfigEntity
import com.ivy.data.model.AccountId
import com.ivy.data.model.CategoryId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class DynamicBudgetConfigEntityMapperTest {

    @Test
    fun `weekly round-trips`() {
        val config = DynamicBudgetConfig.default().copy(periodType = BudgetPeriodType.Weekly)
        val now = Instant.now()
        val entity = DynamicBudgetConfigEntityMapper.toEntity(config, now)

        entity.periodTypeTag shouldBe "WEEKLY"
        entity.salaryPayday shouldBe null
        entity.customStartEpochDay shouldBe null
        entity.customEndEpochDay shouldBe null

        DynamicBudgetConfigEntityMapper.toDomain(entity) shouldBe config
    }

    @Test
    fun `monthly round-trips`() {
        val config = DynamicBudgetConfig.default().copy(periodType = BudgetPeriodType.Monthly)
        val now = Instant.now()
        val entity = DynamicBudgetConfigEntityMapper.toEntity(config, now)

        entity.periodTypeTag shouldBe "MONTHLY"
        DynamicBudgetConfigEntityMapper.toDomain(entity) shouldBe config
    }

    @Test
    fun `salary round-trips with payday`() {
        val config = DynamicBudgetConfig.default().copy(periodType = BudgetPeriodType.Salary(15))
        val now = Instant.now()
        val entity = DynamicBudgetConfigEntityMapper.toEntity(config, now)

        entity.periodTypeTag shouldBe "SALARY"
        entity.salaryPayday shouldBe 15
        entity.customStartEpochDay shouldBe null
        entity.customEndEpochDay shouldBe null

        DynamicBudgetConfigEntityMapper.toDomain(entity) shouldBe config
    }

    @Test
    fun `custom round-trips with dates`() {
        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 1, 31)
        val config = DynamicBudgetConfig.default().copy(periodType = BudgetPeriodType.Custom(start, end))
        val now = Instant.now()
        val entity = DynamicBudgetConfigEntityMapper.toEntity(config, now)

        entity.periodTypeTag shouldBe "CUSTOM"
        entity.salaryPayday shouldBe null
        entity.customStartEpochDay shouldBe start.toEpochDay()
        entity.customEndEpochDay shouldBe end.toEpochDay()

        DynamicBudgetConfigEntityMapper.toDomain(entity) shouldBe config
    }

    @Test
    fun `empty id sets serialize to null`() {
        val config = DynamicBudgetConfig.default().copy(
            includedAccountIds = emptySet(),
            includedCategoryIds = emptySet()
        )
        val entity = DynamicBudgetConfigEntityMapper.toEntity(config, Instant.now())

        entity.includedAccountIdsSerialized shouldBe null
        entity.includedCategoryIdsSerialized shouldBe null
        
        DynamicBudgetConfigEntityMapper.toDomain(entity) shouldBe config
    }

    @Test
    fun `non-empty id sets round-trip as a set`() {
        val accountIds = setOf(
            AccountId(UUID.randomUUID()),
            AccountId(UUID.randomUUID()),
            AccountId(UUID.randomUUID())
        )
        val categoryIds = setOf(
            CategoryId(UUID.randomUUID()),
            CategoryId(UUID.randomUUID())
        )
        val config = DynamicBudgetConfig.default().copy(
            includedAccountIds = accountIds,
            includedCategoryIds = categoryIds
        )
        val entity = DynamicBudgetConfigEntityMapper.toEntity(config, Instant.now())

        val roundTrip = DynamicBudgetConfigEntityMapper.toDomain(entity)
        roundTrip.includedAccountIds shouldBe accountIds
        roundTrip.includedCategoryIds shouldBe categoryIds
    }

    @Test
    fun `unknown tag throws`() {
        val entity = DynamicBudgetConfigEntity(
            id = UUID.randomUUID(),
            periodTypeTag = "BOGUS",
            salaryPayday = null,
            customStartEpochDay = null,
            customEndEpochDay = null,
            budgetLimitMinorUnits = 1000,
            currencyCode = "TND",
            includedAccountIdsSerialized = null,
            includedCategoryIdsSerialized = null,
            includeIncomeInBudget = false,
            dateTime = Instant.now()
        )
        shouldThrow<IllegalStateException> {
            DynamicBudgetConfigEntityMapper.toDomain(entity)
        }
    }

    @Test
    fun `salary tag without payday throws`() {
        val entity = DynamicBudgetConfigEntity(
            id = UUID.randomUUID(),
            periodTypeTag = "SALARY",
            salaryPayday = null,
            customStartEpochDay = null,
            customEndEpochDay = null,
            budgetLimitMinorUnits = 1000,
            currencyCode = "TND",
            includedAccountIdsSerialized = null,
            includedCategoryIdsSerialized = null,
            includeIncomeInBudget = false,
            dateTime = Instant.now()
        )
        shouldThrow<IllegalArgumentException> {
            DynamicBudgetConfigEntityMapper.toDomain(entity)
        }
    }

    @Test
    fun `custom tag without dates throws`() {
        val entity = DynamicBudgetConfigEntity(
            id = UUID.randomUUID(),
            periodTypeTag = "CUSTOM",
            salaryPayday = null,
            customStartEpochDay = null,
            customEndEpochDay = null,
            budgetLimitMinorUnits = 1000,
            currencyCode = "TND",
            includedAccountIdsSerialized = null,
            includedCategoryIdsSerialized = null,
            includeIncomeInBudget = false,
            dateTime = Instant.now()
        )
        shouldThrow<IllegalArgumentException> {
            DynamicBudgetConfigEntityMapper.toDomain(entity)
        }
    }
}
