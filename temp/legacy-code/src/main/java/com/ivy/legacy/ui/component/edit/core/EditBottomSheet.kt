package com.ivy.wallet.ui.edit.core

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.base.model.TransactionType
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.design.system.isAppInDarkTheme
import com.ivy.design.utils.thenIf
import com.ivy.frp.test.TestingContext
import com.ivy.legacy.IvyWalletPreview
import com.ivy.legacy.datamodel.Account
import com.ivy.legacy.ivyWalletCtx
import com.ivy.legacy.utils.addKeyboardListener
import com.ivy.legacy.utils.clickableNoIndication
import com.ivy.legacy.utils.consumeClicks
import com.ivy.legacy.utils.densityScope
import com.ivy.legacy.utils.format
import com.ivy.legacy.utils.hideKeyboard
import com.ivy.legacy.utils.keyboardOnlyWindowInsets
import com.ivy.legacy.utils.lerp
import com.ivy.legacy.utils.navigationBarInsets
import com.ivy.legacy.utils.onScreenStart
import com.ivy.legacy.utils.rememberInteractionSource
import com.ivy.legacy.utils.rememberSwipeListenerState
import com.ivy.legacy.utils.springBounce
import com.ivy.legacy.utils.verticalSwipeListener
import com.ivy.ui.R
import com.ivy.ui.component.LiquidGlassTokens
import com.ivy.ui.component.specularBorder
import com.ivy.wallet.domain.data.IvyCurrency
import com.ivy.wallet.ui.theme.Gradient
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.GreenDark
import com.ivy.wallet.ui.theme.GreenLight
import com.ivy.wallet.ui.theme.Ivy
import com.ivy.wallet.ui.theme.IvyDark
import com.ivy.wallet.ui.theme.components.ActionsRow
import com.ivy.wallet.ui.theme.components.BalanceRow
import com.ivy.wallet.ui.theme.components.CircleButton
import com.ivy.wallet.ui.theme.components.ItemIconSDefaultIcon
import com.ivy.wallet.ui.theme.components.IvyButton
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.findContrastTextColor
import com.ivy.wallet.ui.theme.modal.DURATION_MODAL_ANIM
import com.ivy.wallet.ui.theme.modal.ModalSave
import com.ivy.wallet.ui.theme.modal.ModalSet
import com.ivy.wallet.ui.theme.modal.edit.AmountModal
import com.ivy.wallet.ui.theme.toComposeColor
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

@Deprecated("Old design system. Use `:ivy-design` and Material3")
const val SWIPE_UP_EXPANDED_THRESHOLD = 200

