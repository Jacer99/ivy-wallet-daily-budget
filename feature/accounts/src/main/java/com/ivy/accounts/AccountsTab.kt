package com.ivy.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.base.legacy.Theme
import com.ivy.data.model.Account
import com.ivy.data.model.AccountId
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.design.system.isAppInDarkTheme
import com.ivy.legacy.IvyWalletPreview
import com.ivy.legacy.data.model.AccountData
import com.ivy.legacy.utils.clickableNoIndication
import com.ivy.legacy.utils.format
import com.ivy.legacy.utils.horizontalSwipeListener
import com.ivy.legacy.utils.rememberInteractionSource
import com.ivy.legacy.utils.rememberSwipeListenerState
import com.ivy.navigation.TransactionsScreen
import com.ivy.navigation.navigation
import com.ivy.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.component.LiquidGlassCard
import com.ivy.ui.component.LiquidGlassTokens
import com.ivy.ui.component.atmosphericMeshBackground
import com.ivy.ui.component.glassSurface
import com.ivy.ui.component.pocketMoneyBackground
import com.ivy.ui.component.specularBorder
import com.ivy.ui.rememberScrollPositionListState
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.GreenLight
import com.ivy.wallet.ui.theme.components.BalanceRow
import com.ivy.wallet.ui.theme.components.BalanceRowMini
import com.ivy.wallet.ui.theme.components.ItemIconSDefaultIcon
import com.ivy.wallet.ui.theme.components.ReorderButton
import com.ivy.wallet.ui.theme.components.ReorderModalSingleType
import com.ivy.wallet.ui.theme.dynamicContrast
import com.ivy.wallet.ui.theme.findContrastTextColor
import com.ivy.wallet.ui.theme.toComposeColor
import kotlinx.collections.immutable.persistentListOf
import java.util.UUID
import kotlin.math.absoluteValue

