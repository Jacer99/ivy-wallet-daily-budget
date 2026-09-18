package com.ivy.home

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ivy.base.legacy.Theme
import com.ivy.design.l0_system.UI
import com.ivy.design.utils.thenIf
import com.ivy.legacy.ivyWalletCtx
import com.ivy.legacy.utils.clickableNoIndication
import com.ivy.legacy.utils.colorLerp
import com.ivy.legacy.utils.format
import com.ivy.legacy.utils.lerp
import com.ivy.legacy.utils.navigationBarInset
import com.ivy.legacy.utils.rememberInteractionSource
import com.ivy.legacy.utils.rememberSwipeListenerState
import com.ivy.legacy.utils.springBounce
import com.ivy.legacy.utils.statusBarInset
import com.ivy.legacy.utils.toDensityPx
import com.ivy.legacy.utils.verticalSwipeListener
import com.ivy.navigation.BudgetScreen
import com.ivy.navigation.CategoriesScreen
import com.ivy.navigation.DynamicBudgetConfigScreen
import com.ivy.navigation.IvyPreview
import com.ivy.navigation.LoansScreen
import com.ivy.navigation.PlannedPaymentsScreen
import com.ivy.navigation.ReportScreen
import com.ivy.navigation.SearchScreen
import com.ivy.navigation.SettingsScreen
import com.ivy.navigation.navigation
import com.ivy.ui.R
import com.ivy.ui.component.LiquidGlassCard
import com.ivy.ui.component.pocketMoneyBackground
import com.ivy.wallet.ui.theme.components.CircleButtonFilled
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.modal.AddModalBackHandling
import java.util.UUID
import kotlin.math.roundToInt

private const val SWIPE_UP_THRESHOLD_CLOSE_MORE_MENU = 300

@Composable
fun BoxWithConstraintsScope.MoreMenu(
    expanded: Boolean,

    balance: Double,
    buffer: Double,
    currency: String,
    theme: Theme,

    setExpanded: (Boolean) -> Unit,
    onSwitchTheme: () -> Unit,
    onBufferClick: () -> Unit,
    onCurrencyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ivyContext = ivyWalletCtx()

    val percentExpanded by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = springBounce(),
        label = ""
    )
    val iconRotation by animateFloatAsState(
        targetValue = if (expanded) -180f else 0f,
        animationSpec = springBounce(),
        label = ""
    )

    val buttonSizePx = 40.dp.toDensityPx()

    val xBase = ivyContext.screenWidth - 24.dp.toDensityPx()
    val yBaseCollapsed = 20.dp.toDensityPx() + statusBarInset()
    val yBaseExpanded = ivyContext.screenHeight - 48.dp.toDensityPx() - navigationBarInset()

    val yButton = lerp(
        start = yBaseCollapsed,
        end = yBaseExpanded - buttonSizePx,
        fraction = percentExpanded
    )

    if (percentExpanded > 0.01f) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .pocketMoneyBackground()
                .clickableNoIndication(rememberInteractionSource()) {
                    // Consume click events
                }
                .statusBarsPadding()
                .navigationBarsPadding()
                .alpha(percentExpanded)
                .verticalScroll(rememberScrollState())
                .zIndex(510f)
                .verticalSwipeListener(
                    sensitivity = SWIPE_UP_THRESHOLD_CLOSE_MORE_MENU,
                    state = rememberSwipeListenerState(),
                    onSwipeUp = {
                        setExpanded(false)
                    }
                )
        ) {
            val modalId = remember {
                UUID.randomUUID()
            }

            AddModalBackHandling(
                modalId = modalId,
                visible = expanded
            ) {
                setExpanded(false)
            }

            Content(
                theme = theme,
                onSwitchTheme = onSwitchTheme,
                balance = balance,
                buffer = buffer,
                currency = currency,
                onBufferClick = onBufferClick,
                onCurrencyClick = onCurrencyClick
            )
        }
    }

    CircleButtonFilled(
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                layout(placeable.width, placeable.height) {
                    placeable.place(
                        x = xBase.roundToInt() - buttonSizePx.roundToInt(),
                        y = yButton.roundToInt()
                    )
                }
            }
            .rotate(iconRotation)
            .thenIf(expanded) {
                zIndex(520f)
            }
            .testTag("home_more_menu_arrow"),
        backgroundColor = colorLerp(UI.colors.medium, UI.colors.pure, percentExpanded),
        icon = R.drawable.ic_expandarrow
    ) {
        setExpanded(!expanded)
    }
}

