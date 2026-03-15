package com.alexcemen.cryptoportfolio.ui.screen.portfolio.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore
import com.alexcemen.cryptoportfolio.ui.theme.AppTheme
import com.alexcemen.cryptoportfolio.ui.theme.avatarColors

@Composable
internal fun CoinListCard(coins: List<PortfolioStore.CoinUi>) {
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
    val avatarColor = AppTheme.colors.avatarColors[coin.avatarColorIndex]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoinAvatar(symbol = coin.symbol, color = avatarColor, logoUrl = coin.logoUrl)
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

private val previewCoins = listOf(
    PortfolioStore.CoinUi(symbol = "BTC", priceUsdt = "$65,432.10", quantity = "0.153000", totalPositionUsdt = "$10,011.11", avatarColorIndex = 0),
    PortfolioStore.CoinUi(symbol = "ETH", priceUsdt = "$3,210.00", quantity = "1.842000", totalPositionUsdt = "$5,912.82", avatarColorIndex = 1),
    PortfolioStore.CoinUi(symbol = "SOL", priceUsdt = "$142.50", quantity = "21.000000", totalPositionUsdt = "$2,992.50", avatarColorIndex = 2),
)

@Preview(name = "CoinListCard", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun CoinListCardPreview() {
    AppTheme(themeDark = true) {
        CoinListCard(coins = previewCoins)
    }
}