@Suppress("LongMethod", "LongParameterList", "UnusedParameter", "ParameterNaming")
@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun BoxWithConstraintsScope.EditBottomSheet(
    initialTransactionId: UUID?,
    type: TransactionType,
    accounts: List<Account>,
    selectedAccount: Account?,
    toAccount: Account?,
    amount: Double,
    currency: String,
    amountModalShown: Boolean,
    setAmountModalShown: (Boolean) -> Unit,
    ActionButton: @Composable () -> Unit,
    onAmountChanged: (Double) -> Unit,
    onSelectedAccountChanged: (Account) -> Unit,
    onToAccountChanged: (Account) -> Unit,
    onAddNewAccount: () -> Unit,
    modifier: Modifier = Modifier,
    convertedAmount: Double? = null,
    convertedAmountCurrencyCode: String? = null,
) {
    val isDark = isAppInDarkTheme()
    val rootView = LocalView.current
    var keyboardShown by remember { mutableStateOf(false) }

    onScreenStart {
        rootView.addKeyboardListener {
            keyboardShown = it
        }
    }

    val keyboardShownInsetDp by animateDpAsState(
        targetValue = densityScope {
            if (keyboardShown) keyboardOnlyWindowInsets().bottom.toDp() else 0.dp
        },
        animationSpec = tween(DURATION_MODAL_ANIM)
    )
    val navBarPadding by animateDpAsState(
        targetValue = densityScope {
            if (keyboardShown) 0.dp else navigationBarInsets().bottom.toDp()
        },
        animationSpec = tween(DURATION_MODAL_ANIM)
    )

    var bottomBarHeight by remember { mutableIntStateOf(0) }

    var internalExpanded by remember { mutableStateOf(true) }
    val expanded = internalExpanded && !keyboardShown

    val percentExpanded by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = springBounce()
    )
    val percentCollapsed = 1f - percentExpanded

    val showConvertedAmountText by remember(convertedAmount) {
        if (type == TransactionType.TRANSFER && convertedAmount != null && convertedAmountCurrencyCode != null) {
            mutableStateOf(
                "${
                    convertedAmount.format(
                        IvyCurrency.getDecimalPlaces(
                            convertedAmountCurrencyCode
                        )
                    )
                } $convertedAmountCurrencyCode"
            )
        } else {
            mutableStateOf(null)
        }
    }

    val sheetShape = remember { RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp) }
    val sheetBg = remember(isDark) {
        if (isDark) Color(0xFF0B0B14).copy(alpha = 0.94f) else Color(0xFFFFFFFF).copy(alpha = 0.94f)
    }

    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 24.dp)
            .clip(sheetShape)
            .background(sheetBg, sheetShape)
            .specularBorder(sheetShape, isDark, 0.5.dp)
            .verticalSwipeListener(
                sensitivity = SWIPE_UP_EXPANDED_THRESHOLD,
                state = rememberSwipeListenerState(),
                onSwipeUp = {
                    hideKeyboard(rootView)
                    internalExpanded = true
                },
                onSwipeDown = {
                    internalExpanded = false
                }
            )
            .consumeClicks(rememberInteractionSource())
    ) {
        val label = when (type) {
            TransactionType.INCOME -> stringResource(R.string.add_money_to)
            TransactionType.EXPENSE -> stringResource(R.string.pay_with)
            TransactionType.TRANSFER -> stringResource(R.string.from)
        }

        SheetHeader(
            percentExpanded = percentExpanded,
            label = label,
            type = type,
            accounts = accounts,
            selectedAccount = selectedAccount,
            toAccount = toAccount,
            onSelectedAccountChanged = onSelectedAccountChanged,
            onToAccountChanged = onToAccountChanged,
            onAddNewAccount = onAddNewAccount
        )

        val spacerAboveAmount = lerp(40, 16, percentCollapsed)
        Spacer(Modifier.height(spacerAboveAmount.dp))

        if (type == TransactionType.TRANSFER && percentExpanded < 1f) {
            TransferRowMini(
                percentCollapsed = percentCollapsed,
                fromAccount = selectedAccount,
                toAccount = toAccount,
                onSetExpanded = {
                    internalExpanded = true
                }
            )
        }

        Amount(
            type = type,
            amount = amount,
            currency = currency,
            label = label,
            account = selectedAccount,
            showConvertedAmountText = showConvertedAmountText,
            percentExpanded = percentExpanded,
            onShowAmountModal = {
                setAmountModalShown(true)
            },
            onAccountMiniClick = {
                hideKeyboard(rootView)
                internalExpanded = true
            },
        )

        val lastSpacer = lerp(20f, 8f, percentCollapsed)
        if (lastSpacer > 0) {
            Spacer(Modifier.height(lastSpacer.dp))
        }

        Spacer(Modifier.height(densityScope { bottomBarHeight.toDp() }))
        Spacer(Modifier.height(keyboardShownInsetDp))
    }

    BottomBar(
        keyboardShown = keyboardShown,
        expanded = expanded,
        internalExpanded = internalExpanded,
        setInternalExpanded = {
            internalExpanded = it
        },
        setBottomBarHeight = {
            bottomBarHeight = it
        },

        keyboardShownInsetDp = keyboardShownInsetDp,
        navBarPadding = navBarPadding,

        ActionButton = ActionButton
    )

    val amountModalId = remember(initialTransactionId, amount) {
        UUID.randomUUID()
    }
    AmountModal(
        id = amountModalId,
        visible = amountModalShown,
        currency = currency,
        initialAmount = amount.takeIf { it > 0 },
        Header = {
            Spacer(Modifier.height(24.dp))

            Text(
                modifier = Modifier.padding(start = 32.dp),
                text = stringResource(R.string.account),
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = UI.colors.pureInverse
                )
            )

            Spacer(Modifier.height(16.dp))

            AccountsRow(
                accounts = accounts,
                selectedAccount = selectedAccount,
                onSelectedAccountChanged = onSelectedAccountChanged,
                onAddNewAccount = onAddNewAccount,
                childrenTestTag = "amount_modal_account"
            )
        },
        amountSpacerTop = 48.dp,
        dismiss = {
            setAmountModalShown(false)
        }
    ) {
        onAmountChanged(it)
    }
}

