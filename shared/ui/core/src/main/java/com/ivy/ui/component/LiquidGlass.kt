package com.ivy.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivy.design.system.isAppInDarkTheme
import kotlin.math.max

/**
 * PocketMoney Liquid Glass Design Primitives
 * Direct translation of PocketMoney All Screens reference HTML mockups.
 */

/**
 * 1. Modifier.pocketMoneyBackground
 * Draws the base canvas and 3 ambient radial glow spots with smooth 400ms cross-fade animation when switching themes.
 */
@Composable
fun Modifier.pocketMoneyBackground(isDark: Boolean = isAppInDarkTheme()): Modifier {
    val canvasBase by animateColorAsState(
        targetValue = if (isDark) Color(0xFF0B0B14) else Color(0xFFF4F3F8),
        animationSpec = tween(400),
        label = "pmBgCanvas"
    )
    val radial1Color by animateColorAsState(
        targetValue = if (isDark) Color(0xFF4B3AA8).copy(alpha = 0.45f) else Color(0xFFDCD3FF).copy(alpha = 0.55f),
        animationSpec = tween(400),
        label = "pmBgRadial1"
    )
    val radial2Color by animateColorAsState(
        targetValue = if (isDark) Color(0xFFA83A87).copy(alpha = 0.40f) else Color(0xFFFFD9EC).copy(alpha = 0.50f),
        animationSpec = tween(400),
        label = "pmBgRadial2"
    )
    val radial3Color by animateColorAsState(
        targetValue = if (isDark) Color(0xFF1F2452).copy(alpha = 0.55f) else Color(0xFFCFE9FF).copy(alpha = 0.55f),
        animationSpec = tween(400),
        label = "pmBgRadial3"
    )

    return this.drawBehind {
        val w = size.width
        val h = size.height

        drawRect(canvasBase)

        // Radial 1: top-left accent
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(radial1Color, Color.Transparent),
                center = Offset(if (isDark) w * 0.15f else w * 0.10f, 0f),
                radius = w * 0.70f
            )
        )

        // Radial 2: top-right accent
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(radial2Color, Color.Transparent),
                center = Offset(if (isDark) w * 0.90f else w * 0.95f, if (isDark) h * 0.15f else h * 0.10f),
                radius = w * 0.65f
            )
        )

        // Radial 3: bottom center accent
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(radial3Color, Color.Transparent),
                center = Offset(w * 0.50f, h),
                radius = w * 0.80f
            )
        )
    }
}

/**
 * 2. Modifier.specularBorder
 * Simulates light catching the top rim of the glass using a vertical gradient brush.
 */
@Composable
fun Modifier.specularBorder(
    shape: Shape = RoundedCornerShape(26.dp),
    isDark: Boolean = isAppInDarkTheme(),
    strokeWidth: Dp = 0.5.dp
): Modifier {
    val topColor by animateColorAsState(
        targetValue = if (isDark) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.85f),
        animationSpec = tween(400),
        label = "specularBorderTop"
    )
    val midColor by animateColorAsState(
        targetValue = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFD7B996).copy(alpha = 0.35f),
        animationSpec = tween(400),
        label = "specularBorderMid"
    )
    val brush = Brush.verticalGradient(
        listOf(
            topColor,
            midColor,
            Color.Transparent
        )
    )
    return this.border(strokeWidth, brush, shape)
}

/**
 * 3. LiquidGlassCard
 * Reusable composable container with specular border, adaptive translucent fill, and soft shadow in light mode.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    isDark: Boolean = isAppInDarkTheme(),
    fillColor: Color? = null,
    strokeWidth: Dp = 0.5.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val bgFill = fillColor ?: if (isDark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color(0xFFFFFFFF).copy(alpha = 0.65f) // crisp translucent pearl
    }

    val animatedBgFill by animateColorAsState(
        targetValue = bgFill,
        animationSpec = tween(400),
        label = "LiquidGlassCardBg"
    )

    val shadowModifier = if (!isDark) {
        Modifier.shadow(
            elevation = 6.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.04f),
            spotColor = Color.Black.copy(alpha = 0.06f)
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(shadowModifier)
            .clip(shape)
            .background(animatedBgFill, shape)
            .specularBorder(shape = shape, isDark = isDark, strokeWidth = strokeWidth),
        content = content
    )
}

/**
 * Color Tokens and Gradients
 */
