package com.ivy.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.design.system.isAppInDarkTheme
import com.ivy.navigation.IvyPreview
import com.ivy.ui.R
import com.ivy.ui.component.LiquidGlassTokens
import com.ivy.ui.component.glassSurface
import java.math.BigDecimal
import java.math.RoundingMode

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
        val paydayLabel: String? = null,
        val periodBudgetMinorUnits: Long = 0L,
        val periodSpentMinorUnits: Long = 0L,
        val periodAvailableMinorUnits: Long = 0L,
        val daysLeft: Int = 0,
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

internal fun formatTndAmount(minorUnits: Long): String =
    BigDecimal.valueOf(minorUnits, 3)
        .setScale(2, RoundingMode.HALF_UP)
        .toPlainString()

@Composable
fun SafeToSpendCard(
    state: SafeToSpendCardState,
    onConfigureBudget: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isAppInDarkTheme()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("safe_to_spend_card")
    ) {
        when (state) {
            is SafeToSpendCardState.Loading -> LoadingContent(isDark = isDark)
            is SafeToSpendCardState.NoBudget -> NoBudgetContent(
                isDark = isDark,
                onConfigureBudget = onConfigureBudget,
            )
            is SafeToSpendCardState.Active -> ActiveContent(
                state = state,
                isDark = isDark,
            )
            is SafeToSpendCardState.Error -> ErrorContent(
                message = state.message,
                isDark = isDark,
            )
        }
    }
}

@Composable
private fun LoadingContent(isDark: Boolean, modifier: Modifier = Modifier) {
    val cardShape = remember { RoundedCornerShape(24.dp) }
    val titleColor = if (isDark) Color(0xFF00F2FE).copy(alpha = 0.80f) else UI.colors.mediumInverse
    val textColor = remember(isDark) {
        if (isDark) Color(0xFFCBD5E1) else Color(0xFF44403C)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassSurface(shape = cardShape, isDark = isDark)
            .padding(24.dp)
            .testTag("safe_to_spend_loading"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.safe_to_spend_today).uppercase(),
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.12.sp,
                    color = titleColor
                )
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.calculating_allowance),
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            )
        }
    }
}

@Composable
private fun NoBudgetContent(
    isDark: Boolean,
    onConfigureBudget: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardShape = remember { RoundedCornerShape(24.dp) }
    val textColor = remember(isDark) {
        if (isDark) Color(0xFFCBD5E1) else Color(0xFF44403C)
    }
    val iconTint = remember(isDark) {
        if (isDark) Color(0xFF00F2FE) else Color(0xFF9A421E)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassSurface(shape = cardShape, isDark = isDark)
            .clickable(onClick = onConfigureBudget)
            .padding(20.dp)
            .testTag("safe_to_spend_no_budget"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.no_budget_message),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                ),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = iconTint,
            )
        }
    }
}

@Composable
private fun ErrorContent(message: String, isDark: Boolean, modifier: Modifier = Modifier) {
    val cardShape = remember { RoundedCornerShape(24.dp) }
    val errorColor = remember(isDark) {
        if (isDark) Color(0xFFF43F5E) else Color(0xFFB91C1C)
    }
    val descColor = remember(isDark) {
        if (isDark) Color(0xFFCBD5E1) else Color(0xFF57534E)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassSurface(shape = cardShape, isDark = isDark)
            .padding(20.dp)
            .testTag("safe_to_spend_error"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.allowance_calculation_error),
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = errorColor
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = TextStyle(
                    fontSize = 13.sp,
                    color = descColor
                )
            )
        }
    }
}

