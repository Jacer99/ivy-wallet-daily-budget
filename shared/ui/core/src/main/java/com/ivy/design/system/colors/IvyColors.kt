package com.ivy.design.system.colors

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
object IvyColors {
    val White = Color(0xFFFFFFFF)
    val OffWhite = Color(0xFFFDFBFF)
    val ExtraLightGray = Color(0xFFE7E0EC)
    val LightGray = Color(0xFFCAC4D0)
    val Gray = Color(0xFF7A757F)
    val DarkGray = Color(0xFF49454F)
    val ExtraDarkGray = Color(0xFF1C1B1F)
    val Black = Color(0xFF1C1B1F)
    val TrueBlack = Color(0xFF000000)

    val Red = ColorShades(
        extraLight = Color(0xFFFFDAD6),
        light = Color(0xFFFFDAD6),
        kindaLight = Color(0xFFFF8A8A),
        primary = Color(0xFFFF5252),
        kindaDark = Color(0xFF93000A),
        dark = Color(0xFF690005),
        extraDark = Color(0xFF410002),
    )
    val Orange = ColorShades(
        extraLight = Color(0xFFFFDAD0),
        light = Color(0xFFFFDAD0),
        kindaLight = Color(0xFFFF8A65),
        primary = Color(0xFFFF6E40),
        kindaDark = Color(0xFF7C2E10),
        dark = Color(0xFF5C1900),
        extraDark = Color(0xFF3B0900),
    )
    val Yellow = ColorShades(
        extraLight = Color(0xFFF5E9AB),
        light = Color(0xFFF5E287),
        kindaLight = Color(0xFFF5DC62),
        primary = Color(0xFFF5D018),
        kindaDark = Color(0xFFCCAD14),
        dark = Color(0xFFA38B10),
        extraDark = Color(0xFF7A680C),
    )
    val Green = ColorShades(
        extraLight = Color(0xFFB2EBE0),
        light = Color(0xFFB2EBE0),
        kindaLight = Color(0xFF4DD0C7),
        primary = Color(0xFF00BFA5),
        kindaDark = Color(0xFF005046),
        dark = Color(0xFF003730),
        extraDark = Color(0xFF002019),
    )
    val Blue = ColorShades(
        extraLight = Color(0xFFABD0F5),
        light = Color(0xFF87BEF5),
        kindaLight = Color(0xFF62ABF5),
        primary = Color(0xFF3193F5),
        kindaDark = Color(0xFF3380CC),
        dark = Color(0xFF24598F),
        extraDark = Color(0xFF153352),
    )
    val Purple = ColorShades(
        extraLight = Color(0xFFE8DDFF),
        light = Color(0xFFE8DDFF),
        kindaLight = Color(0xFF9D7BFF),
        primary = Color(0xFF7C4DFF),
        kindaDark = Color(0xFF4B2FA0),
        dark = Color(0xFF2C0079),
        extraDark = Color(0xFF21005D),
    )
    val Pink = ColorShades(
        extraLight = Color(0xFFF5ABD0),
        light = Color(0xFFF587BE),
        kindaLight = Color(0xFFF562AB),
        primary = Color(0xFFF53D99),
        kindaDark = Color(0xFFCC3380),
        dark = Color(0xFF8F2459),
        extraDark = Color(0xFF521433),
    )
    val Tertiary = ColorShades(
        extraLight = Color(0xFFFFDAD0),
        light = Color(0xFFFFDAD0),
        kindaLight = Color(0xFFFF8A65),
        primary = Color(0xFFFF6E40),
        kindaDark = Color(0xFF7C2E10),
        dark = Color(0xFF5C1900),
        extraDark = Color(0xFF3B0900),
    )
}
