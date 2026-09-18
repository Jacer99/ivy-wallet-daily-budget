package com.ivy.design.system

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalAppInDarkTheme = staticCompositionLocalOf<Boolean?> { null }

@Composable
fun isAppInDarkTheme(): Boolean {
    LocalAppInDarkTheme.current?.let { return it }
    val bg = MaterialTheme.colorScheme.background
    if (bg != Color(0xFFFDFBFF)) {
        return true
    }
    return isSystemInDarkTheme()
}

@Immutable
data class PocketMoneyColors(
    val isDark: Boolean,
    val canvasBackground: Color,
    val pureInverse: Color,
    val mediumInverse: Color,
    val cardBackground: Color,
    val glassBorderTop: Color,
    val glassBorderMid: Color,
    val glassBorderBottom: Color
)

val LocalPocketMoneyColors = staticCompositionLocalOf<PocketMoneyColors> {
    error("No PocketMoneyColors provided")
}

object PocketMoneyTheme {
    val colors: PocketMoneyColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPocketMoneyColors.current
}

@Composable
fun PocketMoneyTheme(
    isDark: Boolean = isAppInDarkTheme(),
    content: @Composable () -> Unit
) {
    val canvasBg by animateColorAsState(
        targetValue = if (isDark) Color(0xFF0B0B14) else Color(0xFFF4F3F8),
        animationSpec = tween(400),
        label = "pmThemeCanvas"
    )
    val pureInv by animateColorAsState(
        targetValue = if (isDark) Color.White else Color(0xFF111111),
        animationSpec = tween(400),
        label = "pmThemePureInv"
    )
    val medInv by animateColorAsState(
        targetValue = if (isDark) Color.White.copy(alpha = 0.50f) else Color(0xFF111111).copy(alpha = 0.45f),
        animationSpec = tween(400),
        label = "pmThemeMedInv"
    )
    val cardBg by animateColorAsState(
        targetValue = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFFFFFFF).copy(alpha = 0.65f),
        animationSpec = tween(400),
        label = "pmThemeCardBg"
    )
    val borderTop by animateColorAsState(
        targetValue = if (isDark) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.85f),
        animationSpec = tween(400),
        label = "pmThemeBorderTop"
    )
    val borderMid by animateColorAsState(
        targetValue = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFD7B996).copy(alpha = 0.35f),
        animationSpec = tween(400),
        label = "pmThemeBorderMid"
    )

    val colors = PocketMoneyColors(
        isDark = isDark,
        canvasBackground = canvasBg,
        pureInverse = pureInv,
        mediumInverse = medInv,
        cardBackground = cardBg,
        glassBorderTop = borderTop,
        glassBorderMid = borderMid,
        glassBorderBottom = Color.Transparent
    )

    CompositionLocalProvider(
        LocalPocketMoneyColors provides colors,
        content = content
    )
}
