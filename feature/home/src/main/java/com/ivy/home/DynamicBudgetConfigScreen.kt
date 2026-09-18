package com.ivy.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ivy.design.l0_system.UI
import com.ivy.ui.component.LiquidGlassCard
import com.ivy.ui.component.pocketMoneyBackground
import com.ivy.ui.component.specularBorder

@Composable
fun BoxWithConstraintsScope.DynamicBudgetConfigScreen() {
    val vm: DynamicBudgetConfigViewModel = viewModel()
    DynamicBudgetConfigUi(
        uiState = vm.uiState(),
        onEvent = vm::onEvent,
    )
}

@Composable
fun DynamicBudgetConfigUi(
    uiState: DynamicBudgetConfigState,
    onEvent: (DynamicBudgetConfigEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = !UI.colors.isLight

    Box(
        modifier = modifier
            .fillMaxSize()
            .pocketMoneyBackground(isDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            // --- Header ---------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { onEvent(DynamicBudgetConfigEvent.Back) },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.08f)
                            else Color.White.copy(alpha = 0.60f)
                        )
                        .specularBorder(CircleShape, isDark, strokeWidth = 0.5.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = UI.colors.pureInverse,
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "Spending budget",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = UI.colors.pureInverse,
                )
            }

            if (uiState.loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = if (isDark) Color(0xFF7C4DFF) else Color(0xFFC2623A),
                        strokeWidth = 2.5.dp
                    )
                }
            } else {
                // --- Scrollable Form --------------------------------------
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 8.dp, bottom = 16.dp),
                ) {
                    // --- Amount Input Section ------------------------------
                    LiquidGlassCard(
                        shape = RoundedCornerShape(24.dp),
                        isDark = isDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "BUDGET AMOUNT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp,
                                    color = UI.colors.mediumInverse
                                )
                            )

                            Spacer(Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = uiState.amountInput,
                                    onValueChange = {
                                        onEvent(DynamicBudgetConfigEvent.AmountChanged(it))
                                    },
                                    placeholder = {
                                        Text(
                                            text = "0.00",
                                            style = MaterialTheme.typography.headlineLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = UI.colors.mediumInverse.copy(alpha = 0.35f)
                                            )
                                        )
                                    },
                                    singleLine = true,
                                    isError = uiState.amountError != null,
                                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = UI.colors.pureInverse
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        errorBorderColor = Color.Transparent,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        errorContainerColor = Color.Transparent,
                                        cursorColor = if (isDark) Color(0xFF7C4DFF) else Color(0xFFC2623A)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(Modifier.width(8.dp))

                                // Currency badge ("TND")
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isDark) Color(0xFF7C4DFF).copy(alpha = 0.25f)
                                             else Color(0xFFC2623A).copy(alpha = 0.18f)
                                        )
                                        .specularBorder(RoundedCornerShape(12.dp), isDark, 0.5.dp)
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "TND",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color(0xFF99F6E4) else Color(0xFFC2623A)
                                        )
                                    )
                                }
                            }

                            if (uiState.amountError != null) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = uiState.amountError,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFF43F5E),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }

                            if (!uiState.isBaseCurrencyTnd) {
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = "PocketMoney's spending budget is TND-only. Expenses in other currencies won't count toward your allowance.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = UI.colors.mediumInverse
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // --- Period Selector Section ---------------------------
                    Text(
                        text = "Period",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = UI.colors.pureInverse
                        )
                    )
                    Spacer(Modifier.height(12.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PeriodTypeChoice.entries.forEach { choice ->
                            val isSelected = uiState.periodType == choice
                            val pillShape = RoundedCornerShape(16.dp)

                            val selectedBgColor = if (isDark) {
                                Color(0xFF7C4DFF).copy(alpha = 0.28f)
                            } else {
                                Color(0xFFC2623A).copy(alpha = 0.20f)
                            }

                            val unselectedBgColor = if (isDark) {
                                Color.White.copy(alpha = 0.06f)
                            } else {
                                Color.White.copy(alpha = 0.50f)
                            }

                            val accentColor = if (isDark) Color(0xFF7C4DFF) else Color(0xFFC2623A)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .clip(pillShape)
                                    .background(if (isSelected) selectedBgColor else unselectedBgColor)
                                    .specularBorder(pillShape, isDark, strokeWidth = 0.5.dp)
                                    .clickable {
                                        onEvent(DynamicBudgetConfigEvent.PeriodTypeChanged(choice))
                                    }
                                    .padding(horizontal = 18.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = choice.displayLabel,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) UI.colors.pureInverse else UI.colors.mediumInverse
                                        )
                                    )

                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(accentColor)
                                                .specularBorder(CircleShape, isDark, 0.5.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- Payday / Date Range Pickers -----------------------
                    if (uiState.periodType == PeriodTypeChoice.SALARY) {
                        Spacer(Modifier.height(18.dp))
                        LiquidGlassCard(
                            shape = RoundedCornerShape(20.dp),
                            isDark = isDark,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Text(
                                    text = "Salary cycle payday",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = UI.colors.pureInverse
                                    )
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Day of month when allowance resets (1–31)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = UI.colors.mediumInverse
                                    )
                                )
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = uiState.salaryPaydayInput,
                                    onValueChange = {
                                        onEvent(DynamicBudgetConfigEvent.SalaryPaydayChanged(it))
                                    },
                                    placeholder = {
                                        Text("e.g. 25", color = UI.colors.mediumInverse.copy(alpha = 0.5f))
                                    },
                                    singleLine = true,
                                    isError = uiState.salaryPaydayError != null,
                                    supportingText = uiState.salaryPaydayError?.let { msg ->
                                        { Text(msg, color = Color(0xFFF43F5E)) }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (isDark) Color(0xFF7C4DFF) else Color(0xFFC2623A),
                                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.12f),
                                        focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.40f),
                                        unfocusedContainerColor = if (isDark) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.30f),
                                        focusedTextColor = UI.colors.pureInverse,
                                        unfocusedTextColor = UI.colors.pureInverse,
                                        cursorColor = if (isDark) Color(0xFF7C4DFF) else Color(0xFFC2623A)
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    if (uiState.periodType == PeriodTypeChoice.CUSTOM) {
                        Spacer(Modifier.height(18.dp))
                        LiquidGlassCard(
                            shape = RoundedCornerShape(20.dp),
                            isDark = isDark,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Text(
                                    text = "Custom date range",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = UI.colors.pureInverse
                                    )
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Define the start and end dates for your cycle",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = UI.colors.mediumInverse
                                    )
                                )
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = uiState.customStartInput,
                                    onValueChange = {
                                        onEvent(DynamicBudgetConfigEvent.CustomStartChanged(it))
                                    },
                                    label = { Text("Start (dd/mm/yyyy)", color = UI.colors.mediumInverse) },
                                    singleLine = true,
                                    isError = uiState.customStartError != null,
                                    supportingText = uiState.customStartError?.let { msg ->
                                        { Text(msg, color = Color(0xFFF43F5E)) }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (isDark) Color(0xFF7C4DFF) else Color(0xFFC2623A),
                                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.12f),
                                        focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.40f),
                                        unfocusedContainerColor = if (isDark) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.30f),
                                        focusedTextColor = UI.colors.pureInverse,
                                        unfocusedTextColor = UI.colors.pureInverse,
                                        cursorColor = if (isDark) Color(0xFF7C4DFF) else Color(0xFFC2623A)
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = uiState.customEndInput,
                                    onValueChange = {
                                        onEvent(DynamicBudgetConfigEvent.CustomEndChanged(it))
                                    },
                                    label = { Text("End (dd/mm/yyyy)", color = UI.colors.mediumInverse) },
                                    singleLine = true,
                                    isError = uiState.customEndError != null,
                                    supportingText = uiState.customEndError?.let { msg ->
                                        { Text(msg, color = Color(0xFFF43F5E)) }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (isDark) Color(0xFF7C4DFF) else Color(0xFFC2623A),
                                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.12f),
                                        focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.40f),
                                        unfocusedContainerColor = if (isDark) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.30f),
                                        focusedTextColor = UI.colors.pureInverse,
                                        unfocusedTextColor = UI.colors.pureInverse,
                                        cursorColor = if (isDark) Color(0xFF7C4DFF) else Color(0xFFC2623A)
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }

                // --- Action Buttons ----------------------------------------
                val saveGradient = if (isDark) {
                    Brush.horizontalGradient(listOf(Color(0xFF8A6BFF), Color(0xFF6440E0)))
                } else {
                    Brush.horizontalGradient(listOf(Color(0xFF9E4622), Color(0xFFC2623A)))
                }

                val isSaveEnabled = uiState.canSave && !uiState.saving

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(
                            elevation = if (isSaveEnabled) 8.dp else 0.dp,
                            shape = CircleShape,
                            ambientColor = if (isDark) Color(0xFF7C4DFF).copy(alpha = 0.3f) else Color(0xFFC2623A).copy(alpha = 0.2f),
                            spotColor = if (isDark) Color(0xFF7C4DFF).copy(alpha = 0.4f) else Color(0xFFC2623A).copy(alpha = 0.3f)
                        )
                        .clip(CircleShape)
                        .background(
                            if (isSaveEnabled) saveGradient
                            else Brush.horizontalGradient(
                                listOf(
                                    Color.Gray.copy(alpha = 0.25f),
                                    Color.Gray.copy(alpha = 0.25f)
                                )
                            )
                        )
                        .specularBorder(CircleShape, isDark, strokeWidth = 0.5.dp)
                        .clickable(enabled = isSaveEnabled) {
                            onEvent(DynamicBudgetConfigEvent.Save)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.saving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        Text(
                            text = "Save",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isSaveEnabled) Color.White else Color.White.copy(alpha = 0.45f)
                        )
                    }
                }

                if (uiState.hasExistingBudget) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.08f)
                                else Color.White.copy(alpha = 0.60f)
                            )
                            .specularBorder(CircleShape, isDark, strokeWidth = 0.5.dp)
                            .clickable(enabled = !uiState.saving) {
                                onEvent(DynamicBudgetConfigEvent.RemoveBudget)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Remove budget",
                            color = UI.colors.pureInverse.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}