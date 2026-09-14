package com.ivy.domain.usecase.budget

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(TestParameterInjector::class)
class BudgetPeriodResolverTest {

    enum class ResolverTestCase(
        val typeStr: String,
        val todayStr: String,
        val expectedStartStr: String,
        val expectedEndStr: String
    ) {
        Case1("Weekly", "2026-09-16", "2026-09-14", "2026-09-20"),
        Case2("Weekly", "2026-09-14", "2026-09-14", "2026-09-20"),
        Case3("Weekly", "2026-09-20", "2026-09-14", "2026-09-20"),
        Case4("Monthly", "2026-01-15", "2026-01-01", "2026-01-31"),
        Case5("Monthly", "2026-04-10", "2026-04-01", "2026-04-30"),
        Case6("Monthly", "2026-02-10", "2026-02-01", "2026-02-28"),
        Case7("Monthly", "2028-02-10", "2028-02-01", "2028-02-29"),
        Case8("Salary:25", "2026-09-15", "2026-08-25", "2026-09-24"),
        Case9("Salary:25", "2026-09-26", "2026-09-25", "2026-10-24"),
        Case10("Salary:25", "2026-09-25", "2026-09-25", "2026-10-24"),
        Case11("Salary:31", "2026-02-15", "2026-01-31", "2026-02-27"),
        Case12("Salary:31", "2026-02-28", "2026-02-28", "2026-03-30"),
        Case13("Salary:29", "2027-02-15", "2027-01-29", "2027-02-27"),
        Case14("Salary:30", "2026-04-10", "2026-03-30", "2026-04-29"),
        Case15("Custom:2026-09-14:2026-09-14", "2026-09-14", "2026-09-14", "2026-09-14"),
        Case16("Custom:2026-08-15:2026-09-20", "2026-09-01", "2026-08-15", "2026-09-20")
    }

    private fun parsePeriodType(typeStr: String): BudgetPeriodType {
        return when {
            typeStr == "Weekly" -> BudgetPeriodType.Weekly
            typeStr == "Monthly" -> BudgetPeriodType.Monthly
            typeStr.startsWith("Salary:") -> {
                val payday = typeStr.substringAfter("Salary:").toInt()
                BudgetPeriodType.Salary(payday)
            }
            typeStr.startsWith("Custom:") -> {
                val parts = typeStr.split(":")
                BudgetPeriodType.Custom(LocalDate.parse(parts[1]), LocalDate.parse(parts[2]))
            }
            else -> throw IllegalArgumentException("Unknown type: $typeStr")
        }
    }

    @Test
    fun `resolves budget period correctly`(@TestParameter testCase: ResolverTestCase) {
        val resolver = BudgetPeriodResolver()
        val type = parsePeriodType(testCase.typeStr)
        val today = LocalDate.parse(testCase.todayStr)

        val period = resolver.resolve(type, today)

        period.start shouldBe LocalDate.parse(testCase.expectedStartStr)
        period.end shouldBe LocalDate.parse(testCase.expectedEndStr)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `custom period throws exception if end before start`() {
        val type = BudgetPeriodType.Custom(LocalDate.parse("2026-09-20"), LocalDate.parse("2026-09-14"))
        BudgetPeriodResolver().resolve(type, LocalDate.parse("2026-09-15"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `salary period throws exception if payday is zero`() {
        val type = BudgetPeriodType.Salary(0)
        BudgetPeriodResolver().resolve(type, LocalDate.parse("2026-09-15"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `salary period throws exception if payday is 32`() {
        val type = BudgetPeriodType.Salary(32)
        BudgetPeriodResolver().resolve(type, LocalDate.parse("2026-09-15"))
    }

    enum class DaysRemainingTestCase(
        val startStr: String,
        val endStr: String,
        val todayStr: String,
        val expectedDays: Int
    ) {
        Case20("2026-09-14", "2026-09-20", "2026-09-14", 7),
        Case21("2026-09-14", "2026-09-20", "2026-09-20", 1),
        Case22("2026-09-14", "2026-09-20", "2026-09-25", 0),
        Case23("2026-09-14", "2026-09-20", "2026-09-10", 7)
    }

    @Test
    fun `calculates days remaining correctly`(@TestParameter testCase: DaysRemainingTestCase) {
        val start = LocalDate.parse(testCase.startStr)
        val end = LocalDate.parse(testCase.endStr)
        val today = LocalDate.parse(testCase.todayStr)
        val period = BudgetPeriod(start, end, BudgetPeriodType.Weekly)

        period.daysRemaining(today) shouldBe testCase.expectedDays
    }
}
