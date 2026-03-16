package com.alexcemen.cryptoportfolio.ui.screen.portfolio.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.alexcemen.cryptoportfolio.ui.theme.AppTheme

@Composable
internal fun CoinAvatar(symbol: String, color: Color, logoUrl: String?) {
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
                    .size(42.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