object LiquidGlassTokens {
    // Light Mode Canvas & Accents
    val LightCanvasBase = Color(0xFFF4F3F8)
    val LightRadial1 = Color(0xFFDCD3FF)
    val LightRadial2 = Color(0xFFFFD9EC)
    val LightRadial3 = Color(0xFFCFE9FF)

    val LightGlassBackground = Color.White.copy(alpha = 0.60f)
    val LightGlassBorderTop = Color.White.copy(alpha = 0.90f)
    val LightGlassBorderMid = Color(0xFFD7B996).copy(alpha = 0.35f)
    val LightGlassBorderBottom = Color.Transparent

    val LightHeroAmountBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF9A421E),
            Color(0xFFC2623A),
            Color(0xFFD48052)
        )
    )

    val LightIncomeGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1B3B2B),
            Color(0xFF244B36),
            Color(0xFF2D5540)
        )
    )

    val LightExpenseGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF3E2723),
            Color(0xFF321E1A),
            Color(0xFF241411)
        )
    )

    val LightFabGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF9E4622),
            Color(0xFFC2623A),
            Color(0xFFD67B4D)
        )
    )

    val LightBankVaultGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF3D271D),
            Color(0xFF542D1E),
            Color(0xFF7C3F25)
        )
    )

    val LightCashVaultGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1F3D32),
            Color(0xFF24473A),
            Color(0xFF2B5745)
        )
    )

    // Dark Mode Canvas & Accents
    val DarkCanvasBase = Color(0xFF0B0B14)
    val DarkRadial1 = Color(0xFF4B3AA8)
    val DarkRadial2 = Color(0xFFA83A87)
    val DarkRadial3 = Color(0xFF1F2452)

    val DarkGlassBackground = Color.White.copy(alpha = 0.08f)
    val DarkGlassBorderTop = Color.White.copy(alpha = 0.25f)
    val DarkGlassBorderMid = Color.White.copy(alpha = 0.05f)
    val DarkGlassBorderBottom = Color.Transparent

    val DarkHeroAmountBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF67E8F9),
            Color(0xFF99F6E4),
            Color(0xFFF0ABFC)
        )
    )

    val DarkIncomeGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF063B3B),
            Color(0xFF052B36),
            Color(0xFF041D24)
        )
    )

    val DarkExpenseGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF160C25),
            Color(0xFF0F0A1D),
            Color(0xFF090614)
        )
    )

    val DarkFabGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF7C4DFF),
            Color(0xFF632CE5),
            Color(0xFF00F2FE)
        )
    )

    val DarkBankVaultGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0C142B),
            Color(0xFF10193D),
            Color(0xFF151240)
        )
    )

    val DarkCashVaultGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF2A0B36),
            Color(0xFF3A0D4C),
            Color(0xFF220733)
        )
    )

    // Accents
    val CyanNeon = Color(0xFF00F2FE)
    val PurpleNeon = Color(0xFF7C4DFF)
    val CopperAccent = Color(0xFFC2623A)
    val RoseNeon = Color(0xFFF43F5E)
    val AmberDeficit = Color(0xFFF59E0B)
}

/**
 * Backward compatibility functions for screens
 */
@Composable
fun Modifier.atmosphericMeshBackground(isDark: Boolean = isAppInDarkTheme()): Modifier = pocketMoneyBackground(isDark)

@Composable
fun Modifier.glassSpecularBorder(
    shape: Shape = RoundedCornerShape(24.dp),
    isDark: Boolean = isAppInDarkTheme(),
    strokeWidth: Dp = 0.5.dp
): Modifier = specularBorder(shape, isDark, strokeWidth)

@Composable
fun Modifier.glassSurface(
    shape: Shape = RoundedCornerShape(24.dp),
    isDark: Boolean = isAppInDarkTheme(),
    backgroundColor: Color? = null,
    strokeWidth: Dp = 0.5.dp
): Modifier {
    val fill = backgroundColor ?: if (isDark) LiquidGlassTokens.DarkGlassBackground else LiquidGlassTokens.LightGlassBackground
    return this
        .clip(shape)
        .background(fill, shape)
        .specularBorder(shape, isDark, strokeWidth)
}
