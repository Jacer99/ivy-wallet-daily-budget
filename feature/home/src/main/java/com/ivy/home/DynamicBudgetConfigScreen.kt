package com.ivy.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

private val PocketMoneyAccent = Color(0xFFC4783E)

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
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp),
        ) {
            // --- Top bar ---------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { onEvent(DynamicBudgetConfigEvent.Back) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Spending budget",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
            )

            if (uiState.loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = PocketMoneyAccent)
                }
            } else {
                // --- Scrollable form --------------------------------------
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(top = 16.dp),
                ) {
                    // Amount
                    OutlinedTextField(
                        value = uiState.amountInput,
                        onValueChange = {
                            onEvent(DynamicBudgetConfigEvent.AmountChanged(it))
                        },
                        label = { Text("Amount (TND)") },
                        singleLine = true,
                        isError = uiState.amountError != null,
                        supportingText = uiState.amountError?.let { msg ->
                            { Text(msg) }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "Period",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(8.dp))

                    Column(Modifier.selectableGroup()) {
                        PeriodTypeChoice.entries.forEach { choice ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = uiState.periodType == choice,
                                        onClick = {
                                            onEvent(
                                                DynamicBudgetConfigEvent
                                                    .PeriodTypeChanged(choice)
                                            )
                                        },
                                        role = Role.RadioButton,
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = uiState.periodType == choice,
                                    onClick = null,
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = choice.displayLabel,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                    }

                    if (uiState.periodType == PeriodTypeChoice.SALARY) {
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = uiState.salaryPaydayInput,
                            onValueChange = {
                                onEvent(DynamicBudgetConfigEvent.SalaryPaydayChanged(it))
                            },
                            label = { Text("Payday (1–31)") },
                            singleLine = true,
                            isError = uiState.salaryPaydayError != null,
                            supportingText = uiState.salaryPaydayError?.let { msg ->
                                { Text(msg) }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    if (uiState.periodType == PeriodTypeChoice.CUSTOM) {
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = uiState.customStartInput,
                            onValueChange = {
                                onEvent(DynamicBudgetConfigEvent.CustomStartChanged(it))
                            },
                            label = { Text("Start (dd/mm/yyyy)") },
                            singleLine = true,
                            isError = uiState.customStartError != null,
                            supportingText = uiState.customStartError?.let { msg ->
                                { Text(msg) }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.customEndInput,
                            onValueChange = {
                                onEvent(DynamicBudgetConfigEvent.CustomEndChanged(it))
                            },
                            label = { Text("End (dd/mm/yyyy)") },
                            singleLine = true,
                            isError = uiState.customEndError != null,
                            supportingText = uiState.customEndError?.let { msg ->
                                { Text(msg) }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                }

                // --- Save button ------------------------------------------
                Button(
                    onClick = { onEvent(DynamicBudgetConfigEvent.Save) },
                    enabled = uiState.canSave && !uiState.saving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PocketMoneyAccent,
                        contentColor = Color.White,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = if (uiState.hasExistingBudget) 8.dp else 16.dp),
                ) {
                    if (uiState.saving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        Text("Save", fontWeight = FontWeight.SemiBold)
                    }
                }

                if (uiState.hasExistingBudget) {
                    OutlinedButton(
                        onClick = { onEvent(DynamicBudgetConfigEvent.RemoveBudget) },
                        enabled = !uiState.saving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                    ) {
                        Text("Remove budget")
                    }
                }
            }
        }
    }
}