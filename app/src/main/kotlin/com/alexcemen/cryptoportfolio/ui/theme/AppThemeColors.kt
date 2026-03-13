package com.alexcemen.cryptoportfolio.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

interface AppThemeColors {
    interface Text {
        val primary: Color
        val placeholder: Color
        val red: Color
        val blue: Color
        val primaryUniform: Color
        val reverse: Color
        val blueTwo: Color
        val whiteUniform: Color
    }

    interface Background {
        val basic: Color
        val blue: Color
        val red: Color
        val green: Color
        val secondary: Color
        val secondaryTwo: Color
        val primary: Color
        val basicUniform: Color
        val mask: Color
        val yellow: Color
        val blueLight: Color
    }

    val text: Text
    val background: Background
}

class AppThemeColorsSchemes(
    val dark: AppThemeColors,
    val light: AppThemeColors,
    private val current: AppThemeColors,
) : AppThemeColors by current

@Immutable
object LightText : AppThemeColors.Text {
    override val primary: Color get() = Color(0xFF1C1C1C)
    override val placeholder: Color get() = Color(0xFF808080)
    override val red: Color get() = Color(0xFFD45858)
    override val blue: Color get() = Color(0xFF588BD4)
    override val primaryUniform: Color get() = Color(0xFF1C1C1C)
    override val reverse: Color get() = Color(0xFFFBFBFB)
    override val blueTwo: Color get() = Color(0xFF286BAF)
    override val whiteUniform: Color get() = Color(0xFFFFFFFF)
}

@Immutable
object LightBackground : AppThemeColors.Background {
    override val basic: Color get() = Color(0xFFFFFFFF)
    override val blue: Color get() = Color(0xFF588BD4)
    override val red: Color get() = Color(0xFFD45858)
    override val green: Color get() = Color(0xFF48CA72)
    override val secondary: Color get() = Color(0xFF6C6C6C)
    override val secondaryTwo: Color get() = Color(0xFFF9F9F9)
    override val primary: Color get() = Color(0xFF292929)
    override val basicUniform: Color get() = Color(0xFFFFFFFF)
    override val mask: Color get() = Color(0xFFCCCCCC)
    override val yellow: Color get() = Color(0xFFD9BA0D)
    override val blueLight: Color get() = Color(0xFF7EAFE1)
}

object LightColor : AppThemeColors {
    override val text: AppThemeColors.Text = LightText
    override val background: AppThemeColors.Background = LightBackground
}

@Immutable
object DarkText : AppThemeColors.Text {
    override val primary: Color get() = Color(0xFFFFFFFF)
    override val placeholder: Color get() = Color(0xFFA6A6A6)
    override val red: Color get() = Color(0xFFE67474)
    override val blue: Color get() = Color(0xFF588BD4)
    override val primaryUniform: Color get() = Color(0xFF1C1C1C)
    override val reverse: Color get() = Color(0xFF000000)
    override val blueTwo: Color get() = Color(0xFF4987C5)
    override val whiteUniform: Color get() = Color(0xFFFFFFFF)
}

@Immutable
object DarkBackground : AppThemeColors.Background {
    override val basic: Color get() = Color(0xFF262626)
    override val blue: Color get() = Color(0xFF376EB6)
    override val red: Color get() = Color(0xFF9A3B3B)
    override val green: Color get() = Color(0xFF33914D)
    override val secondary: Color get() = Color(0xFF9C9C9C)
    override val secondaryTwo: Color get() = Color(0xFF3F3F3F)
    override val primary: Color get() = Color(0xFFF6F6F6)
    override val basicUniform: Color get() = Color(0xFFFFFFFF)
    override val mask: Color get() = Color(0xFF8F8F8F)
    override val yellow: Color get() = Color(0xFFA68E0F)
    override val blueLight: Color get() = Color(0xFF3C6479)
}

object DarkColor : AppThemeColors {
    override val text: AppThemeColors.Text = DarkText
    override val background: AppThemeColors.Background = DarkBackground
}
