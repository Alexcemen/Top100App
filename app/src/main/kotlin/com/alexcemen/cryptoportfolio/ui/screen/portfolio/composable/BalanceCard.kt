package com.alexcemen.cryptoportfolio.ui.screen.portfolio.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alexcemen.cryptoportfolio.R
import com.alexcemen.cryptoportfolio.ui.theme.AppTheme

@Composable
internal fun BalanceCard(formattedBalance: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.background.secondaryTwo),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
            Text(
                stringResource(R.string.portfolio_title),
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

@Preview(name = "BalanceCard", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun BalanceCardPreview() {
    AppTheme(themeDark = true) {
        BalanceCard(formattedBalance = "$18,916.43")
    }
}
