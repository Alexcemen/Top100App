package com.alexcemen.cryptoportfolio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.alexcemen.cryptoportfolio.ui.navigation.PortfolioScreen
import com.alexcemen.cryptoportfolio.ui.navigation.SettingsScreen
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.composable.PortfolioScreenContent
import com.alexcemen.cryptoportfolio.ui.screen.settings.composable.SettingsScreenContent
import com.alexcemen.cryptoportfolio.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

val RootNavigation = compositionLocalOf<NavBackStack<NavKey>?> { null }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val backStack = rememberNavBackStack(PortfolioScreen())
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    entryProvider = entryProvider {
                        entry<PortfolioScreen> {
                            PortfolioScreenContent()
                        }
                        entry<SettingsScreen> {
                            SettingsScreenContent()
                        }
                    }
                )
            }
        }
    }
}
