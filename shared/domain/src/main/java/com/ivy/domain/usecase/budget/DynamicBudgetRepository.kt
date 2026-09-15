package com.ivy.domain.usecase.budget

import com.ivy.data.model.AllocationMode
import com.ivy.data.repository.TransactionRepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Single entry point for obtaining a safe-to-spend budget snapshot.
 *
 * This repository coordinates resolving the current budget period, fetching relevant
 * transactions from the [TransactionRepository], and invoking the [DynamicBudgetEngine]
 * to compute the snapshot.
 *
 * v1 Limitations:
 * - Configuration is passed in manually (not yet persisted).
 * - Reservations are not yet implemented (defaults to zero).
 * - Income inclusion is not yet implemented (defaults to zero).
 * - Allocation mode is always [AllocationMode.TODAY].
 */
class DynamicBudgetRepository @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val resolver: BudgetPeriodResolver,
    private val mapper: DynamicBudgetMapper,
    private val engine: DynamicBudgetEngine,
) {

    /**
     * Calculates the [BudgetSnapshot] for the given configuration and date.
     *
     * @param config The budget configuration.
     * @param today The reference "today" date.
     * @param zoneId The user's local timezone.
     */
    suspend fun calculate(
        config: DynamicBudgetConfig,
        today: LocalDate,
        zoneId: ZoneId,
    ): BudgetSnapshot {
        val period = resolver.resolve(config.periodType, today)

        // Ivy's TransactionRepository.findAllBetween is inclusive on both ends.
        // We start at the beginning of the first day of the period.
        val startInstant = period.start.atStartOfDay(zoneId).toInstant()
        // We include up to the last nanosecond of the last day so no transactions 
        // on the final day are missed due to time-of-day.
        val endInstant = period.end.atTime(23, 59, 59, 999_999_999).atZone(zoneId).toInstant()

        val transactions = transactionRepository.findAllBetween(startInstant, endInstant)
        val input = mapper.map(transactions, config, today, zoneId)

        return engine.calculate(input)
    }
}
