package com.ivy.wallet.ui.theme.modal.edit

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.design.system.isAppInDarkTheme
import com.ivy.domain.di.FeaturesEntryPoint
import com.ivy.legacy.IvyWalletPreview
import com.ivy.legacy.utils.amountToDouble
import com.ivy.legacy.utils.amountToDoubleOrNull
import com.ivy.legacy.utils.format
import com.ivy.legacy.utils.formatInputAmount
import com.ivy.legacy.utils.formatInt
import com.ivy.legacy.utils.hideKeyboard
import com.ivy.legacy.utils.localDecimalSeparator
import com.ivy.legacy.utils.onScreenStart
import com.ivy.ui.R
import com.ivy.ui.component.LiquidGlassCard
import com.ivy.ui.component.specularBorder
import com.ivy.wallet.ui.theme.Red
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.modal.IvyModal
import com.ivy.wallet.ui.theme.modal.ModalPositiveButton
import com.ivy.wallet.ui.theme.modal.modalPreviewActionRowHeight
import dagger.hilt.android.EntryPointAccessors
import java.util.UUID
import kotlin.math.truncate

@SuppressLint("ComposeModifierMissing")
@Suppress("ParameterNaming")
@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun BoxWithConstraintsScope.AmountModal(
    id: UUID,
    visible: Boolean,
    currency: String,
    initialAmount: Double?,
    dismiss: () -> Unit,
    showPlusMinus: Boolean = false,
    decimalCountMax: Int = 2,
    Header: (@Composable () -> Unit)? = null,
    amountSpacerTop: Dp = 64.dp,
    onAmountChanged: (Double) -> Unit,
) {
    val isDark = isAppInDarkTheme()
    var amount by remember(id) {
        mutableStateOf(
            if (currency.isNotEmpty()) {
                initialAmount?.takeIf { it != 0.0 }?.format(currency)
                    ?: ""
            } else {
                initialAmount?.takeIf { it != 0.0 }?.format(decimalCountMax)
                    ?: ""
            }
        )
    }

    var calculatorModalVisible by remember(id) {
        mutableStateOf(false)
    }

    IvyModal(
        id = id,
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {
            val calcBtnShape = remember { CircleShape }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(calcBtnShape)
                    .background(
                        if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.60f),
                        calcBtnShape
                    )
                    .specularBorder(calcBtnShape, isDark, 0.5.dp)
                    .clickable { calculatorModalVisible = true }
                    .testTag("btn_calculator"),
                contentAlignment = Alignment.Center
            ) {
                IvyIcon(
                    icon = R.drawable.ic_custom_calculator_m,
                    tint = UI.colors.pureInverse,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            ModalPositiveButton(
                text = stringResource(R.string.enter),
                iconStart = R.drawable.ic_check
            ) {
                try {
                    onAmountChanged(amount.amountToDouble())
                    dismiss()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        },
        SecondaryActions = {
            if (showPlusMinus) {
                Row {
                    Spacer(modifier = Modifier.width(24.dp))
                    KeypadCircleButton(
                        text = "+/-",
                        testTag = "plus_minus",
                        fontSize = 22.sp,
                        btnSize = 52.dp,
                        onClick = {
                            when {
                                amount.firstOrNull() == '-' -> {
                                    amount = amount.drop(1)
                                }

                                amount.isNotEmpty() -> {
                                    amount = "-$amount"
                                }
                            }
                        }
                    )
                }
            }
        }
    ) {
        Header?.invoke()

        Spacer(Modifier.height(amountSpacerTop))

        val rootView = LocalView.current
        onScreenStart {
            hideKeyboard(rootView)
        }

        AmountCurrency(
            amount = amount,
            currency = currency
        )

        Spacer(Modifier.height(10.dp))

        AmountInput(
            currency = currency,
            decimalCountMax = decimalCountMax,
            amount = amount
        ) {
            amount = it
        }

        Spacer(Modifier.height(24.dp))
    }

    CalculatorModal(
        visible = calculatorModalVisible,
        initialAmount = amount.amountToDoubleOrNull(),
        currency = currency,
        dismiss = {
            calculatorModalVisible = false
        },
        onCalculation = {
            amount = if (currency.isNotEmpty()) it.format(currency) else it.format(decimalCountMax)
        }
    )
}

@SuppressLint("ComposeModifierMissing")
@Composable
fun AmountCurrency(
    amount: String,
    currency: String,
) {
    val isDark = isAppInDarkTheme()
    val cardShape = remember { RoundedCornerShape(22.dp) }

    LiquidGlassCard(
        shape = cardShape,
        isDark = isDark,
        strokeWidth = 0.5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = amount.ifBlank { "0" },
                style = TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = UI.colors.pureInverse,
                    letterSpacing = (-0.02).sp
                )
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = currency,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = UI.colors.mediumInverse
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
fun AmountInput(
    currency: String,
    amount: String,
    decimalCountMax: Int = 2,
    setAmount: (String) -> Unit,
) {
    var firstInput by remember { mutableStateOf(true) }

    AmountKeyboard(
        horizontalPadding = 40.dp,
        forCalculator = false,
        onNumberPressed = {
            if (firstInput) {
                setAmount(it)
                firstInput = false
            } else {
                val formattedAmount = formatInputAmount(
                    currency = currency,
                    amount = amount,
                    newSymbol = it,
                    decimalCountMax = decimalCountMax
                )
                if (formattedAmount != null) {
                    setAmount(formattedAmount)
                }
            }
        },
        onDecimalPoint = {
            if (firstInput) {
                setAmount("0${localDecimalSeparator()}")
                firstInput = false
            } else {
                val newlyEnteredString = if (amount.isEmpty()) {
                    "0${localDecimalSeparator()}"
                } else {
                    "$amount${localDecimalSeparator()}"
                }
                if (newlyEnteredString.amountToDoubleOrNull() != null) {
                    setAmount(newlyEnteredString)
                }
            }
        },
        onBackspace = {
            if (firstInput) {
                setAmount("")
                firstInput = false
            } else {
                if (amount.isNotEmpty()) {
                    val formattedNumber = formatNumber(amount.dropLast(1))
                    setAmount(formattedNumber ?: "")
                }
            }
        }
    )
}

private fun formatNumber(number: String): String? {
    val decimalPartString = number
        .split(localDecimalSeparator())
        .getOrNull(1)
    val newDecimalCount = decimalPartString?.length ?: 0

    val amountDouble = number.amountToDoubleOrNull()

    if (newDecimalCount <= 2 && amountDouble != null) {
        val intPart = truncate(amountDouble).toInt()
        val decimalFormatted = if (decimalPartString != null) {
            "${localDecimalSeparator()}$decimalPartString"
        } else {
            ""
        }

        return formatInt(intPart) + decimalFormatted
    }

    return null
}

@SuppressLint(
    "ComposeContentEmitterReturningValues",
    "ComposeMultipleContentEmitters",
    "ComposeModifierMissing",
)
@Suppress("ParameterNaming")
@Composable
fun AmountKeyboard(
    forCalculator: Boolean,
    onNumberPressed: (String) -> Unit,
    onDecimalPoint: () -> Unit,
    horizontalPadding: Dp = 0.dp,
    ZeroRow: (@Composable RowScope.() -> Unit)? = null,
    FirstRowExtra: (@Composable RowScope.() -> Unit)? = null,
    SecondRowExtra: (@Composable RowScope.() -> Unit)? = null,
    ThirdRowExtra: (@Composable RowScope.() -> Unit)? = null,
    FourthRowExtra: (@Composable RowScope.() -> Unit)? = null,
    onBackspace: () -> Unit,
) {
    val context = LocalContext.current
    val features = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            FeaturesEntryPoint::class.java
        ).getFeatures()
    }
    val isStandardLayout = features.standardKeypadLayout.asEnabledState()

    val countButtonValues = if (isStandardLayout) {
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
        )
    } else {
        listOf(
            listOf("7", "8", "9"),
            listOf("4", "5", "6"),
            listOf("1", "2", "3"),
        )
    }

    if (ZeroRow != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ZeroRow.invoke(this)
        }

        Spacer(Modifier.height(8.dp))
    }

    countButtonValues.forEachIndexed { rowIndex, rowNumbers ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            rowNumbers.forEach { num ->
                CircleNumberButton(
                    forCalculator = forCalculator,
                    value = num,
                    onNumberPressed = onNumberPressed
                )
            }

            when (rowIndex) {
                0 -> FirstRowExtra?.invoke(this)
                1 -> SecondRowExtra?.invoke(this)
                2 -> ThirdRowExtra?.invoke(this)
            }
        }

        Spacer(Modifier.height(8.dp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        KeypadCircleButton(
            text = localDecimalSeparator(),
            testTag = if (forCalculator) {
                "calc_key_decimal_separator"
            } else {
                "key_decimal_separator"
            }
        ) {
            onDecimalPoint()
        }

        CircleNumberButton(
            forCalculator = forCalculator,
            value = "0",
            onNumberPressed = onNumberPressed
        )

        val isDark = isAppInDarkTheme()
        val btnShape = remember { CircleShape }
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(btnShape)
                .background(
                    if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.50f),
                    btnShape
                )
                .specularBorder(btnShape, isDark, 0.5.dp)
                .clickable { onBackspace() }
                .testTag("key_del"),
            contentAlignment = Alignment.Center
        ) {
            IvyIcon(
                icon = R.drawable.ic_backspace,
                tint = if (isDark) Color(0xFFF43F5E) else Color(0xFFB91C1C),
                modifier = Modifier.size(22.dp)
            )
        }

        FourthRowExtra?.invoke(this)
    }
}