@Composable
private fun BottomBar(
    keyboardShown: Boolean,
    keyboardShownInsetDp: Dp,
    setBottomBarHeight: (Int) -> Unit,
    expanded: Boolean,
    internalExpanded: Boolean,
    setInternalExpanded: (Boolean) -> Unit,
    navBarPadding: Dp,
    ActionButton: @Composable () -> Unit
) {
    val ivyContext = ivyWalletCtx()

    ActionsRow(
        modifier = Modifier
            .onSizeChanged {
                setBottomBarHeight(it.height)
            }
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                val systemOffsetBottom = keyboardShownInsetDp.toPx()
                val visibleHeight = placeable.height * 1f
                val y = ivyContext.screenHeight - visibleHeight - systemOffsetBottom

                layout(placeable.width, placeable.height) {
                    placeable.place(
                        0,
                        y.roundToInt()
                    )
                }
            }
            .padding(bottom = 12.dp)
            .padding(bottom = navBarPadding),
        lineColor = UI.colors.medium
    ) {
        Spacer(Modifier.width(24.dp))

        val expandRotation by animateFloatAsState(
            targetValue = if (expanded) 0f else -180f,
            animationSpec = springBounce()
        )

        val rootView = LocalView.current
        CircleButton(
            modifier = Modifier.rotate(expandRotation),
            icon = R.drawable.ic_expand_more,
        ) {
            setInternalExpanded(!internalExpanded || keyboardShown)
            hideKeyboard(rootView)
        }

        Spacer(Modifier.weight(1f))

        ActionButton()

        Spacer(Modifier.width(24.dp))
    }
}

@Composable
@Suppress("ParameterNaming", "MultipleEmitters")
private fun TransferRowMini(
    percentCollapsed: Float,
    fromAccount: Account?,
    toAccount: Account?,
    onSetExpanded: () -> Unit
) {
    Row(
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                val height = placeable.height * (percentCollapsed)

                layout(placeable.width, height.roundToInt()) {
                    placeable.placeRelative(
                        x = 0,
                        y = 0
                    )
                }
            }
            .alpha(percentCollapsed)
            .clickableNoIndication(rememberInteractionSource()) {
                onSetExpanded()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(24.dp))

        val fromColor = fromAccount?.color?.toComposeColor() ?: Ivy
        val fromContrastColor = findContrastTextColor(fromColor)
        IvyButton(
            text = fromAccount?.name ?: "Null",
            iconStart = R.drawable.ic_accounts,
            backgroundGradient = Gradient.solid(fromColor),
            iconTint = fromContrastColor,
            textStyle = UI.typo.b2.style(
                color = fromContrastColor,
                fontWeight = FontWeight.ExtraBold
            ),
            padding = 10.dp,
        ) {
            onSetExpanded()
        }

        IvyIcon(
            icon = R.drawable.ic_arrow_right,
            tint = UI.colors.pureInverse
        )

        val toColor = toAccount?.color?.toComposeColor() ?: Ivy
        val toContrastColor = findContrastTextColor(toColor)
        IvyButton(
            text = toAccount?.name ?: "Null",
            iconStart = R.drawable.ic_accounts,
            backgroundGradient = Gradient.solid(toColor),
            iconTint = toContrastColor,
            textStyle = UI.typo.b2.style(
                color = toContrastColor,
                fontWeight = FontWeight.ExtraBold
            ),
            padding = 10.dp,
        ) {
            onSetExpanded()
        }
    }

    val transferMiniBottomSpacer = 20 * percentCollapsed
    if (transferMiniBottomSpacer > 0f) {
        Spacer(modifier = Modifier.height(transferMiniBottomSpacer.dp))
    }
}

