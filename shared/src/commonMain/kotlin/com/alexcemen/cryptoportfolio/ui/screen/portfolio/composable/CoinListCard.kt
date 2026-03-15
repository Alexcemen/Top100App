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
