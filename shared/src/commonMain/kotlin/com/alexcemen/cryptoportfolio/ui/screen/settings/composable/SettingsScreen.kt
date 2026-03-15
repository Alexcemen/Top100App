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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.alexcemen.cryptoportfolio.ui.mvi.sideEffect
import com.alexcemen.cryptoportfolio.ui.navigation.LocalNavigator
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsScreenModel
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsStore
import com.alexcemen.cryptoportfolio.ui.theme.AppTheme
import cryptoportfolio.shared.generated.resources.Res
import cryptoportfolio.shared.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(screenModel: SettingsScreenModel = koinInject()) {
    val uiState by screenModel.uiState.collectAsState()
    val navigator = LocalNavigator.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) { onDispose { screenModel.onCleared() } }

    screenModel.sideEffect { effect ->
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
                        stringResource(Res.string.settings_title),
                        style = AppTheme.textStyle.titleOne,
                        color = AppTheme.colors.text.primary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigator.back() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.cd_back),
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
            onEvent = screenModel::onEvent,
            contentPadding = padding,
        )
    }
}
