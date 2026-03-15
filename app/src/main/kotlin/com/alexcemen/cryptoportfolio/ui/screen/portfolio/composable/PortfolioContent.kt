package com.alexcemen.cryptoportfolio.ui.screen.portfolio.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore
import com.alexcemen.cryptoportfolio.ui.theme.AppTheme
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

private val avatarColors = listOf(
    Color(0xFF376EB6),
    Color(0xFF33914D),
    Color(0xFF9A3B3B),
    Color(0xFFA68E0F),
    Color(0xFF3C6479),
    Color(0xFF6B4A8A),
    Color(0xFF5C5C5C),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioContent(
    uiState: PortfolioStore.UiState,
    onEvent: (PortfolioStore.Event) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background.basic)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp,
            )
        ) {
            item { AppHeader(onEvent) }
            item { Spacer(Modifier.height(16.dp)) }
            item { BalanceCard(currencyFormat.format(uiState.totalUsdt)) }
            item { Spacer(Modifier.height(12.dp)) }
            item { ActionButtonsRow(uiState.isLoading, onEvent) }
            item { Spacer(Modifier.height(12.dp)) }

            if (uiState.coins.isEmpty() && !uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "No portfolio data. Tap Update to fetch.",
                            style = AppTheme.textStyle.captionOne,
                            color = AppTheme.colors.text.placeholder,
                        )
                    }
                }
            } else {
                item { CoinListCard(uiState.coins) }
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.4f),
                ) {}
                CircularProgressIndicator()
            }
        }

        if (uiState.showSellSheet) {
            ModalBottomSheet(onDismissRequest = { onEvent(PortfolioStore.Event.CloseSellSheet) }) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("Sell", style = AppTheme.textStyle.titleOne, color = AppTheme.colors.text.primary)
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
private fun AppHeader(onEvent: (PortfolioStore.Event) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = AppTheme.colors.text.primary,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "CryptoPortfolio",
                style = AppTheme.textStyle.titleOne,
                color = AppTheme.colors.text.primary,
            )
        }
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = AppTheme.colors.background.secondaryTwo,
        ) {
            IconButton(onClick = { onEvent(PortfolioStore.Event.NavigateToSettings) }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(18.dp),
                    tint = AppTheme.colors.text.primary,
                )
            }
        }
    }
}

@Composable
private fun BalanceCard(formattedBalance: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.background.secondaryTwo),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
            Text(
                "Portfolio",
                style = AppTheme.textStyle.captionOne,
                color = AppTheme.colors.text.placeholder,
            )
            Text(
                formattedBalance,
                style = AppTheme.textStyle.largeTitleOne,
                color = AppTheme.colors.text.primary,
            )
        }
    }
}

@Composable
private fun ActionButtonsRow(isLoading: Boolean, onEvent: (PortfolioStore.Event) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ActionButton("Update", Icons.Default.Refresh, !isLoading, Modifier.weight(1f)) {
            onEvent(PortfolioStore.Event.Update)
        }
        ActionButton("Rebalance", Icons.Default.SwapVert, !isLoading, Modifier.weight(1f)) {
            onEvent(PortfolioStore.Event.Rebalance)
        }
        ActionButton("Sell", Icons.AutoMirrored.Filled.CallMade, !isLoading, Modifier.weight(1f)) {
            onEvent(PortfolioStore.Event.OpenSellSheet)
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val contentAlpha = if (enabled) 1f else 0.4f
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50.dp),
        color = AppTheme.colors.background.secondaryTwo,
        onClick = onClick,
        enabled = enabled,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = AppTheme.colors.text.primary.copy(alpha = contentAlpha),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style = AppTheme.textStyle.captionOne,
                color = AppTheme.colors.text.primary.copy(alpha = contentAlpha),
            )
        }
    }
}

@Composable
private fun CoinListCard(coins: List<PortfolioStore.CoinUi>) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.background.secondaryTwo),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            coins.forEachIndexed { index, coin ->
                CoinListItem(coin)
                if (index < coins.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = AppTheme.colors.background.secondary,
                        thickness = 0.5.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun CoinListItem(coin: PortfolioStore.CoinUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoinAvatar(coin.symbol, coin.logoUrl)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(coin.symbol, style = AppTheme.textStyle.subheadOne, color = AppTheme.colors.text.primary)
            Text(coin.priceUsdt, style = AppTheme.textStyle.captionOne, color = AppTheme.colors.text.placeholder)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(coin.totalPositionUsdt, style = AppTheme.textStyle.subheadOne, color = AppTheme.colors.text.primary)
            Text("${coin.quantity} ${coin.symbol}", style = AppTheme.textStyle.captionOne, color = AppTheme.colors.text.placeholder)
        }
    }
}

@Composable
private fun CoinAvatar(symbol: String, logoUrl: String?) {
    val color = avatarColors[abs(symbol.hashCode()) % avatarColors.size]
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            symbol.first().uppercaseChar().toString(),
            style = AppTheme.textStyle.subtitleOne,
            color = Color.White,
        )
        if (logoUrl != null) {
            AsyncImage(
                model = logoUrl,
                contentDescription = symbol,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

private val previewCoins = listOf(
    PortfolioStore.CoinUi(symbol = "BTC", priceUsdt = "$65,432.10", quantity = "0.153", totalPositionUsdt = "$10,011.11"),
    PortfolioStore.CoinUi(symbol = "ETH", priceUsdt = "$3,210.00", quantity = "1.842", totalPositionUsdt = "$5,912.82"),
    PortfolioStore.CoinUi(symbol = "SOL", priceUsdt = "$142.50", quantity = "21.0", totalPositionUsdt = "$2,992.50"),
)

@Preview(name = "Portfolio — with coins", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PortfolioContentPreview() {
    AppTheme(themeDark = true) {
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

@Preview(name = "Portfolio — sell sheet", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PortfolioContentSellPreview() {
    AppTheme(themeDark = true) {
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

@Preview(name = "Portfolio — empty", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PortfolioContentEmptyPreview() {
    AppTheme(themeDark = true) {
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

@Preview(name = "Portfolio — loading", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PortfolioContentLoadingPreview() {
    AppTheme(themeDark = true) {
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
