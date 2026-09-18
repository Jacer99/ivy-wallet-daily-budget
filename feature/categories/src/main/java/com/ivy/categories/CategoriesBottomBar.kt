package com.ivy.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ivy.design.l0_system.UI
import com.ivy.legacy.IvyWalletPreview
import com.ivy.ui.R
import com.ivy.ui.component.LiquidGlassCard
import com.ivy.ui.component.specularBorder

@Composable
internal fun BoxScope.CategoriesBottomBar(
    onClose: () -> Unit,
    onAddCategory: () -> Unit
) {
    val isDark = !UI.colors.isLight
    val plusGradient = if (isDark) {
        Brush.linearGradient(listOf(Color(0xFF7C4DFF), Color(0xFF00F2FE)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF9E4622), Color(0xFFC2623A)))
    }

    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = 20.dp, start = 20.dp, end = 20.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Frosted circular back button
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    if (isDark) Color.White.copy(alpha = 0.08f)
                    else Color.White.copy(alpha = 0.65f)
                )
                .specularBorder(CircleShape, isDark, strokeWidth = 0.5.dp)
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = UI.colors.pureInverse,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        // Floating frosted glass pill with gradient '+' icon and dynamic text
        LiquidGlassCard(
            shape = CircleShape,
            isDark = isDark,
            fillColor = if (isDark) Color(0xFF1B1838).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f),
            strokeWidth = 0.5.dp,
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .clickable { onAddCategory() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(plusGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = stringResource(R.string.add_category),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = UI.colors.pureInverse
                    )
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewBottomBar() {
    com.ivy.legacy.IvyWalletPreview {
        CategoriesBottomBar(
            onAddCategory = {},
            onClose = {}
        )
    }
}