@Composable
@Suppress("ParameterNaming")
private fun SheetHeader(
    percentExpanded: Float,
    label: String,
    type: TransactionType,
    accounts: List<Account>,
    selectedAccount: Account?,
    toAccount: Account?,
    onSelectedAccountChanged: (Account) -> Unit,
    onToAccountChanged: (Account) -> Unit,
    onAddNewAccount: () -> Unit,
) {
    if (percentExpanded > 0.01f) {
        Column(
            modifier = Modifier
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val height = placeable.height * percentExpanded

                    layout(placeable.width, height.roundToInt()) {
                        placeable.placeRelative(
                            x = 0,
                            y = -(height * (1f - percentExpanded)).roundToInt(),
                        )
                    }
                }
                .alpha(percentExpanded)
        ) {
            Spacer(Modifier.height(32.dp))

            Text(
                modifier = Modifier.padding(start = 32.dp),
                text = label,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = UI.colors.pureInverse
                )
            )

            Spacer(Modifier.height(if (type == TransactionType.TRANSFER) 8.dp else 16.dp))

            AccountsRow(
                accounts = accounts,
                selectedAccount = selectedAccount,
                onSelectedAccountChanged = onSelectedAccountChanged,
                onAddNewAccount = onAddNewAccount,
                childrenTestTag = "from_account"
            )

            if (type == TransactionType.TRANSFER) {
                Spacer(Modifier.height(24.dp))

                Text(
                    modifier = Modifier.padding(start = 32.dp),
                    text = stringResource(R.string.to),
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = UI.colors.pureInverse
                    )
                )

                Spacer(Modifier.height(8.dp))

                AccountsRow(
                    accounts = accounts,
                    selectedAccount = toAccount,
                    onSelectedAccountChanged = onToAccountChanged,
                    onAddNewAccount = onAddNewAccount,
                    childrenTestTag = "to_account",
                )
            }
        }
    }
}

@Composable
@Suppress("ParameterNaming")
private fun AccountsRow(
    accounts: List<Account>,
    selectedAccount: Account?,
    onSelectedAccountChanged: (Account) -> Unit,
    modifier: Modifier = Modifier,
    childrenTestTag: String? = null,
    onAddNewAccount: () -> Unit,
) {
    val lazyState = rememberLazyListState()

    LaunchedEffect(accounts, selectedAccount) {
        if (selectedAccount != null) {
            val selectedIndex = accounts.indexOf(selectedAccount)
            if (selectedIndex != -1) {
                launch {
                    if (TestingContext.inTest) return@launch

                    lazyState.scrollToItem(
                        index = selectedIndex,
                    )
                }
            }
        }
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        state = lazyState
    ) {
        item {
            Spacer(Modifier.width(24.dp))
        }

        itemsIndexed(accounts) { _, account ->
            AccountItem(
                account = account,
                selected = selectedAccount == account,
                testTag = childrenTestTag ?: "account"
            ) {
                onSelectedAccountChanged(account)
            }
            Spacer(Modifier.width(8.dp))
        }

        item {
            AddAccount {
                onAddNewAccount()
            }
        }

        item {
            Spacer(Modifier.width(24.dp))
        }
    }
}

