package com.alexcemen.cryptoportfolio.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.alexcemen.cryptoportfolio.ui.navigation.LocalNavigator
import com.alexcemen.cryptoportfolio.ui.navigation.Navigator
import com.alexcemen.cryptoportfolio.ui.navigation.Screen
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.composable.PortfolioScreenContent
import com.alexcemen.cryptoportfolio.ui.screen.settings.composable.SettingsScreenContent
import com.alexcemen.cryptoportfolio.ui.theme.AppTheme

@Composable
expect fun BackHandler(enabled: Boolean, onBack: () -> Unit)

@Composable
fun App() {
    val navigator = remember { Navigator() }

    CompositionLocalProvider(LocalNavigator provides navigator) {
        AppTheme {
            BackHandler(enabled = navigator.canGoBack) {
                navigator.back()
            }
            when (navigator.current) {
                Screen.Portfolio -> PortfolioScreenContent()
                Screen.Settings -> SettingsScreenContent()
            }
        }
    }
}
