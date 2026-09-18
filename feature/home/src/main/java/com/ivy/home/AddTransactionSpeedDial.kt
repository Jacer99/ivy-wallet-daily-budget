package com.ivy.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ivy.design.l0_system.UI
import com.ivy.design.system.isAppInDarkTheme
import com.ivy.legacy.utils.clickableNoIndication
import com.ivy.legacy.utils.rememberInteractionSource
import com.ivy.ui.component.LiquidGlassCard
import com.ivy.ui.component.specularBorder

/**
 * AddTransactionSpeedDial
 * Mirror of PocketMoney Add Transaction (Turn 3a & 3b):
 * 1. Fullscreen Backdrop Overlay: Translucent frosted overlay (Dark: #050508 @ 72%, Light: #FFFFFF @ 55%).
 * 2. Planned Payment Action Pill: Centered floating glass pill with text '⚡ Add planned payment'.
 * 3. 3 Circular Action FABs (74dp diameter):
 *    - Add Income: Green gradient brush (#30E0A8 -> #1AA878) with down arrow '↓' and "ADD INCOME" label.
 *    - Add Expense (Center elevated): Translucent frosted glass FAB with up arrow '↑', 0.5dp specular border, drop glow, and "ADD EXPENSE" label.
 *    - Account Transfer: Purple gradient brush (#9C6BFF -> #6440E0) with exchange arrow '⇄' and "ACCOUNT TRANSFER" label.
 * 4. Dismiss Button: Bottom circular frosted glass button (52dp) with '✕' icon.
 */
@Composable
fun AddTransactionSpeedDial(
    visible: Boolean,
    onDismiss: () -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onAddTransfer: () -> Unit,
    onAddPlannedPayment: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = isAppInDarkTheme()
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(180))
    ) {
        val backdropColor = if (isDark) {
            Color(0xFF050508).copy(alpha = 0.72f)
        } else {
            Color.White.copy(alpha = 0.55f)
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(backdropColor)
                .clickableNoIndication(rememberInteractionSource()) {
                    onDismiss()
                }
                .zIndex(300f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
                    .clickableNoIndication(rememberInteractionSource()) {
                        // Consume clicks inside bottom area
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Planned Payment Action Pill
                LiquidGlassCard(
                    shape = RoundedCornerShape(30.dp),
                    isDark = isDark,
                    strokeWidth = 0.5.dp,
                    modifier = Modifier
                        .testTag("speed_dial_planned_payment")
                        .clickable {
                            onDismiss()
                            onAddPlannedPayment()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "⚡ Add planned payment",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = UI.colors.pureInverse,
                                letterSpacing = 0.2.sp
                            )
                        )
                    }
                }

                Spacer(Modifier.height(36.dp))

                // 2. 3 Circular Action FABs (74dp diameter)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Left: Add Income FAB
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = CircleShape,
                                    ambientColor = Color(0xFF30E0A8).copy(alpha = 0.35f),
                                    spotColor = Color(0xFF30E0A8).copy(alpha = 0.45f)
                                )
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(Color(0xFF30E0A8), Color(0xFF1AA878))
                                    ),
                                    shape = CircleShape
                                )
                                .specularBorder(shape = CircleShape, isDark = isDark, strokeWidth = 0.5.dp)
                                .clickable {
                                    onDismiss()
                                    onAddIncome()
                                }
                                .testTag("speed_dial_income"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "↓",
                                style = TextStyle(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "ADD INCOME",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = UI.colors.pureInverse
                            )
                        )
                    }

                    // Center Elevated: Add Expense FAB
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.offset(y = (-20).dp)
                    ) {
                        val glowColor = if (isDark) Color(0xFF00F2FE) else Color(0xFFC2532F)
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .drawBehind {
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                glowColor.copy(alpha = 0.45f),
                                                Color.Transparent
                                            ),
                                            radius = size.width * 1.15f
                                        )
                                    )
                                }
                                .shadow(
                                    elevation = 16.dp,
                                    shape = CircleShape,
                                    ambientColor = glowColor.copy(alpha = 0.25f),
                                    spotColor = glowColor.copy(alpha = 0.40f)
                                )
                                .clip(CircleShape)
                                .background(
                                    color = if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.85f),
                                    shape = CircleShape
                                )
                                .specularBorder(shape = CircleShape, isDark = isDark, strokeWidth = 0.5.dp)
                                .clickable {
                                    onDismiss()
                                    onAddExpense()
                                }
                                .testTag("speed_dial_expense"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "↑",
                                style = TextStyle(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = UI.colors.pureInverse
                                )
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "ADD EXPENSE",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = UI.colors.pureInverse
                            )
                        )
                    }

                    // Right: Account Transfer FAB
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = CircleShape,
                                    ambientColor = Color(0xFF9C6BFF).copy(alpha = 0.35f),
                                    spotColor = Color(0xFF9C6BFF).copy(alpha = 0.45f)
                                )
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(Color(0xFF9C6BFF), Color(0xFF6440E0))
                                    ),
                                    shape = CircleShape
                                )
                                .specularBorder(shape = CircleShape, isDark = isDark, strokeWidth = 0.5.dp)
                                .clickable {
                                    onDismiss()
                                    onAddTransfer()
                                }
                                .testTag("speed_dial_transfer"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⇄",
                                style = TextStyle(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "ACCOUNT TRANSFER",
                            style = TextStyle(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp,
                                color = UI.colors.pureInverse
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(36.dp))

                // 3. Dismiss Button: Bottom circular frosted glass button (52dp) with '✕' icon
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.08f),
                            spotColor = Color.Black.copy(alpha = 0.12f)
                        )
                        .clip(CircleShape)
                        .background(
                            color = if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.75f),
                            shape = CircleShape
                        )
                        .specularBorder(shape = CircleShape, isDark = isDark, strokeWidth = 0.5.dp)
                        .clickable { onDismiss() }
                        .testTag("speed_dial_dismiss"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = UI.colors.pureInverse
                        )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewAddTransactionSpeedDialDark() {
    Box(modifier = Modifier.fillMaxSize()) {
        AddTransactionSpeedDial(
            visible = true,
            onDismiss = {},
            onAddIncome = {},
            onAddExpense = {},
            onAddTransfer = {},
            onAddPlannedPayment = {},
            isDark = true
        )
    }
}

@Preview
@Composable
private fun PreviewAddTransactionSpeedDialLight() {
    Box(modifier = Modifier.fillMaxSize()) {
        AddTransactionSpeedDial(
            visible = true,
            onDismiss = {},
            onAddIncome = {},
            onAddExpense = {},
            onAddTransfer = {},
            onAddPlannedPayment = {},
            isDark = false
        )
    }
}
