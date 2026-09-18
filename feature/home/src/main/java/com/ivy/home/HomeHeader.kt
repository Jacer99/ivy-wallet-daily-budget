package com.ivy.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.ivy.design.system.isAppInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.base.model.TransactionType
import com.ivy.design.api.LocalTimeConverter
import com.ivy.design.api.LocalTimeFormatter
import com.ivy.design.api.LocalTimeProvider
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.design.utils.thenIf
import com.ivy.legacy.data.model.TimePeriod
import com.ivy.legacy.ivyWalletCtx
import com.ivy.legacy.ui.component.transaction.TransactionsDividerLine
import com.ivy.legacy.utils.clickableNoIndication
import com.ivy.legacy.utils.drawColoredShadow
import com.ivy.legacy.utils.format
import com.ivy.legacy.utils.horizontalSwipeListener
import com.ivy.legacy.utils.isNotNullOrBlank
import com.ivy.legacy.utils.rememberInteractionSource
import com.ivy.legacy.utils.rememberSwipeListenerState
import com.ivy.legacy.utils.springBounce
import com.ivy.legacy.utils.verticalSwipeListener
import com.ivy.navigation.PieChartStatisticScreen
import com.ivy.navigation.navigation
import com.ivy.ui.R
import com.ivy.ui.component.LiquidGlassTokens
import com.ivy.ui.component.glassSpecularBorder
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.components.IvyOutlinedButton
import kotlin.math.absoluteValue

@ExperimentalAnimationApi
@Composable
internal fun HomeHeader(
    expanded: Boolean,
    name: String,
    period: TimePeriod,
    safeToSpend: SafeToSpendCardState,
    onShowMonthModal: () -> Unit,
    onSelectNextMonth: () -> Unit,
    onSelectPreviousMonth: () -> Unit,
) {
    Column {
        val percentExpanded by animateFloatAsState(
            targetValue = if (expanded) 1f else 0f,
            animationSpec = springBounce(
                stiffness = Spring.StiffnessLow
            ),
            label = "Home Header Expand Collapse"
        )

        Spacer(Modifier.height(20.dp))

        HeaderStickyRow(
            percentExpanded = percentExpanded,
            name = name,
            period = period,
            safeToSpend = safeToSpend,
            onShowMonthModal = onShowMonthModal,
            onSelectNextMonth = onSelectNextMonth,
            onSelectPreviousMonth = onSelectPreviousMonth,
        )

        Spacer(Modifier.height(16.dp))

        if (percentExpanded < 0.5f) {
            TransactionsDividerLine(
                modifier = Modifier.alpha(1f - percentExpanded),
                paddingHorizontal = 0.dp
            )
        }
    }
}

@Composable
private fun HeaderStickyRow(
    percentExpanded: Float,
    name: String,
    period: TimePeriod,
    safeToSpend: SafeToSpendCardState,
    onShowMonthModal: () -> Unit,
    onSelectNextMonth: () -> Unit,
    onSelectPreviousMonth: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                modifier = Modifier
                    .alpha(percentExpanded)
                    .testTag("home_greeting_text"),
                text = if (name.isNotNullOrBlank()) {
                    stringResource(
                        R.string.hi_name,
                        name,
                    )
                } else {
                    stringResource(R.string.hi)
                },
                style = UI.typo.b1.style(
                    fontWeight = FontWeight.ExtraBold,
                    color = UI.colors.pureInverse,
                ),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )

            // Collapsed Safe-to-Spend hero amount
            if (percentExpanded < 1f && safeToSpend is SafeToSpendCardState.Active) {
                Text(
                    modifier = Modifier
                        .alpha(alpha = 1f - percentExpanded)
                        .testTag("home_sticky_safe_to_spend"),
                    text = "${formatTndAmount(safeToSpend.remainingAllowanceMinorUnits)} TND",
                    style = UI.typo.b1.style(
                        fontWeight = FontWeight.ExtraBold,
                        color = UI.colors.pureInverse,
                    ),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        }

        IvyOutlinedButton(
            modifier = Modifier.horizontalSwipeListener(
                sensitivity = 75,
                state = rememberSwipeListenerState(),
                onSwipeLeft = {
                    onSelectNextMonth()
                },
                onSwipeRight = {
                    onSelectPreviousMonth()
                },
            ),
            iconStart = R.drawable.ic_calendar,
            text = period.toDisplayShort(
                startDateOfMonth = ivyWalletCtx().startDayOfMonth,
                timeConverter = LocalTimeConverter.current,
                timeProvider = LocalTimeProvider.current,
                timeFormatter = LocalTimeFormatter.current,
            ),
            minWidth = 130.dp,
        ) {
            onShowMonthModal()
        }

        Spacer(Modifier.width(12.dp))

        Spacer(Modifier.width(40.dp)) // settings menu button spacer
    }
}

