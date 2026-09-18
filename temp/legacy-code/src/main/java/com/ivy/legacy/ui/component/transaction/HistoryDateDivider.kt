package com.ivy.legacy.ui.component.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.design.system.isAppInDarkTheme
import com.ivy.legacy.IvyWalletComponentPreview
import com.ivy.legacy.utils.dateNowLocal
import com.ivy.legacy.utils.dateNowUTC
import com.ivy.legacy.utils.format
import com.ivy.legacy.utils.formatLocal
import com.ivy.ui.R
import com.ivy.ui.component.specularBorder
import java.time.LocalDate

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun HistoryDateDivider(
    date: LocalDate,
    spacerTop: Dp,
    baseCurrency: String,
    income: Double,
    expenses: Double
) {
    val isDark = isAppInDarkTheme()
    val pillShape = remember { CircleShape }

    Spacer(Modifier.height(spacerTop))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val today = dateNowLocal()

        Column {
            val dateText = remember(date, today) {
                date.formatLocal(
                    if (today.year == date.year) "MMMM dd." else "MMM dd. yyy"
                )
            }
            Text(
                text = dateText,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = UI.colors.pureInverse
                )
            )

            Spacer(Modifier.height(2.dp))

            val relativeDay = when (date) {
                today -> stringResource(R.string.today)
                today.minusDays(1) -> stringResource(R.string.yesterday)
                today.plusDays(1) -> stringResource(R.string.tomorrow)
                else -> date.formatLocal("EEEE")
            }

            Text(
                text = relativeDay.uppercase(),
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = UI.colors.mediumInverse
                )
            )
        }

        Spacer(Modifier.weight(1f))

        val cashflow = income - expenses
        if (cashflow != 0.0) {
            val amountColor = remember(cashflow, isDark) {
                if (cashflow > 0) {
                    if (isDark) Color(0xFF67E8F9) else Color(0xFF1E823E)
                } else {
                    if (isDark) Color(0xFFFECDD3) else Color(0xFFB91C1C)
                }
            }
            val pillBg = remember(isDark) {
                if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.50f)
            }

            Box(
                modifier = Modifier
                    .clip(pillShape)
                    .background(pillBg, pillShape)
                    .specularBorder(pillShape, isDark, 0.5.dp)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${if (cashflow > 0) "+" else ""}${cashflow.format(baseCurrency)} $baseCurrency",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )
                )
            }
        }
    }

    Spacer(Modifier.height(6.dp))
}

@Preview
@Composable
private fun Preview_Today() {
    IvyWalletComponentPreview {
        HistoryDateDivider(
            date = dateNowUTC(),
            spacerTop = 32.dp,
            baseCurrency = "BGN",
            income = 13.50,
            expenses = 256.13
        )
    }
}