@Composable
private fun AccountItem(
    account: Account,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val isDark = isAppInDarkTheme()
    val pillShape = remember { CircleShape }

    val bg = remember(selected, account.color, isDark) {
        if (selected) {
            account.color.toComposeColor().copy(alpha = if (isDark) 0.35f else 0.25f)
        } else {
            if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.60f)
        }
    }
    val borderCol = remember(selected, account.color, isDark) {
        if (selected) account.color.toComposeColor() else (if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.10f))
    }

    Row(
        modifier = Modifier
            .clip(pillShape)
            .background(bg, pillShape)
            .border(1.dp, borderCol, pillShape)
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemIconSDefaultIcon(
            iconName = account.icon,
            defaultIcon = R.drawable.ic_custom_account_s,
            tint = UI.colors.pureInverse
        )

        Spacer(Modifier.width(6.dp))

        Text(
            text = account.name,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = UI.colors.pureInverse
            )
        )
    }
}

@Composable
private fun AddAccount(
    onClick: () -> Unit
) {
    val isDark = isAppInDarkTheme()
    val pillShape = remember { CircleShape }

    Row(
        modifier = Modifier
            .clip(pillShape)
            .background(
                if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.60f),
                pillShape
            )
            .specularBorder(pillShape, isDark, 0.5.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IvyIcon(
            icon = R.drawable.ic_plus,
            tint = UI.colors.pureInverse
        )

        Spacer(Modifier.width(6.dp))

        Text(
            text = stringResource(R.string.add_account),
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = UI.colors.pureInverse
            )
        )
    }
}

@Composable
private fun Amount(
    type: TransactionType,
    amount: Double,
    currency: String,
    percentExpanded: Float,
    label: String,
    account: Account?,
    onShowAmountModal: () -> Unit,
    showConvertedAmountText: String? = null,
    onAccountMiniClick: () -> Unit
) {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val percentCollapsed = 1f - percentExpanded
        val balanceFontSize = lerp(40, 30, percentCollapsed)
        val currencyFontSize = lerp(30, 18, percentCollapsed)

        Spacer(Modifier.width(32.dp))

        if (percentExpanded > 0.01f) {
            Spacer(
                Modifier.weight(
                    (1f * percentExpanded).coerceAtLeast(0.01f)
                )
            )
        }

        Column {
            BalanceRow(
                modifier = Modifier
                    .clickableNoIndication(rememberInteractionSource()) {
                        onShowAmountModal()
                    }
                    .testTag("edit_amount_balance_row"),
                currency = currency,
                balance = amount,

                spacerCurrency = 8.dp,

                balanceFontSize = balanceFontSize.sp,
                currencyFontSize = currencyFontSize.sp,

                currencyUpfront = false
            )
            if (showConvertedAmountText != null) {
                Text(
                    text = showConvertedAmountText,
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = UI.colors.pureInverse
                    )
                )
            }
        }

        Spacer(Modifier.weight(1f))

        if (percentExpanded < 1f && type != TransactionType.TRANSFER) {
            LabelAccountMini(
                percentExpanded = percentExpanded,
                label = label,
                account = account,
                onClick = onAccountMiniClick
            )
        }

        Spacer(Modifier.width(32.dp))
    }
}

@Composable
private fun LabelAccountMini(
    percentExpanded: Float,
    label: String,
    account: Account?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)

                val width = placeable.width * (1f - percentExpanded)

                layout(width.roundToInt(), placeable.height) {
                    placeable.placeRelative(
                        x = 0,
                        y = 0
                    )
                }
            }
            .alpha(1f - percentExpanded)
            .clickableNoIndication(
                interactionSource = rememberInteractionSource(),
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = UI.colors.mediumInverse
            )
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text = account?.name?.toUpperCase(Locale.getDefault()) ?: "",
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = UI.colors.pureInverse
            )
        )
    }
}
