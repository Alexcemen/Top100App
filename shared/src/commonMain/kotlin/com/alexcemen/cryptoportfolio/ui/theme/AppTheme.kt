package com.alexcemen.cryptoportfolio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

@Composable
fun AppTheme(
    themeDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val color = remember(themeDark) { createThemeColor(themeDark) }
    val appThemeTypography = AppThemeTypography()

    CompositionLocalProvider(
        LocalThemedColors provides color,
        LocalTypography provides appThemeTypography,
        content = content,
    )
}

private fun createThemeColor(isDark: Boolean): AppThemeColorsSchemes {
    val theme = if (isDark) DarkColor else LightColor
    return AppThemeColorsSchemes(LightColor, LightColor, theme)
}

internal val LocalThemedColors =
    staticCompositionLocalOf {
        AppThemeColorsSchemes(LightColor, LightColor, LightColor)
    }

val LocalTypography =
    staticCompositionLocalOf {
        AppThemeTypography()
    }

object AppTheme {
    val colors @Composable get() = LocalThemedColors.current
    val textStyle @Composable get() = LocalTypography.current
}