@Composable
private fun ColumnScope.Content(
    balance: Double,
    buffer: Double,
    currency: String,
    theme: Theme,

    onSwitchTheme: () -> Unit,
    onBufferClick: () -> Unit,
    onCurrencyClick: () -> Unit,
) {
    Spacer(Modifier.height(24.dp))

    val nav = navigation()
    SearchButton {
        nav.navigateTo(
            screen = SearchScreen
        )
    }

    Spacer(Modifier.height(20.dp))

    QuickAccess(
        theme = theme,
        onSwitchTheme = onSwitchTheme
    )

    Spacer(Modifier.height(36.dp))

    Buffer(
        buffer = buffer,
        currency = currency,
        balance = balance,
        onBufferClick = onBufferClick
    )

    Spacer(Modifier.height(20.dp))

    OpenSource()

    Spacer(Modifier.weight(1f))
}

@Composable
private fun SearchButton(
    onClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable {
                onClick()
            },
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IvyIcon(
                icon = R.drawable.ic_search,
                tint = UI.colors.mediumInverse
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = stringResource(R.string.search_transactions),
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = UI.colors.mediumInverse
                )
            )
        }
    }
}

@Composable
private fun ColumnScope.OpenSource() {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(26.dp)
    ) {
        Text(
            text = "Built with love for my wife ❤️",
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = UI.colors.pureInverse
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}

@Composable
private fun ColumnScope.Buffer(
    buffer: Double,
    currency: String,
    balance: Double,
    onBufferClick: () -> Unit
) {
    val bufferExceeded = balance < buffer
    val leftToSpend = balance - buffer

    // Savings goal row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickableNoIndication(rememberInteractionSource()) {
                onBufferClick()
            }
            .testTag("savings_goal_row"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.savings_goal),
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = UI.colors.pureInverse
            )
        )

        Spacer(Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = buffer.format(currency),
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = UI.colors.pureInverse
                )
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = currency,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = UI.colors.mediumInverse
                ),
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    if (bufferExceeded) {
        BufferExceededCard(
            exceededAmount = kotlin.math.abs(leftToSpend),
            currency = currency,
            onClick = onBufferClick
        )
    } else {
        BufferRemainingCard(
            remainingAmount = leftToSpend,
            currency = currency,
            onClick = onBufferClick
        )
    }
}

@Composable
private fun BufferExceededCard(
    exceededAmount: Double,
    currency: String,
    onClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                width = 0.5.dp,
                color = Color(0xFFFF9F0A).copy(alpha = 0.35f),
                shape = RoundedCornerShape(26.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(26.dp),
        fillColor = Color(0xFFFF9F0A).copy(alpha = 0.14f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF9F0A).copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF9F0A)
                    )
                )
            }

            Spacer(Modifier.width(14.dp))

            Column {
                Text(
                    text = stringResource(R.string.buffer_exceeded_by),
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = UI.colors.mediumInverse
                    )
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = "${exceededAmount.format(currency)} $currency",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = UI.colors.pureInverse
                    )
                )
            }
        }
    }
}

@Composable
private fun BufferRemainingCard(
    remainingAmount: Double,
    currency: String,
    onClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(26.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981).copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                IvyIcon(
                    icon = R.drawable.ic_buffer_ok,
                    tint = Color(0xFF10B981)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column {
                Text(
                    text = stringResource(R.string.left_to_spend),
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = UI.colors.mediumInverse
                    )
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = "${remainingAmount.format(currency)} $currency",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = UI.colors.pureInverse
                    )
                )
            }
        }
    }
}

