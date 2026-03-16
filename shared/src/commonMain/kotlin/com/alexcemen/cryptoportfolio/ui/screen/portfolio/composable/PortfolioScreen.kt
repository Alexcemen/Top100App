package com.alexcemen.cryptoportfolio.ui.screen.portfolio.composable

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.alexcemen.cryptoportfolio.ui.mvi.sideEffect
import com.alexcemen.cryptoportfolio.ui.navigation.LocalNavigator
import com.alexcemen.cryptoportfolio.ui.navigation.Screen
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioScreenModel
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore
import kotlinx.coroutines.launch
import org.koin.compose.getKoin

@Composable
fun PortfolioScreenContent() {
    val koin = getKoin()
    val screenModel = remember { koin.get<PortfolioScreenModel>() }
    val uiState by screenModel.uiState.collectAsState()
    val navigator = LocalNavigator.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) { onDispose { screenModel.onCleared() } }

    screenModel.sideEffect { effect ->
        when (effect) {
            is PortfolioStore.SideEffect.ShowSnackbar ->
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
            PortfolioStore.SideEffect.NavigateToSettings ->
                navigator.navigate(Screen.Settings)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PortfolioContent(
            uiState = uiState,
            onEvent = screenModel::onEvent,
            contentPadding = padding,
        )
    }
}