@Composable
@Suppress("ParameterNaming")
fun CircleNumberButton(
    forCalculator: Boolean,
    value: String,
    onNumberPressed: (String) -> Unit,
) {
    KeypadCircleButton(
        text = value,
        testTag = if (forCalculator) {
            "calc_key_$value"
        } else {
            "key_$value"
        },
        onClick = {
            onNumberPressed(value)
        }
    )
}

@SuppressLint("ComposeModifierMissing")
@Composable
fun KeypadCircleButton(
    text: String,
    testTag: String,
    textColor: Color = UI.colors.pureInverse,
    fontSize: TextUnit = 28.sp,
    btnSize: Dp = 72.dp,
    onClick: () -> Unit,
) {
    val isDark = isAppInDarkTheme()
    val btnShape = remember { CircleShape }

    Box(
        modifier = Modifier
            .size(btnSize)
            .clip(btnShape)
            .background(
                if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.55f),
                btnShape
            )
            .specularBorder(btnShape, isDark, 0.5.dp)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            style = TextStyle(
                color = textColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Preview
@Composable
private fun Preview() {
    IvyWalletPreview {
        BoxWithConstraints(
            modifier = Modifier.padding(bottom = modalPreviewActionRowHeight())
        ) {
            AmountModal(
                id = UUID.randomUUID(),
                visible = true,
                currency = "BGN",
                initialAmount = null,
                dismiss = { }
            ) {
            }
        }
    }
}
