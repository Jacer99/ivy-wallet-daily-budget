package com.ivy.domain.usecase.budget

import com.ivy.data.db.dao.read.DynamicBudgetConfigDao
import com.ivy.data.db.dao.write.WriteDynamicBudgetConfigDao
import com.ivy.data.db.entity.DynamicBudgetConfigEntity
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Instant
import java.util.UUID

class RoomDynamicBudgetConfigStoreTest {

    private val readDao = mockk<DynamicBudgetConfigDao>()
    private val writeDao = mockk<WriteDynamicBudgetConfigDao>(relaxed = true)
    private val store = RoomDynamicBudgetConfigStore(readDao, writeDao)

    @Test
    fun `load returns default when no row exists`() = runTest {
        coEvery { readDao.findFirstOrNull() } returns null

        val result = store.load()
        val default = DynamicBudgetConfig.default()

        result.periodType shouldBe default.periodType
        result.budgetLimitMinorUnits shouldBe default.budgetLimitMinorUnits
        result.currencyCode shouldBe default.currencyCode
        result.includedAccountIds shouldBe default.includedAccountIds
        result.includedCategoryIds shouldBe default.includedCategoryIds
        result.includeIncomeInBudget shouldBe default.includeIncomeInBudget
    }

    @Test
    fun `load returns mapped entity when row exists`() = runTest {
        val id = UUID.randomUUID()
        val now = Instant.now()
        val entity = DynamicBudgetConfigEntity(
            id = id,
            periodTypeTag = "MONTHLY",
            salaryPayday = null,
            customStartEpochDay = null,
            customEndEpochDay = null,
            budgetLimitMinorUnits = 5000L,
            currencyCode = "EUR",
            includedAccountIdsSerialized = null,
            includedCategoryIdsSerialized = null,
            includeIncomeInBudget = true,
            dateTime = now
        )
        coEvery { readDao.findFirstOrNull() } returns entity

        val result = store.load()
        result.id shouldBe id
        result.periodType shouldBe BudgetPeriodType.Monthly
        result.budgetLimitMinorUnits shouldBe 5000L
        result.currencyCode shouldBe "EUR"
        result.includeIncomeInBudget shouldBe true
    }

    @Test
    fun `save on empty table uses now for dateTime`() = runTest {
        coEvery { readDao.findFirstOrNull() } returns null
        val config = DynamicBudgetConfig.default()
        
        val before = Instant.now().minusMillis(1)
        store.save(config)
        val after = Instant.now().plusMillis(1)

        val slot = slot<DynamicBudgetConfigEntity>()
        coVerify { 
            writeDao.deleteAll()
            writeDao.save(capture(slot))
        }

        val captured = slot.captured
        captured.dateTime.isAfter(before) shouldBe true
        captured.dateTime.isBefore(after) shouldBe true
    }

    @Test
    fun `save on existing row preserves original dateTime`() = runTest {
        val oldTime = Instant.parse("2020-01-01T00:00:00Z")
        val existing = DynamicBudgetConfigEntity(
            id = UUID.randomUUID(),
            periodTypeTag = "WEEKLY",
            salaryPayday = null,
            customStartEpochDay = null,
            customEndEpochDay = null,
            budgetLimitMinorUnits = 1000L,
            currencyCode = "TND",
            includedAccountIdsSerialized = null,
            includedCategoryIdsSerialized = null,
            includeIncomeInBudget = false,
            dateTime = oldTime
        )
        coEvery { readDao.findFirstOrNull() } returns existing

        val config = DynamicBudgetConfig.default().copy(budgetLimitMinorUnits = 2000L)
        store.save(config)

        val slot = slot<DynamicBudgetConfigEntity>()
        coVerify { 
            writeDao.deleteAll()
            writeDao.save(capture(slot))
        }
        slot.captured.dateTime shouldBe oldTime
        slot.captured.budgetLimitMinorUnits shouldBe 2000L
    }
}
