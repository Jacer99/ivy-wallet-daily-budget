package com.ivy.accounts

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * AccountsScreen
 * Wrapper entry point for Accounts tab adhering to Liquid Glass design system.
 */
@Composable
fun AccountsScreen(
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        AccountsTab()
    }
}
