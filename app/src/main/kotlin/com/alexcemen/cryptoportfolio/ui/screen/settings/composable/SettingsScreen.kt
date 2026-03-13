package com.alexcemen.cryptoportfolio.ui.screen.settings.composable

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alexcemen.cryptoportfolio.RootNavigation
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsStore
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsViewModel
import com.alexcemen.cryptoportfolio.ui.mvi.sideEffect
import kotlinx.coroutines.launch

@Composable
fun SettingsScreenContent(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nav = RootNavigation.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    viewModel.sideEffect { effect ->
        when (effect) {
            is SettingsStore.SideEffect.ShowSnackbar ->
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        SettingsContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onBack = { nav?.removeLastOrNull() },
            contentPadding = padding,
        )
    }
}
