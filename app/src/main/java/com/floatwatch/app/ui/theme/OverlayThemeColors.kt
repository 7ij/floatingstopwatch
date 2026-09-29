package com.floatwatch.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.floatwatch.app.data.OverlayTheme

data class OverlayThemeColors(
    val background: Color,
    val surface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val accent: Color,
    val borderColor: Color
)

fun getOverlayThemeColors(theme: OverlayTheme): OverlayThemeColors {
    return when (theme) {
        OverlayTheme.MINIMAL_DARK -> OverlayThemeColors(
            background = Color(0xFF14171F),
            surface = Color(0xFF202430),
            primaryText = Color(0xFFFFFFFF),
            secondaryText = Color(0xFF9EAAAF),
            accent = Color(0xFF38EF7D),
            borderColor = Color(0xFF2C3242)
        )
        OverlayTheme.MINIMAL_LIGHT -> OverlayThemeColors(
            background = Color(0xFFF7F9FC),
            surface = Color(0xFFEBEFF5),
            primaryText = Color(0xFF181C24),
            secondaryText = Color(0xFF6A7282),
            accent = Color(0xFF0070F3),
            borderColor = Color(0xFFD6DCE5)
        )
        OverlayTheme.CYBERPUNK -> OverlayThemeColors(
            background = Color(0xFF0A0518),
            surface = Color(0xFF1F1138),
            primaryText = Color(0xFF00FFCC),
            secondaryText = Color(0xFFFF007F),
            accent = Color(0xFFFFEE00),
            borderColor = Color(0xFF00FFCC)
        )
        OverlayTheme.GLASSMORPHISM -> OverlayThemeColors(
            background = Color(0x99232938),
            surface = Color(0x663B445C),
            primaryText = Color(0xFFF0F4F8),
            secondaryText = Color(0xFFBAC7D5),
            accent = Color(0xFF70A6FF),
            borderColor = Color(0x88A3B8CC)
        )
        OverlayTheme.OLED_RED -> OverlayThemeColors(
            background = Color(0xFF000000),
            surface = Color(0xFF120303),
            primaryText = Color(0xFFFF2E4C),
            secondaryText = Color(0xFF99182C),
            accent = Color(0xFFFF4D67),
            borderColor = Color(0x4DFF2E4C)
        )
    }
}
