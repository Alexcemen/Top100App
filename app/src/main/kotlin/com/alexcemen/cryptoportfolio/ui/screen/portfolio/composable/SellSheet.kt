package com.alexcemen.cryptoportfolio.ui.screen.portfolio.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore
import com.alexcemen.cryptoportfolio.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SellSheet(sellAmountInput: String, onEvent: (PortfolioStore.Event) -> Unit) {
    ModalBottomSheet(onDismissRequest = { onEvent(PortfolioStore.Event.CloseSellSheet) }) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Sell", style = AppTheme.textStyle.titleOne, color = AppTheme.colors.text.primary)
            OutlinedTextField(
                value = sellAmountInput,
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

@Preview(name = "SellSheet", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun SellSheetPreview() {
    AppTheme(themeDark = true) {
        SellSheet(sellAmountInput = "500", onEvent = {})
    }
}