@ExperimentalAnimationApi
@Composable
fun CashFlowInfo(
    currency: String,
    balance: Double,
    monthlyIncome: Double,
    monthlyExpenses: Double,
    hideBalance: Boolean,
    hideIncome: Boolean,
    onHiddenIncomeClick: () -> Unit,
    onOpenMoreMenu: () -> Unit,
    onBalanceClick: () -> Unit,
    percentExpanded: Float,
    onHiddenBalanceClick: () -> Unit,
    modifier: Modifier = Modifier,
    safeToSpendIsActive: Boolean = false,
) {
    val isDark = isAppInDarkTheme()

    Column(
        modifier = modifier
            .verticalSwipeListener(
                sensitivity = Constants.SWIPE_DOWN_THRESHOLD_OPEN_MORE_MENU,
                state = rememberSwipeListenerState(),
                onSwipeDown = {
                    onOpenMoreMenu()
                },
            ),
    ) {
        IncomeExpenses(
            percentExpanded = percentExpanded,
            currency = currency,
            monthlyIncome = monthlyIncome,
            monthlyExpenses = monthlyExpenses,
            hideIncome = hideIncome,
            onHiddenIncomeClick = onHiddenIncomeClick,
            isDark = isDark,
        )

        val cashflow = monthlyIncome - monthlyExpenses
        if (cashflow != 0.0 && !hideBalance) {
            Spacer(Modifier.height(10.dp))

            // Cashflow Sub-Indicator Pill (Centered floating frosted pill with pulsing colored dot)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                val pillShape = remember { CircleShape }
                val pillBackground = remember(isDark) {
                    if (isDark) Color(0xFF0C1322).copy(alpha = 0.82f) else Color(0xFFFBF9F4).copy(alpha = 0.88f)
                }
                val pillBorder = remember(isDark) {
                    if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE8DFCF)
                }
                val dotColor = remember(cashflow, isDark) {
                    if (cashflow < 0) {
                        if (isDark) Color(0xFFF43F5E) else Color(0xFFC2532F)
                    } else {
                        if (isDark) Color(0xFF00F2FE) else Color(0xFF2D5540)
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(pillShape)
                        .background(pillBackground, pillShape)
                        .border(1.dp, pillBorder, pillShape)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(dotColor, pillShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Cashflow: ",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF57534E)
                        )
                    )
                    Text(
                        text = "${if (cashflow > 0) "+" else ""}${cashflow.format(currency)} $currency",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (cashflow < 0) {
                                if (isDark) Color(0xFFFB7185) else Color(0xFFC2532F)
                            } else {
                                if (isDark) Color(0xFF67E8F9) else Color(0xFF2D5540)
                            }
                        )
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
        } else {
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun IncomeExpenses(
    percentExpanded: Float,
    currency: String,
    monthlyIncome: Double,
    monthlyExpenses: Double,
    hideIncome: Boolean,
    onHiddenIncomeClick: () -> Unit,
    isDark: Boolean,
) {
    val nav = navigation()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Twin Income Card
        TwinCashflowCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.income),
            amount = monthlyIncome,
            currency = currency,
            isIncome = true,
            isDark = isDark,
            testTag = "home_card_income",
            onClick = {
                if (hideIncome) {
                    onHiddenIncomeClick()
                } else {
                    nav.navigateTo(PieChartStatisticScreen(type = TransactionType.INCOME))
                }
            }
        )

        // Twin Expense Card
        TwinCashflowCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.expenses),
            amount = monthlyExpenses.absoluteValue,
            currency = currency,
            isIncome = false,
            isDark = isDark,
            testTag = "home_card_expense",
            onClick = {
                nav.navigateTo(PieChartStatisticScreen(type = TransactionType.EXPENSE))
            }
        )
    }
}