@Composable
private fun QuickAccess(
    theme: Theme,
    onSwitchTheme: () -> Unit
) {
    Column {
        val nav = navigation()

        val themeIcon = when (theme) {
            Theme.AUTO -> R.drawable.home_more_menu_auto_mode
            Theme.LIGHT -> R.drawable.home_more_menu_light_mode
            Theme.DARK, Theme.AMOLED_DARK -> R.drawable.home_more_menu_dark_mode
        }

        val themeLabel = when (theme) {
            Theme.AUTO -> "System"
            Theme.LIGHT -> stringResource(R.string.light_mode)
            Theme.DARK, Theme.AMOLED_DARK -> stringResource(R.string.dark_mode)
        }

        Text(
            modifier = Modifier.padding(start = 24.dp),
            text = stringResource(R.string.quick_access),
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = UI.colors.mediumInverse
            )
        )

        Spacer(Modifier.height(16.dp))

        // First Row: Settings, Categories, Light/Dark toggle, Planned Payments
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            QuickAccessButton(
                icon = R.drawable.home_more_menu_settings,
                label = stringResource(R.string.settings),
                onClick = { nav.navigateTo(SettingsScreen) }
            )

            QuickAccessButton(
                icon = R.drawable.home_more_menu_categories,
                label = stringResource(R.string.categories),
                onClick = { nav.navigateTo(CategoriesScreen) }
            )

            QuickAccessButton(
                icon = themeIcon,
                label = themeLabel,
                onClick = onSwitchTheme
            )

            QuickAccessButton(
                icon = R.drawable.home_more_menu_planned_payments,
                label = stringResource(R.string.planned_payments),
                onClick = { nav.navigateTo(PlannedPaymentsScreen) }
            )
        }

        Spacer(Modifier.height(16.dp))

        // Second Row: Spending budget, Reports, Budgets, Loans
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            QuickAccessButton(
                icon = R.drawable.ic_budget_xs,
                label = "Spending budget",
                onClick = { nav.navigateTo(DynamicBudgetConfigScreen) }
            )

            QuickAccessButton(
                icon = R.drawable.home_more_menu_reports,
                label = stringResource(R.string.reports),
                onClick = { nav.navigateTo(ReportScreen) }
            )

            QuickAccessButton(
                icon = R.drawable.home_more_menu_budgets,
                label = stringResource(R.string.budgets),
                onClick = { nav.navigateTo(BudgetScreen) }
            )

            QuickAccessButton(
                icon = R.drawable.home_more_menu_loans,
                label = stringResource(R.string.loans),
                onClick = { nav.navigateTo(LoansScreen) }
            )
        }
    }
}

@Composable
private fun QuickAccessButton(
    @DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(80.dp)
            .clickableNoIndication(rememberInteractionSource()) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LiquidGlassCard(
            modifier = Modifier
                .size(56.dp)
                .clickable { onClick() },
            shape = CircleShape
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                IvyIcon(
                    icon = icon,
                    tint = UI.colors.pureInverse
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = label,
            style = TextStyle(
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
                color = UI.colors.mediumInverse,
                textAlign = TextAlign.Center
            ),
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun BoxWithConstraintsScope.Preview_Expanded() {
    IvyPreview {
        MoreMenu(
            expanded = true,
            balance = 7523.43,
            buffer = 5000.0,
            currency = "BGN",
            theme = Theme.LIGHT,
            setExpanded = {
            },
            onSwitchTheme = {},
            onBufferClick = {},
            onCurrencyClick = {}
        )
    }
}

@Preview
@Composable
private fun BoxWithConstraintsScope.Preview() {
    IvyPreview {
        var expanded by remember { mutableStateOf(false) }

        MoreMenu(
            expanded = expanded,
            balance = 7523.43,
            buffer = 5000.0,
            currency = "BGN",
            theme = Theme.LIGHT,
            setExpanded = {
                expanded = it
            },
            onSwitchTheme = {},
            onBufferClick = {},
            onCurrencyClick = {}
        )
    }
}
