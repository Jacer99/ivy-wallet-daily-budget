package com.ivy.domain.usecase.budget

import com.ivy.data.db.entity.DynamicBudgetConfigEntity
import com.ivy.data.model.AccountId
import com.ivy.data.model.CategoryId
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

internal object DynamicBudgetConfigEntityMapper {

    /**
     * Builds an entity from a domain config. The caller supplies `dateTime`
     * because the store decides whether to preserve an existing creation
     * time or stamp a fresh one.
     */
    fun toEntity(
        config: DynamicBudgetConfig,
        dateTime: Instant,
    ): DynamicBudgetConfigEntity {
        val (tag, payday, start, end) = when (val p = config.periodType) {
            is BudgetPeriodType.Weekly -> Quad(
                "WEEKLY", null, null, null
            )
            is BudgetPeriodType.Monthly -> Quad(
                "MONTHLY", null, null, null
            )
            is BudgetPeriodType.Salary -> Quad(
                "SALARY", p.payday, null, null
            )
            is BudgetPeriodType.Custom -> Quad(
                "CUSTOM", null, p.start.toEpochDay(), p.end.toEpochDay()
            )
        }

        return DynamicBudgetConfigEntity(
            id = config.id,
            periodTypeTag = tag,
            salaryPayday = payday,
            customStartEpochDay = start,
            customEndEpochDay = end,
            budgetLimitMinorUnits = config.budgetLimitMinorUnits,
            currencyCode = config.currencyCode,
            includedAccountIdsSerialized = serializeIds(config.includedAccountIds.map { it.value }),
            includedCategoryIdsSerialized = serializeIds(config.includedCategoryIds.map { it.value }),
            includeIncomeInBudget = config.includeIncomeInBudget,
            dateTime = dateTime
        )
    }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

    /**
     * Rebuilds a domain config from a persisted entity. Throws
     * IllegalArgumentException if the entity's tag+params combination is
     * inconsistent (e.g. tag = "SALARY" but salaryPayday = null) or if the
     * tag is unrecognized.
     */
    fun toDomain(entity: DynamicBudgetConfigEntity): DynamicBudgetConfig {
        val periodType = when (entity.periodTypeTag) {
            "WEEKLY" -> BudgetPeriodType.Weekly
            "MONTHLY" -> BudgetPeriodType.Monthly
            "SALARY" -> {
                val payday = requireNotNull(entity.salaryPayday) {
                    "SALARY period requires salaryPayday"
                }
                BudgetPeriodType.Salary(payday)
            }
            "CUSTOM" -> {
                val start = requireNotNull(entity.customStartEpochDay) {
                    "CUSTOM period requires customStartEpochDay"
                }
                val end = requireNotNull(entity.customEndEpochDay) {
                    "CUSTOM period requires customEndEpochDay"
                }
                BudgetPeriodType.Custom(
                    LocalDate.ofEpochDay(start),
                    LocalDate.ofEpochDay(end)
                )
            }
            else -> error("Unknown periodTypeTag: ${entity.periodTypeTag}")
        }

        return DynamicBudgetConfig(
            id = entity.id,
            periodType = periodType,
            budgetLimitMinorUnits = entity.budgetLimitMinorUnits,
            currencyCode = entity.currencyCode,
            includedAccountIds = parseIds(entity.includedAccountIdsSerialized).map(::AccountId).toSet(),
            includedCategoryIds = parseIds(entity.includedCategoryIdsSerialized).map(::CategoryId).toSet(),
            includeIncomeInBudget = entity.includeIncomeInBudget
        )
    }

    private fun serializeIds(ids: List<UUID>): String? {
        if (ids.isEmpty()) return null
        return ids.sorted().joinToString(separator = ",")
    }

    private fun parseIds(idsString: String?): List<UUID> {
        if (idsString.isNullOrBlank()) return emptyList()
        return try {
            idsString.split(",")
                .map { it.trim() }
                .map { UUID.fromString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