@Composable
fun BoxWithConstraintsScope.AccountsTab() {
    val viewModel: AccountsViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()

    UI(
        state = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun BoxWithConstraintsScope.UI(
    state: AccountsState,
    onEvent: (AccountsEvent) -> Unit = {}
) {
    val nav = navigation()
    val isDark = isAppInDarkTheme()
    val ivyContext = com.ivy.legacy.ivyWalletCtx()
    var listState = rememberLazyListState()
    if (!state.accountsData.isEmpty()) {
        listState = rememberScrollPositionListState(
            key = "accounts_lazy_column",
            initialFirstVisibleItemIndex = ivyContext.accountsListState?.firstVisibleItemIndex ?: 0,
            initialFirstVisibleItemScrollOffset = ivyContext.accountsListState?.firstVisibleItemScrollOffset
                ?: 0
        )
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .pocketMoneyBackground(isDark = isDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .horizontalSwipeListener(
                sensitivity = 200,
                state = rememberSwipeListenerState(),
                onSwipeLeft = {
                    ivyContext.selectMainTab(com.ivy.legacy.data.model.MainTab.HOME)
                },
                onSwipeRight = {
                    ivyContext.selectMainTab(com.ivy.legacy.data.model.MainTab.HOME)
                }
            ),
        state = listState
    ) {
        item {
            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.accounts),
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = UI.colors.pureInverse
                    )
                )

                Spacer(Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.65f),
                            CircleShape
                        )
                        .specularBorder(shape = CircleShape, isDark = isDark, strokeWidth = 0.5.dp)
                        .clickable {
                            onEvent(
                                AccountsEvent.OnReorderModalVisible(reorderVisible = true)
                            )
                        }
                        .testTag("accounts_menu_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_reorder),
                        contentDescription = "Menu",
                        tint = UI.colors.pureInverse,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if (!state.hideTotalBalance) {
                Spacer(Modifier.height(16.dp))
                TotalBalanceSplitCard(
                    baseCurrency = state.baseCurrency,
                    totalWithoutExcluded = state.totalBalanceWithoutExcluded.toDoubleOrNull() ?: 0.00,
                    totalWithExcluded = state.totalBalanceWithExcluded.toDoubleOrNull() ?: 0.00,
                    isDark = isDark,
                )
                Spacer(Modifier.height(16.dp))
            }
        }
        items(state.accountsData) {
            Spacer(Modifier.height(16.dp))
            AccountCard(
                baseCurrency = state.baseCurrency,
                accountData = it,
                compactModeEnabled = state.compactAccountsModeEnabled,
                isDark = isDark,
                onBalanceClick = {
                    nav.navigateTo(
                        TransactionsScreen(
                            accountId = it.account.id.value,
                            categoryId = null
                        )
                    )
                }
            ) {
                nav.navigateTo(
                    TransactionsScreen(
                        accountId = it.account.id.value,
                        categoryId = null
                    )
                )
            }
        }

        item {
            Spacer(Modifier.height(150.dp)) // scroll hack
        }
    }

    ReorderModalSingleType(
        visible = state.reorderVisible,
        initialItems = state.accountsData,
        dismiss = {
            onEvent(AccountsEvent.OnReorderModalVisible(reorderVisible = false))
        },
        onReordered = {
            onEvent(AccountsEvent.OnReorder(reorderedList = it))
        }
    ) { _, item ->
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 24.dp)
                .padding(vertical = 8.dp),
            text = item.account.name.value,
            style = UI.typo.b1.style(
                color = item.account.color.value.toComposeColor(),
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun TotalBalanceSplitCard(
    baseCurrency: String,
    totalWithoutExcluded: Double,
    totalWithExcluded: Double,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    LiquidGlassCard(
        shape = RoundedCornerShape(24.dp),
        isDark = isDark,
        strokeWidth = 0.5.dp,
        modifier = modifier
            .padding(horizontal = 22.dp)
            .fillMaxWidth()
            .testTag("accounts_total_balance_split_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Total Balance
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.total_balance).uppercase(),
                    style = TextStyle(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = UI.colors.mediumInverse
                    )
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = totalWithoutExcluded.format(baseCurrency),
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = UI.colors.pureInverse,
                            letterSpacing = (-0.02).sp
                        )
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = baseCurrency,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = UI.colors.mediumInverse
                        ),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            // 0.5dp vertical frosted divider line
            Box(
                modifier = Modifier
                    .width(0.5.dp)
                    .height(44.dp)
                    .background(
                        if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.10f)
                    )
            )

            Spacer(Modifier.width(16.dp))

            // Right: Total Balance Excluded
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.total_balance_excluded).uppercase(),
                    style = TextStyle(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = UI.colors.mediumInverse
                    )
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = totalWithExcluded.format(baseCurrency),
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = UI.colors.pureInverse,
                            letterSpacing = (-0.02).sp
                        )
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = baseCurrency,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = UI.colors.mediumInverse
                        ),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountCard(
    baseCurrency: String,
    accountData: AccountData,
    compactModeEnabled: Boolean,
    isDark: Boolean,
    onBalanceClick: () -> Unit,
    onClick: () -> Unit,
) {
    val account = accountData.account
    val currency = account.asset.code
    val accountName = account.name.value
    val iconId = account.icon?.id ?: ""

    val isCash = accountName.contains("cash", ignoreCase = true) || iconId.contains("cash", ignoreCase = true)
    val isRevolut = accountName.contains("revolut", ignoreCase = true) || iconId.contains("revolut", ignoreCase = true)
    val isBank = accountName.contains("bank", ignoreCase = true) || accountName.contains("dsk", ignoreCase = true) || accountName.contains("phyre", ignoreCase = true) || (!isCash && !isRevolut)

    val (tintFillColor, customBorderColor, vaultLabel) = when {
        isCash -> Triple(
            Color(0xFF30D158).copy(alpha = if (isDark) 0.14f else 0.10f),
            Color(0xFF30D158).copy(alpha = 0.32f),
            "CASH VAULT"
        )
        isRevolut -> Triple(
            Color(0xFF40C8FF).copy(alpha = if (isDark) 0.12f else 0.10f),
            Color(0xFF40C8FF).copy(alpha = 0.30f),
            "REVOLUT VAULT"
        )
        else -> Triple(
            Color(0xFF8A6BFF).copy(alpha = if (isDark) 0.16f else 0.12f),
            Color(0xFF8A6BFF).copy(alpha = 0.35f),
            "BANK VAULT"
        )
    }

    val cardShape = RoundedCornerShape(32.dp)

    LiquidGlassCard(
        shape = cardShape,
        isDark = isDark,
        fillColor = tintFillColor,
        strokeWidth = 0.5.dp,
        modifier = Modifier
            .padding(horizontal = 22.dp)
            .fillMaxWidth()
            .border(0.5.dp, customBorderColor, cardShape)
            .clickable(onClick = onClick)
            .testTag("account_card_${account.name.value}")
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            // Top Row: Icon + Name + Indicator Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.50f),
                                CircleShape
                            )
                            .specularBorder(shape = CircleShape, isDark = isDark, strokeWidth = 0.5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ItemIconSDefaultIcon(
                            iconName = account.icon?.id,
                            defaultIcon = R.drawable.ic_custom_account_s,
                            tint = UI.colors.pureInverse
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = account.name.value,
                            style = TextStyle(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = UI.colors.pureInverse
                            )
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = vaultLabel,
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp,
                                color = UI.colors.mediumInverse
                            )
                        )
                    }
                }

                // Indicator Pill
                val (pillDotColor, pillText, pillTextColor) = when {
                    accountData.balance < 0 -> Triple(
                        Color(0xFFF43F5E),
                        "Deficit",
                        if (isDark) Color(0xFFFECDD3) else Color(0xFFE11D48)
                    )
                    !account.includeInBalance -> Triple(
                        Color(0xFFF59E0B),
                        stringResource(R.string.excluded),
                        if (isDark) Color(0xFFFDE68A) else Color(0xFFD97706)
                    )
                    isCash -> Triple(
                        Color(0xFF30D158),
                        "Vault",
                        if (isDark) Color(0xFFA7F3D0) else Color(0xFF059669)
                    )
                    isRevolut -> Triple(
                        Color(0xFF40C8FF),
                        "Active",
                        if (isDark) Color(0xFFCFFAFE) else Color(0xFF0284C7)
                    )
                    else -> Triple(
                        Color(0xFF8A6BFF),
                        "Active",
                        if (isDark) Color(0xFFDDD6FE) else Color(0xFF7C3AED)
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.50f),
                            CircleShape
                        )
                        .specularBorder(shape = CircleShape, isDark = isDark, strokeWidth = 0.5.dp)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(pillDotColor, CircleShape)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = pillText,
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = pillTextColor
                        )
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Large Balance Display (34sp extra bold)
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.clickableNoIndication(rememberInteractionSource()) {
                    onBalanceClick()
                }
            ) {
                Text(
                    text = accountData.balance.format(currency),
                    style = TextStyle(
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = UI.colors.pureInverse,
                        letterSpacing = (-0.02).sp
                    )
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = currency,
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = UI.colors.mediumInverse
                    ),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (currency != baseCurrency && accountData.balanceBaseCurrency != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "≈ ${accountData.balanceBaseCurrency!!.format(baseCurrency)} $baseCurrency",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = UI.colors.mediumInverse
                    ),
                    modifier = Modifier.testTag("baseCurrencyEquivalent")
                )
            }

            // 0.5dp translucent horizontal divider line
            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(
                        if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.10f)
                    )
            )

            // Split sub-metrics row: "INCOME" and "EXPENSES" monthly totals with 10.5sp bold uppercase labels
            if (!compactModeEnabled) {
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Income
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.income).uppercase(),
                            style = TextStyle(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = UI.colors.mediumInverse
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "+${accountData.monthlyIncome.format(currency)} $currency",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF30D158) else Color(0xFF1E823E)
                            )
                        )
                    }

                    // Frosted Divider
                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .height(26.dp)
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.10f)
                            )
                    )

                    Spacer(Modifier.width(16.dp))

                    // Expenses
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.expenses).uppercase(),
                            style = TextStyle(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = UI.colors.mediumInverse
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "-${accountData.monthlyExpenses.absoluteValue.format(currency)} $currency",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFFFF453A) else Color(0xFFD92D20)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewAccountsTabCompactModeDisabled(theme: Theme = Theme.LIGHT) {
    IvyWalletPreview(theme = theme) {
        val acc1 = Account(
            id = AccountId(UUID.randomUUID()),
            name = NotBlankTrimmedString.unsafe("Phyre"),
            color = ColorInt(Green.toArgb()),
            asset = AssetCode.unsafe("USD"),
            icon = null,
            includeInBalance = true,
            orderNum = 0.0,
        )

        val acc2 = Account(
            id = AccountId(UUID.randomUUID()),
            name = NotBlankTrimmedString.unsafe("DSK"),
            color = ColorInt(GreenLight.toArgb()),
            asset = AssetCode.unsafe("USD"),
            icon = null,
            includeInBalance = true,
            orderNum = 0.0,
        )

        val acc3 = Account(
            id = AccountId(UUID.randomUUID()),
            name = NotBlankTrimmedString.unsafe("Revolut"),
            color = ColorInt(Green.toArgb()),
            asset = AssetCode.unsafe("USD"),
            icon = IconAsset.unsafe("revolut"),
            includeInBalance = true,
            orderNum = 0.0,
        )

        val acc4 = Account(
            id = AccountId(UUID.randomUUID()),
            name = NotBlankTrimmedString.unsafe("Cash"),
            color = ColorInt(Green.toArgb()),
            asset = AssetCode.unsafe("USD"),
            icon = IconAsset.unsafe("cash"),
            includeInBalance = true,
            orderNum = 0.0,
        )
        val state = AccountsState(
            baseCurrency = "BGN",
            accountsData = persistentListOf(
                AccountData(
                    account = acc1,
                    balance = 2125.0,
                    balanceBaseCurrency = null,
                    monthlyExpenses = 920.0,
                    monthlyIncome = 3045.0
                ),
                AccountData(
                    account = acc2,
                    balance = 12125.21,
                    balanceBaseCurrency = null,
                    monthlyExpenses = 1350.50,
                    monthlyIncome = 8000.48
                ),
                AccountData(
                    account = acc3,
                    balance = 1200.0,
                    balanceBaseCurrency = 1979.64,
                    monthlyExpenses = 750.0,
                    monthlyIncome = 1000.30
                ),
                AccountData(
                    account = acc4,
                    balance = 820.0,
                    balanceBaseCurrency = null,
                    monthlyExpenses = 340.0,
                    monthlyIncome = 400.0
                ),
            ),
            totalBalanceWithExcluded = "25.54",
            totalBalanceWithExcludedText = "BGN 25.54",
            totalBalanceWithoutExcluded = "25.54",
            totalBalanceWithoutExcludedText = "BGN 25.54",
            reorderVisible = false,
            compactAccountsModeEnabled = false,
            hideTotalBalance = false
        )
        UI(state = state)
    }
}

