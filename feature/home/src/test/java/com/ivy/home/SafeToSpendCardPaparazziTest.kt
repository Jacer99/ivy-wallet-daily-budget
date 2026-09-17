package com.ivy.home

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.ivy.base.legacy.Theme
import com.ivy.legacy.IvyWalletPreview
import com.ivy.ui.testing.PaparazziScreenshotTest
import com.ivy.ui.testing.PaparazziTheme
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class SafeToSpendCardPaparazziTest(
    @TestParameter
    private val theme: PaparazziTheme,
) : PaparazziScreenshotTest() {

    @Test
    fun activeState() {
        snapshot(theme) {
            SafeToSpendCardTestContainer(
                theme = theme,
                state = SafeToSpendCardState.Active(
                    remainingAllowanceMinorUnits = 57_400L,
                    openingAllowanceMinorUnits = 80_400L,
                    todayChargesMinorUnits = 23_000L,
                    tomorrowProjectionMinorUnits = 62_000L,
                    message = null,
                ),
            )
        }
    }

    @Test
    fun overTodayState() {
        snapshot(theme) {
            SafeToSpendCardTestContainer(
                theme = theme,
                state = SafeToSpendCardState.Active(
                    remainingAllowanceMinorUnits = 0L,
                    openingAllowanceMinorUnits = 80_400L,
                    todayChargesMinorUnits = 95_000L,
                    tomorrowProjectionMinorUnits = 45_000L,
                    message = SafeToSpendMessage.OverToday(overByMinorUnits = 14_600L),
                ),
            )
        }
    }

    @Test
    fun periodExhaustedState() {
        snapshot(theme) {
            SafeToSpendCardTestContainer(
                theme = theme,
                state = SafeToSpendCardState.Active(
                    remainingAllowanceMinorUnits = 0L,
                    openingAllowanceMinorUnits = 0L,
                    todayChargesMinorUnits = 0L,
                    tomorrowProjectionMinorUnits = 0L,
                    message = SafeToSpendMessage.PeriodExhausted,
                ),
            )
        }
    }

    @Test
    fun lastDayState() {
        snapshot(theme) {
            SafeToSpendCardTestContainer(
                theme = theme,
                state = SafeToSpendCardState.Active(
                    remainingAllowanceMinorUnits = 57_400L,
                    openingAllowanceMinorUnits = 80_400L,
                    todayChargesMinorUnits = 23_000L,
                    tomorrowProjectionMinorUnits = null,
                    message = null,
                ),
            )
        }
    }

    @Test
    fun noBudgetState() {
        snapshot(theme) {
            SafeToSpendCardTestContainer(
                theme = theme,
                state = SafeToSpendCardState.NoBudget,
            )
        }
    }

    @Test
    fun errorState() {
        snapshot(theme) {
            SafeToSpendCardTestContainer(
                theme = theme,
                state = SafeToSpendCardState.Error("Please try again."),
            )
        }
    }

    @Test
    fun loadingState() {
        snapshot(theme) {
            SafeToSpendCardTestContainer(
                theme = theme,
                state = SafeToSpendCardState.Loading,
            )
        }
    }
}

@Composable
private fun SafeToSpendCardTestContainer(
    theme: PaparazziTheme,
    state: SafeToSpendCardState,
) {
    val ivyTheme = if (theme == PaparazziTheme.Dark) Theme.DARK else Theme.LIGHT
    IvyWalletPreview(ivyTheme) {
        SafeToSpendCard(
            state = state,
            onAddExpense = {},
            onAddIncome = {},
            onConfigureBudget = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