@Composable
private fun TwinCashflowCard(
    label: String,
    amount: Double,
    currency: String,
    isIncome: Boolean,
    isDark: Boolean,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val cardShape = remember { RoundedCornerShape(18.dp) }
    val circleShape = remember { CircleShape }
    val backgroundBrush = remember(isIncome, isDark) {
        if (isIncome) {
            if (isDark) LiquidGlassTokens.DarkIncomeGradient else LiquidGlassTokens.LightIncomeGradient
        } else {
            if (isDark) LiquidGlassTokens.DarkExpenseGradient else LiquidGlassTokens.LightExpenseGradient
        }
    }
    val borderColor = remember(isIncome, isDark) {
        if (isIncome) {
            if (isDark) Color(0xFF00F2FE).copy(alpha = 0.30f) else Color(0xFF30D158).copy(alpha = 0.35f)
        } else {
            if (isDark) Color(0xFFD946EF).copy(alpha = 0.30f) else Color(0xFFFF453A).copy(alpha = 0.35f)
        }
    }
    val labelColor = remember(isIncome, isDark) {
        if (isIncome) {
            if (isDark) Color(0xFFCFFAFE) else Color(0xFFE2F3E7)
        } else {
            if (isDark) Color(0xFFE2E8F0) else Color(0xFFFCEBE7)
        }
    }
    val amountColor = remember(isIncome, isDark) {
        if (isIncome) {
            if (isDark) Color(0xFFA5F3FC) else Color.White
        } else {
            if (isDark) Color(0xFFFECDD3) else Color.White
        }
    }
    val currencyColor = remember(isIncome, isDark) {
        if (isIncome) {
            if (isDark) Color(0xFF67E8F9) else Color(0xFFC0DFC9)
        } else {
            if (isDark) Color(0xFFFDA4AF) else Color(0xFFF0CBC0)
        }
    }
    val subLabelColor = remember(isIncome, isDark) {
        if (isIncome) {
            if (isDark) Color(0xFF22D3EE) else Color(0xFFA1CBB0)
        } else {
            if (isDark) Color(0xFFE879F9) else Color(0xFFE5A898)
        }
    }
    val badgeBg = remember(isIncome, isDark) {
        if (isIncome) {
            if (isDark) Color(0xFF00F2FE).copy(alpha = 0.20f) else Color.White.copy(alpha = 0.18f)
        } else {
            if (isDark) Color(0xFFF43F5E).copy(alpha = 0.20f) else Color.White.copy(alpha = 0.15f)
        }
    }
    val badgeTint = remember(isIncome, isDark) {
        if (isIncome) {
            if (isDark) Color(0xFF67E8F9) else Color.White
        } else {
            if (isDark) Color(0xFFFB7185) else Color.White
        }
    }

    Column(
        modifier = modifier
            .clip(cardShape)
            .background(backgroundBrush, cardShape)
            .border(1.dp, borderColor, cardShape)
            .testTag(testTag)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Row: Label and Circular Translucent Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = labelColor
                )
            )

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(circleShape)
                    .background(badgeBg, circleShape)
                    .thenIf(isDark) {
                        border(
                            1.dp,
                            if (isIncome) Color(0xFF00F2FE).copy(alpha = 0.40f) else Color(0xFFF43F5E).copy(alpha = 0.40f),
                            circleShape
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = badgeTint,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Bottom Section: Amount, Currency, and "THIS MONTH"
        Column {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = amount.format(currency),
                    style = TextStyle(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = amountColor,
                        letterSpacing = (-0.02).sp
                    )
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = currency,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = currencyColor
                    ),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(Modifier.height(3.dp))

            Text(
                text = "THIS MONTH",
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.08.sp,
                    color = subLabelColor
                )
            )
        }
    }
}
