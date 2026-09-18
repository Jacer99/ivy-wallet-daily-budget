package com.ivy.design.api.systems

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.base.legacy.Theme
import com.ivy.design.api.IvyDesign
import com.ivy.design.l0_system.*
import com.ivy.ui.R

@Deprecated("Old design system. Use `:ivy-design` and Material3")
abstract class IvyWalletDesign : IvyDesign {
    companion object {
        const val OPEN_SANS_BASELINE_SHIFT = 0.075f
        const val RALEWAY_BASELINE_SHIFT = 0.2f
    }

    @Deprecated("Old design system. Use `:ivy-design` and Material3")
    override fun typography(): IvyTypography {
        val openSans = FontFamily(
            Font(R.font.opensans_regular, FontWeight.Normal),
            Font(R.font.opensans_regular, FontWeight.Medium),
            Font(R.font.opensans_bold, FontWeight.Black),
            Font(R.font.opensans_semibold, FontWeight.SemiBold),
            Font(R.font.opensans_bold, FontWeight.Bold),
            Font(R.font.opensans_extrabold, FontWeight.ExtraBold),
        )

        val raleWay = FontFamily(
            Font(R.font.raleway_regular, FontWeight.Normal),
            Font(R.font.raleway_medium, FontWeight.Medium),
            Font(R.font.raleway_black, FontWeight.Black),
            Font(R.font.raleway_light, FontWeight.Light),
            Font(R.font.raleway_semibold, FontWeight.SemiBold),
            Font(R.font.raleway_bold, FontWeight.Bold),
            Font(R.font.raleway_extrabold, FontWeight.ExtraBold),
        )

        val h1 = 40.sp
        val h2 = 32.sp
        val b1 = 20.sp
        val b2 = 16.sp
        val c = 12.sp

        return object : IvyTypography {
            override val h1 = TextStyle(
                fontFamily = raleWay,
                fontWeight = FontWeight.Black,
                fontSize = h1,
                baselineShift = BaselineShift(RALEWAY_BASELINE_SHIFT),
            )
            override val h2 = TextStyle(
                fontFamily = raleWay,
                fontWeight = FontWeight.ExtraBold,
                fontSize = h2,
                baselineShift = BaselineShift(RALEWAY_BASELINE_SHIFT),
            )
            override val b1 = TextStyle(
                fontFamily = raleWay,
                fontWeight = FontWeight.Bold,
                fontSize = b1,
                baselineShift = BaselineShift(RALEWAY_BASELINE_SHIFT),
            )
            override val b2 = TextStyle(
                fontFamily = raleWay,
                fontWeight = FontWeight.Medium,
                fontSize = b2,
                baselineShift = BaselineShift(RALEWAY_BASELINE_SHIFT),
            )
            override val c = TextStyle(
                fontFamily = raleWay,
                fontWeight = FontWeight.ExtraBold,
                fontSize = c,
                baselineShift = BaselineShift(RALEWAY_BASELINE_SHIFT),
            )

            override val nH1 = TextStyle(
                fontFamily = openSans,
                fontWeight = FontWeight.Bold,
                fontSize = h1,
                baselineShift = BaselineShift(OPEN_SANS_BASELINE_SHIFT),
            )
            override val nH2 = TextStyle(
                fontFamily = openSans,
                fontWeight = FontWeight.Bold,
                fontSize = h2,
                baselineShift = BaselineShift(OPEN_SANS_BASELINE_SHIFT),
            )
            override val nB1 = TextStyle(
                fontFamily = openSans,
                fontWeight = FontWeight.Bold,
                fontSize = b1,
                baselineShift = BaselineShift(OPEN_SANS_BASELINE_SHIFT),
            )
            override val nB2 = TextStyle(
                fontFamily = openSans,
                fontWeight = FontWeight.Normal,
                fontSize = b2,
                baselineShift = BaselineShift(OPEN_SANS_BASELINE_SHIFT),
            )
            override val nC = TextStyle(
                fontFamily = openSans,
                fontWeight = FontWeight.Bold,
                fontSize = c,
                baselineShift = BaselineShift(OPEN_SANS_BASELINE_SHIFT),
            )
        }
    }

    @Deprecated("Old design system. Use `:ivy-design` and Material3")
    override fun colors(theme: Theme, isDarkModeEnabled: Boolean): IvyColors {
        return when (theme) {
            Theme.LIGHT -> object : IvyColors {
                override val pure = Color(0xFFF4F3F8)
                override val pureInverse = Color(0xFF111111)
                override val gray = Gray
                override val medium = MediumWhite
                override val mediumInverse = Color(0xFF111111).copy(alpha = 0.45f)

                override val primary = Color(0xFF7C4DFF)
                override val primary1 = Color(0xFFE8DDFF)

                override val green = Color(0xFF00C853)
                override val green1 = Color(0xFF00BFA5)

                override val orange = Color(0xFFFF6E40)
                override val orange1 = Color(0xFFFFDAD0)

                override val red = Color(0xFFFF5252)
                override val red1 = Color(0xFFFFDAD6)
                override val red1Inverse = Color(0xFF410002)

                override val isLight = true
            }

            Theme.DARK -> object : IvyColors {
                override val pure = Color(0xFF0B0B14)
                override val pureInverse = Color.White
                override val gray = Gray
                override val medium = MediumBlack
                override val mediumInverse = Color.White.copy(alpha = 0.50f)

                override val primary = Color(0xFF9D7BFF)
                override val primary1 = Color(0xFF4B2FA0)

                override val green = Color(0xFF5EE87F)
                override val green1 = Color(0xFF005046)

                override val orange = Color(0xFFFF8A65)
                override val orange1 = Color(0xFF7C2E10)

                override val red = Color(0xFFFF8A8A)
                override val red1 = Color(0xFF93000A)
                override val red1Inverse = Color(0xFFFFDAD6)

                override val isLight = false
            }

            Theme.AMOLED_DARK -> object : IvyColors {
                override val pure = Color(0xFF000000)
                override val pureInverse = Color.White
                override val gray = Gray
                override val medium = MediumBlack
                override val mediumInverse = Color.White.copy(alpha = 0.50f)

                override val primary = Color(0xFF9D7BFF)
                override val primary1 = Color(0xFF4B2FA0)

                override val green = Color(0xFF5EE87F)
                override val green1 = Color(0xFF005046)

                override val orange = Color(0xFFFF8A65)
                override val orange1 = Color(0xFF7C2E10)

                override val red = Color(0xFFFF8A8A)
                override val red1 = Color(0xFF93000A)
                override val red1Inverse = Color(0xFFFFDAD6)

                override val isLight = false
            }

            Theme.AUTO -> if (isDarkModeEnabled) {
                colors(Theme.DARK, true)
            } else {
                colors(
                    Theme.LIGHT,
                    false
                )
            }
        }
    }

    @Deprecated("Old design system. Use `:ivy-design` and Material3")
    override fun shapes(): IvyShapes {
        return object : IvyShapes() {
            override val r1 = RoundedCornerShape(32.dp)
            override val r1Top = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            override val r1Bot = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)

            override val r2 = RoundedCornerShape(24.dp)
            override val r2Top = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            override val r2Bot = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)

            override val r3 = RoundedCornerShape(20.dp)
            override val r3Top = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            override val r3Bot = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)

            override val r4 = RoundedCornerShape(16.dp)
            override val r4Top = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            override val r4Bot = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        }
    }
}
