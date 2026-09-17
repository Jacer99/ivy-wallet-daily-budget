package com.ivy.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.navigation.IvyPreview
import com.ivy.ui.R
import java.math.BigDecimal

@Immutable
sealed interface SafeToSpendCardState {

    data object Loading : SafeToSpendCardState

    data object NoBudget : SafeToSpendCardState

    data class Active(
        val remainingAllowanceMinorUnits: Long,
        val openingAllowanceMinorUnits: Long,
        val todayChargesMinorUnits: Long,
        val tomorrowProjectionMinorUnits: Long?,
        val message: SafeToSpendMessage?,
    ) : SafeToSpendCardState

    data class Error(
        val message: String,
    ) : SafeToSpendCardState
}

@Immutable
sealed interface SafeToSpendMessage {
    data class OverToday(val overByMinorUnits: Long) : SafeToSpendMessage
    data object PeriodExhausted : SafeToSpendMessage
}

private val PocketMoneyAccent = Color(0xFFC4783E)

private fun formatTndAmount(minorUnits: Long): String =
    BigDecimal.valueOf(minorUnits, 3).toPlainString()

@Composable
fun SafeToSpendCard(
    state: SafeToSpendCardState,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onConfigureBudget: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(UI.shapes.r4)
            .background(UI.colors.medium)
            .testTag("safe_to_spend_card")
    ) {
        when (state) {
            is SafeToSpendCardState.Loading -> LoadingContent(Modifier.padding(24.dp))
            is SafeToSpendCardState.NoBudget -> NoBudgetContent(
                onConfigureBudget = onConfigureBudget,
                modifier = Modifier.padding(24.dp),
            )
            is SafeToSpendCardState.Active -> ActiveContent(
                state = state,
                onAddExpense = onAddExpense,
                onAddIncome = onAddIncome,
            )
            is SafeToSpendCardState.Error -> ErrorContent(
                message = state.message,
                modifier = Modifier.padding(24.dp),
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier.testTag("safe_to_spend_loading")) {
        Text(
            text = stringResource(R.string.safe_to_spend_today),
            style = UI.typo.c.style(
                color = UI.colors.mediumInverse,
                fontWeight = FontWeight.ExtraBold
            )
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.calculating_allowance),
            style = UI.typo.nB1.style(
                color = UI.colors.pureInverse,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun NoBudgetContent(
    onConfigureBudget: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onConfigureBudget)
            .testTag("safe_to_spend_no_budget"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.no_budget_message),
            style = UI.typo.nB1.style(
                color = UI.colors.pureInverse,
                fontWeight = FontWeight.Medium,
            ),
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = UI.colors.mediumInverse,
        )
    }
}

@Composable
private fun ErrorContent(message: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.testTag("safe_to_spend_error")) {
        Text(
            text = stringResource(R.string.allowance_calculation_error),
            style = UI.typo.c.style(
                color = UI.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold
            )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = UI.typo.nB2.style(color = UI.colors.mediumInverse)
        )
    }
}

@Composable
private fun ActiveContent(
    state: SafeToSpendCardState.Active,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
) {
    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.safe_to_spend_today),
                style = UI.typo.c.style(
                    color = UI.colors.mediumInverse,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            Spacer(Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.testTag("safe_to_spend_amount")
            ) {
                Text(
                    text = formatTndAmount(state.remainingAllowanceMinorUnits),
                    style = UI.typo.h1.style(
                        color = PocketMoneyAccent,
                        fontWeight = FontWeight.Black
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.safe_to_spend_currency),
                    style = UI.typo.b1.style(
                        color = UI.colors.mediumInverse,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(
                    R.string.safe_to_spend_context,
                    formatTndAmount(state.openingAllowanceMinorUnits),
                    formatTndAmount(state.todayChargesMinorUnits)
                ),
                style = UI.typo.nB2.style(color = UI.colors.mediumInverse),
            )

            state.tomorrowProjectionMinorUnits?.let { projectionMinorUnits ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.safe_to_spend_tomorrow,
                        formatTndAmount(projectionMinorUnits),
                        stringResource(R.string.safe_to_spend_currency)
                    ),
                    style = UI.typo.nB2.style(color = UI.colors.mediumInverse),
                    modifier = Modifier.testTag("safe_to_spend_tomorrow")
                )
            }

            when (val m = state.message) {
                null -> Unit
                is SafeToSpendMessage.OverToday -> {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(
                            R.string.safe_to_spend_over_today,
                            formatTndAmount(m.overByMinorUnits),
                        ),
                        style = UI.typo.nB2.style(
                            color = UI.colors.mediumInverse,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
                is SafeToSpendMessage.PeriodExhausted -> {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.safe_to_spend_period_exhausted),
                        style = UI.typo.nB2.style(
                            color = UI.colors.mediumInverse,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                }
            }
        }

        Divider(
            color = UI.colors.mediumInverse.copy(alpha = 0.2f),
            thickness = 1.dp,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("safe_to_spend_actions"),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onAddExpense,
                modifier = Modifier.weight(1f).testTag("safe_to_spend_add_expense"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PocketMoneyAccent,
                ),
                border = BorderStroke(1.dp, PocketMoneyAccent),
            ) {
                Text(
                    text = stringResource(R.string.add_expense),
                    style = UI.typo.nB2.style(
                        color = PocketMoneyAccent,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }

            OutlinedButton(
                onClick = onAddIncome,
                modifier = Modifier.weight(1f).testTag("safe_to_spend_add_income"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = UI.colors.mediumInverse,
                ),
                border = BorderStroke(1.dp, UI.colors.mediumInverse),
            ) {
                Text(
                    text = stringResource(R.string.add_income),
                    style = UI.typo.nB2.style(
                        color = UI.colors.mediumInverse,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewActive() {
    SafeToSpendCardPreview(
        SafeToSpendCardState.Active(
            remainingAllowanceMinorUnits = 57_400L,
            openingAllowanceMinorUnits = 80_400L,
            todayChargesMinorUnits = 23_000L,
            tomorrowProjectionMinorUnits = 62_000L,
            message = null,
        )
    )
}

@Preview
@Composable
private fun PreviewOverToday() {
    SafeToSpendCardPreview(
        SafeToSpendCardState.Active(
            remainingAllowanceMinorUnits = 0L,
            openingAllowanceMinorUnits = 80_400L,
            todayChargesMinorUnits = 95_000L,
            tomorrowProjectionMinorUnits = 45_000L,
            message = SafeToSpendMessage.OverToday(overByMinorUnits = 14_600L),
        )
    )
}

@Preview
@Composable
private fun PreviewNoBudget() {
    SafeToSpendCardPreview(SafeToSpendCardState.NoBudget)
}

@Preview
@Composable
private fun PreviewError() {
    SafeToSpendCardPreview(SafeToSpendCardState.Error("Please try again."))
}

@Composable
private fun SafeToSpendCardPreview(state: SafeToSpendCardState) {
    IvyPreview {
        SafeToSpendCard(
            state = state,
            onAddExpense = {},
            onAddIncome = {},
            onConfigureBudget = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