@Composable
private fun ActiveContent(
    state: SafeToSpendCardState.Active,
    isDark: Boolean,
) {
    val cardShape = remember { RoundedCornerShape(24.dp) }
    val pillShape = remember { CircleShape }

    val titleColor = if (isDark) Color(0xFF00F2FE).copy(alpha = 0.80f) else UI.colors.mediumInverse
    val currencyColor = remember(isDark) {
        if (isDark) Color(0xFFCBD5E1) else Color(0xFF292524)
    }
    val amountBrush = remember(isDark) {
        if (isDark) LiquidGlassTokens.DarkHeroAmountBrush else LiquidGlassTokens.LightHeroAmountBrush
    }
    val overspendColor = remember(isDark) {
        if (isDark) Color(0xFFF43F5E) else Color(0xFFB91C1C)
    }

    val tomorrowFill = remember(isDark) {
        if (isDark) Color(0xFF111C30).copy(alpha = 0.90f) else Color(0xFFF3EDE2)
    }
    val tomorrowBorder = remember(isDark) {
        if (isDark) Color(0xFF00F2FE).copy(alpha = 0.20f) else Color(0xFFE4DAC9)
    }
    val tomorrowTextColor = remember(isDark) {
        if (isDark) Color(0xFFCBD5E1) else Color(0xFF111111)
    }
    val tomorrowIconTint = remember(isDark) {
        if (isDark) Color(0xFF22D3EE) else Color(0xFF111111)
    }
    val paydayTextColor = remember(isDark) {
        if (isDark) Color(0xFF94A3B8) else Color(0xFF78716C)
    }

    val darkOrb1 = remember {
        Brush.radialGradient(
            colors = listOf(Color(0xFF7C4DFF).copy(alpha = 0.25f), Color.Transparent)
        )
    }
    val darkOrb2 = remember {
        Brush.radialGradient(
            colors = listOf(Color(0xFF00F2FE).copy(alpha = 0.20f), Color.Transparent)
        )
    }
    val lightOrb1 = remember {
        Brush.radialGradient(
            colors = listOf(Color(0xFFE8A377).copy(alpha = 0.25f), Color.Transparent)
        )
    }
    val lightOrb2 = remember {
        Brush.radialGradient(
            colors = listOf(Color(0xFF487358).copy(alpha = 0.20f), Color.Transparent)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassSurface(shape = cardShape, isDark = isDark)
            .drawBehind {
                val orbRadius = size.minDimension * 0.75f
                if (isDark) {
                    drawCircle(
                        brush = darkOrb1,
                        center = Offset(size.width + 20.dp.toPx(), -20.dp.toPx()),
                        radius = orbRadius
                    )
                    drawCircle(
                        brush = darkOrb2,
                        center = Offset(-20.dp.toPx(), size.height + 20.dp.toPx()),
                        radius = orbRadius
                    )
                } else {
                    drawCircle(
                        brush = lightOrb1,
                        center = Offset(size.width + 20.dp.toPx(), -20.dp.toPx()),
                        radius = orbRadius
                    )
                    drawCircle(
                        brush = lightOrb2,
                        center = Offset(-20.dp.toPx(), size.height + 20.dp.toPx()),
                        radius = orbRadius
                    )
                }
            }
            .padding(horizontal = 24.dp, vertical = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card Title: 11sp bold, uppercase, tracked letter spacing
            Text(
                text = stringResource(R.string.safe_to_spend_today).uppercase(),
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.12.sp,
                    color = titleColor
                )
            )

            Spacer(Modifier.height(4.dp))

            // Giant Amount Display
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.testTag("safe_to_spend_amount")
            ) {
                Text(
                    text = formatTndAmount(state.remainingAllowanceMinorUnits),
                    style = TextStyle(
                        fontSize = 46.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.03).sp,
                        brush = amountBrush
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.safe_to_spend_currency),
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = currencyColor
                    ),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Tomorrow Estimate Pill
            state.tomorrowProjectionMinorUnits?.let { projectionMinorUnits ->
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .clip(pillShape)
                        .background(tomorrowFill, pillShape)
                        .border(
                            width = 0.5.dp,
                            color = tomorrowBorder,
                            shape = pillShape
                        )
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                        .testTag("safe_to_spend_tomorrow"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = tomorrowIconTint
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Tomorrow: ${formatTndAmount(projectionMinorUnits)} ${stringResource(R.string.safe_to_spend_currency)}",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = tomorrowTextColor
                            )
                        )
                    }
                }
            }

            state.paydayLabel?.let { label ->
                Spacer(Modifier.height(6.dp))
                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = paydayTextColor
                    ),
                    modifier = Modifier.testTag("safe_to_spend_payday")
                )
            }

            when (val m = state.message) {
                null -> Unit
                is SafeToSpendMessage.OverToday -> {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(
                            R.string.safe_to_spend_over_today,
                            formatTndAmount(m.overByMinorUnits),
                        ),
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = overspendColor,
                        ),
                    )
                }
                is SafeToSpendMessage.PeriodExhausted -> {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.safe_to_spend_period_exhausted),
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = overspendColor,
                        ),
                    )
                }
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
            onConfigureBudget = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