@Preview
@Composable
private fun PreviewAccountsTabCompactModeEnabled(theme: Theme = Theme.LIGHT) {
    IvyWalletPreview(theme = theme) {
        val acc1 = Account(
            id = AccountId(UUID.randomUUID()),
            name = NotBlankTrimmedString.unsafe("Phyre"),
            color = ColorInt(Green.toArgb()),
            asset = AssetCode.unsafe("USD"),
            icon = null,
            includeInBalance = true,
            orderNum = 0.0,
        )

        val acc2 = Account(
            id = AccountId(UUID.randomUUID()),
            name = NotBlankTrimmedString.unsafe("DSK"),
            color = ColorInt(GreenLight.toArgb()),
            asset = AssetCode.unsafe("USD"),
            icon = null,
            includeInBalance = true,
            orderNum = 0.0,
        )

        val acc3 = Account(
            id = AccountId(UUID.randomUUID()),
            name = NotBlankTrimmedString.unsafe("Revolut"),
            color = ColorInt(Green.toArgb()),
            asset = AssetCode.unsafe("USD"),
            icon = IconAsset.unsafe("revolut"),
            includeInBalance = true,
            orderNum = 0.0,
        )

        val acc4 = Account(
            id = AccountId(UUID.randomUUID()),
            name = NotBlankTrimmedString.unsafe("Cash"),
            color = ColorInt(Green.toArgb()),
            asset = AssetCode.unsafe("USD"),
            icon = IconAsset.unsafe("cash"),
            includeInBalance = true,
            orderNum = 0.0,
        )
        val state = AccountsState(
            baseCurrency = "BGN",
            accountsData = persistentListOf(
                AccountData(
                    account = acc1,
                    balance = 2125.0,
                    balanceBaseCurrency = null,
                    monthlyExpenses = 920.0,
                    monthlyIncome = 3045.0
                ),
                AccountData(
                    account = acc2,
                    balance = 12125.21,
                    balanceBaseCurrency = null,
                    monthlyExpenses = 1350.50,
                    monthlyIncome = 8000.48
                ),
                AccountData(
                    account = acc3,
                    balance = 1200.0,
                    balanceBaseCurrency = 1979.64,
                    monthlyExpenses = 750.0,
                    monthlyIncome = 1000.30
                ),
                AccountData(
                    account = acc4,
                    balance = 820.0,
                    balanceBaseCurrency = null,
                    monthlyExpenses = 340.0,
                    monthlyIncome = 400.0
                ),
            ),
            totalBalanceWithExcluded = "25.54",
            totalBalanceWithExcludedText = "BGN 25.54",
            totalBalanceWithoutExcluded = "25.54",
            totalBalanceWithoutExcludedText = "BGN 25.54",
            reorderVisible = false,
            compactAccountsModeEnabled = true,
            hideTotalBalance = false
        )
        UI(state = state)
    }
}

/** For screen shot testing **/
@Composable
fun AccountsTabNonCompactUITest(dark: Boolean) {
    val theme = when (dark) {
        true -> Theme.DARK
        false -> Theme.LIGHT
    }
    PreviewAccountsTabCompactModeDisabled(theme)
}

/** For screen shot testing **/
@Composable
fun AccountsTabCompactUITest(dark: Boolean) {
    val theme = when (dark) {
        true -> Theme.DARK
        false -> Theme.LIGHT
    }
    PreviewAccountsTabCompactModeEnabled(theme)
}