package com.alexcemen.cryptoportfolio.ui.screen.portfolio.composable

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alexcemen.cryptoportfolio.RootNavigation
import com.alexcemen.cryptoportfolio.ui.mvi.sideEffect
import com.alexcemen.cryptoportfolio.ui.navigation.SettingsScreen
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioViewModel
import kotlinx.coroutines.launch

@Composable
fun PortfolioScreenContent(viewModel: PortfolioViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nav = RootNavigation.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    viewModel.sideEffect { effect ->
        when (effect) {
            is PortfolioStore.SideEffect.ShowSnackbar ->
                scope.launch { snackbarHostState.showSnackbar(effect.message) }

            PortfolioStore.SideEffect.NavigateToSettings ->
                nav?.add(SettingsScreen())
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PortfolioContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            contentPadding = padding,
        )
    }
}
