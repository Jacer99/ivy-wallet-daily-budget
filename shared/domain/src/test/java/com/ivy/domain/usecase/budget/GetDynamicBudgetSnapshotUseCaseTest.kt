package com.ivy.domain.usecase.budget

import com.ivy.data.repository.TransactionRepository
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class GetDynamicBudgetSnapshotUseCaseTest {

    private val zone = ZoneId.of("Africa/Tunis")
    private val store = mockk<DynamicBudgetConfigStore>()
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
    private val useCase = GetDynamicBudgetSnapshotUseCase(store, repository)

    @Test
    fun `use case loads config from the store then delegates to the repository`() = runTest {
        // given
        val config = DynamicBudgetConfig.default().copy(budgetLimitMinorUnits = 700_000L)
        val today = LocalDate.of(2026, 9, 14) // Mon
        coEvery { store.load() } returns config
        coEvery { transactionRepo.findAllBetween(any(), any()) } returns emptyList()

        // when
        val snapshot = useCase(today, zone)

        // then
        snapshot.capacity shouldBe 700_000
        snapshot.daysRemaining shouldBe 7
        snapshot.openingAllowance shouldBe 100_000
        snapshot.todayCharges shouldBe 0
        snapshot.remainingAllowance shouldBe 100_000
        snapshot.tomorrowProjection shouldBe 116_666
    }

    @Test
    fun `use case returns empty-expense snapshot when repository returns no transactions`() = runTest {
        // given
        val config = DynamicBudgetConfig.default().copy(budgetLimitMinorUnits = 700_000L)
        val today = LocalDate.of(2026, 9, 14) // Mon
        coEvery { store.load() } returns config
        coEvery { transactionRepo.findAllBetween(any(), any()) } returns emptyList()

        // when
        val snapshot = useCase(today, zone)

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
    fun `use case propagates config correctly when the store returns a monthly config`() = runTest {
        // given
        val config = DynamicBudgetConfig.default().copy(
            periodType = BudgetPeriodType.Monthly,
            budgetLimitMinorUnits = 3_100_000L,
        )
        val today = LocalDate.of(2026, 9, 16) // Wed
        coEvery { store.load() } returns config
        coEvery { transactionRepo.findAllBetween(any(), any()) } returns emptyList()

        // when
        val snapshot = useCase(today, zone)

        // then
        snapshot.periodStart shouldBe LocalDate.of(2026, 9, 1)
        snapshot.periodEnd shouldBe LocalDate.of(2026, 9, 30)
        snapshot.capacity shouldBe 3_100_000
        snapshot.daysRemaining shouldBe 15
        snapshot.openingAllowance shouldBe 206_666
    }

    @Test
    fun `use case propagates config correctly when the store returns a salary config`() = runTest {
        // given
        val config = DynamicBudgetConfig.default().copy(
            periodType = BudgetPeriodType.Salary(payday = 25),
            budgetLimitMinorUnits = 1_000_000L,
        )
        val today = LocalDate.of(2026, 9, 15) // Tue
        coEvery { store.load() } returns config
        coEvery { transactionRepo.findAllBetween(any(), any()) } returns emptyList()

        // when
        val snapshot = useCase(today, zone)

        // then
        snapshot.periodStart shouldBe LocalDate.of(2026, 8, 25)
        snapshot.periodEnd shouldBe LocalDate.of(2026, 9, 24)
        snapshot.daysRemaining shouldBe 10
        snapshot.openingAllowance shouldBe 100_000
    }

    @Test
    fun `use case does not call store save during calculate`() = runTest {
        // given
        val config = DynamicBudgetConfig.default()
        val today = LocalDate.of(2026, 9, 14)
        coEvery { store.load() } returns config
        coEvery { transactionRepo.findAllBetween(any(), any()) } returns emptyList()

        // when
        useCase(today, zone)

        // then
        coVerify(exactly = 0) { store.save(any()) }
    }
}
