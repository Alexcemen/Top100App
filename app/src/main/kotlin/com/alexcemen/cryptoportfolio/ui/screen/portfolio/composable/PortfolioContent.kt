package com.alexcemen.cryptoportfolio.ui.screen.portfolio.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioContent(
    uiState: PortfolioStore.UiState,
    onEvent: (PortfolioStore.Event) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
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
                    Text("Sell", style = MaterialTheme.typography.titleLarge)
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

private val previewCoins = listOf(
    PortfolioStore.CoinUi(symbol = "BTC", priceUsdt = "$65,432.10", quantity = "0.153", totalPositionUsdt = "$10,011.11"),
    PortfolioStore.CoinUi(symbol = "ETH", priceUsdt = "$3,210.00", quantity = "1.842", totalPositionUsdt = "$5,912.82"),
    PortfolioStore.CoinUi(symbol = "SOL", priceUsdt = "$142.50", quantity = "21.0", totalPositionUsdt = "$2,992.50"),
)

@Preview(name = "Portfolio — with coins", showBackground = true)
@Composable
private fun PortfolioContentPreview() {
    MaterialTheme {
        PortfolioContent(
            uiState = PortfolioStore.UiState(
                coins = previewCoins,
                totalUsdt = 18916.43,
                isLoading = false,
                showSellSheet = false,
                sellAmountInput = "",
            ),
            onEvent = {},
        )
    }
}

@Preview(name = "Portfolio — with coins", showBackground = true)
@Composable
private fun PortfolioContentSellPreview() {
    MaterialTheme {
        PortfolioContent(
            uiState = PortfolioStore.UiState(
                coins = previewCoins,
                totalUsdt = 18916.43,
                isLoading = false,
                showSellSheet = true,
                sellAmountInput = "",
            ),
            onEvent = {},
        )
    }
}

@Preview(name = "Portfolio — empty", showBackground = true)
@Composable
private fun PortfolioContentEmptyPreview() {
    MaterialTheme {
        PortfolioContent(
            uiState = PortfolioStore.UiState(
                coins = emptyList(),
                totalUsdt = 0.0,
                isLoading = false,
                showSellSheet = false,
                sellAmountInput = "",
            ),
            onEvent = {},
        )
    }
}

@Preview(name = "Portfolio — loading", showBackground = true)
@Composable
private fun PortfolioContentLoadingPreview() {
    MaterialTheme {
        PortfolioContent(
            uiState = PortfolioStore.UiState(
                coins = previewCoins,
                totalUsdt = 18916.43,
                isLoading = true,
                showSellSheet = false,
                sellAmountInput = "",
            ),
            onEvent = {},
        )
    }
}

@Preview(name = "CoinCard", showBackground = true)
@Composable
private fun CoinCardPreview() {
    MaterialTheme {
        CoinCard(coin = previewCoins.first())
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
