package com.alexcemen.cryptoportfolio.ui.screen.settings.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alexcemen.cryptoportfolio.RootNavigation
import com.alexcemen.cryptoportfolio.ui.mvi.sideEffect
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsStore
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsViewModel
import com.alexcemen.cryptoportfolio.ui.theme.AppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        containerColor = AppTheme.colors.background.basic,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = AppTheme.textStyle.titleOne,
                        color = AppTheme.colors.text.primary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { nav?.removeLastOrNull() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AppTheme.colors.text.primary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.background.basic,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        SettingsContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            contentPadding = padding,
        )
    }
}
