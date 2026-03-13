package com.alexcemen.cryptoportfolio.ui.screen.portfolio.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioContent(
    uiState: PortfolioStore.UiState,
    onEvent: (PortfolioStore.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Portfolio") },
                    actions = {
                        IconButton(onClick = { onEvent(PortfolioStore.Event.NavigateToSettings) }) {
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
                        onClick = { onEvent(PortfolioStore.Event.Update) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.weight(1f),
                    ) { Text("Update") }
                    Button(
                        onClick = { onEvent(PortfolioStore.Event.Rebalance) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.weight(1f),
                    ) { Text("Rebalance") }
                    Button(
                        onClick = { onEvent(PortfolioStore.Event.OpenSellSheet) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.weight(1f),
                    ) { Text("Sell") }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                // Header card
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(
                        text = currencyFormat.format(uiState.totalUsdt),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp),
                    )
                }

                if (uiState.coins.isEmpty() && !uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No portfolio data. Tap Update to fetch.")
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.coins, key = { it.symbol }) { coin ->
                            CoinCard(coin = coin)
                        }
                    }
                }
            }
        }

        // Loading overlay
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.3f),
                ) {}
                CircularProgressIndicator()
            }
        }

        // Sell bottom sheet
        if (uiState.showSellSheet) {
            ModalBottomSheet(onDismissRequest = { onEvent(PortfolioStore.Event.CloseSellSheet) }) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Sell Portfolio", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = uiState.sellAmountInput,
                        onValueChange = { onEvent(PortfolioStore.Event.SetSellAmount(it)) },
                        label = { Text("USDT amount") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0.25f to "25%", 0.5f to "50%", 0.75f to "75%", 1.0f to "100%").forEach { (pct, label) ->
                            OutlinedButton(
                                onClick = { onEvent(PortfolioStore.Event.SetSellPercent(pct)) },
                                modifier = Modifier.weight(1f),
                            ) { Text(label) }
                        }
                    }
                    Button(
                        onClick = { onEvent(PortfolioStore.Event.Sell) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Sell") }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun CoinCard(coin: PortfolioStore.CoinUi) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(coin.symbol, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(coin.totalPositionUsdt, style = MaterialTheme.typography.titleMedium)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(coin.priceUsdt, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(coin.quantity, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
