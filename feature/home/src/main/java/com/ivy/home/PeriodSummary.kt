package com.ivy.home

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.ui.R
import com.ivy.ui.component.glassSurface

@Composable
fun PeriodSummary(
    state: SafeToSpendCardState.Active,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassSurface(shape = RoundedCornerShape(20.dp), isDark = isDark)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("period_summary"),
    ) {
        PeriodSummaryRow(
            label = stringResource(R.string.period_summary_budget),
            value = "${formatTndAmount(state.periodBudgetMinorUnits)} TND",
            testTag = "period_summary_budget",
            valueTestTag = "period_summary_budget_value",
            isDark = isDark,
        )
        Spacer(Modifier.height(10.dp))
        PeriodSummaryRow(
            label = stringResource(R.string.period_summary_spent),
            value = "${formatTndAmount(state.periodSpentMinorUnits)} TND",
            testTag = "period_summary_spent",
            valueTestTag = "period_summary_spent_value",
            isDark = isDark,
        )
        Spacer(Modifier.height(10.dp))
        PeriodSummaryRow(
            label = stringResource(R.string.period_summary_available),
            value = "${formatTndAmount(state.periodAvailableMinorUnits)} TND",
            testTag = "period_summary_available",
            valueTestTag = "period_summary_available_value",
            isDark = isDark,
        )
        Spacer(Modifier.height(10.dp))
        PeriodSummaryRow(
            label = stringResource(R.string.period_summary_days_left),
            value = "${state.daysLeft}",
            testTag = "period_summary_days_left",
            valueTestTag = "period_summary_days_left_value",
            isDark = isDark,
        )
    }
}

@Composable
private fun PeriodSummaryRow(
    label: String,
    value: String,
    testTag: String,
    valueTestTag: String,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF78716C),
            ),
        )
        Text(
            text = value,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1C1917),
            ),
            modifier = Modifier.testTag(valueTestTag),
        )
    }
}
