package com.ivy.design.system

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun IvyMaterial3Theme(
    isTrueBlack: Boolean,
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (dark) ivyDarkColorScheme(isTrueBlack) else ivyLightColorScheme(),
        content = content,
    )
}

private fun ivyLightColorScheme(): ColorScheme = ColorScheme(
    primary = Color(0xFF7C4DFF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8DDFF),
    onPrimaryContainer = Color(0xFF21005D),
    inversePrimary = Color(0xFF9D7BFF),

    secondary = Color(0xFF00BFA5),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFB2EBE0),
    onSecondaryContainer = Color(0xFF002019),

    tertiary = Color(0xFFFF6E40),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDAD0),
    onTertiaryContainer = Color(0xFF3B0900),

    error = Color(0xFFFF5252),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = Color(0xFFFDFBFF),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    surfaceTint = Color(0xFF7C4DFF),
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),

    outline = Color(0xFF7A757F),
    outlineVariant = Color(0xFFCAC4D0),
    scrim = Color(0xFF000000).copy(alpha = 0.8f)
)

private fun ivyDarkColorScheme(isTrueBlack: Boolean): ColorScheme = ColorScheme(
    primary = Color(0xFF9D7BFF),
    onPrimary = Color(0xFF2C0079),
    primaryContainer = Color(0xFF4B2FA0),
    onPrimaryContainer = Color(0xFFE8DDFF),
    inversePrimary = Color(0xFF7C4DFF),

    secondary = Color(0xFF4DD0C7),
    onSecondary = Color(0xFF003730),
    secondaryContainer = Color(0xFF005046),
    onSecondaryContainer = Color(0xFFB2EBE0),

    tertiary = Color(0xFFFF8A65),
    onTertiary = Color(0xFF5C1900),
    tertiaryContainer = Color(0xFF7C2E10),
    onTertiaryContainer = Color(0xFFFFDAD0),

    error = Color(0xFFFF8A8A),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = if (isTrueBlack) Color(0xFF000000) else Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = if (isTrueBlack) Color(0xFF000000) else Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceTint = Color(0xFF9D7BFF),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = if (isTrueBlack) Color(0xFF000000) else Color(0xFF1C1B1F),

    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    scrim = Color(0xFF000000).copy(alpha = 0.8f)
)
