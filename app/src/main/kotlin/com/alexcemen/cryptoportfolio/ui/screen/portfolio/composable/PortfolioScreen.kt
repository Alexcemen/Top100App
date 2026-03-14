package com.alexcemen.cryptoportfolio.ui.screen.portfolio.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alexcemen.cryptoportfolio.RootNavigation
import com.alexcemen.cryptoportfolio.ui.mvi.sideEffect
import com.alexcemen.cryptoportfolio.ui.navigation.SettingsScreen
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
        topBar = {
            TopAppBar(
                title = { Text("Portfolio") },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(PortfolioStore.Event.NavigateToSettings) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.onEvent(PortfolioStore.Event.Update) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.weight(1f),
                ) { Text("Update") }
                Button(
                    onClick = { viewModel.onEvent(PortfolioStore.Event.Rebalance) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.weight(1f),
                ) { Text("Rebalance") }
                Button(
                    onClick = { viewModel.onEvent(PortfolioStore.Event.OpenSellSheet) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.weight(1f),
                ) { Text("Sell") }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PortfolioContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            contentPadding = padding,
        )
    }
}
