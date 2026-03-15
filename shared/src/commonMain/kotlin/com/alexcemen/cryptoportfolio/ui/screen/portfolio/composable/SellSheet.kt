package com.alexcemen.cryptoportfolio.ui.screen.portfolio.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore
import com.alexcemen.cryptoportfolio.ui.theme.AppTheme
import cryptoportfolio.shared.generated.resources.Res
import cryptoportfolio.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SellSheet(sellAmountInput: String, onEvent: (PortfolioStore.Event) -> Unit) {
    ModalBottomSheet(
        onDismissRequest = { onEvent(PortfolioStore.Event.CloseSellSheet) },
        containerColor = AppTheme.colors.background.secondaryTwo,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(Res.string.action_sell), style = AppTheme.textStyle.titleOne, color = AppTheme.colors.text.primary)
            OutlinedTextField(
                value = sellAmountInput,
                onValueChange = { onEvent(PortfolioStore.Event.SetSellAmount(it)) },
                label = { Text(stringResource(Res.string.sell_usdt_amount_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = AppTheme.textStyle.bodyOne,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppTheme.colors.text.primary,
                    unfocusedTextColor = AppTheme.colors.text.primary,
                    focusedBorderColor = AppTheme.colors.text.primary,
                    unfocusedBorderColor = AppTheme.colors.text.placeholder,
                    focusedLabelColor = AppTheme.colors.text.primary,
                    unfocusedLabelColor = AppTheme.colors.text.placeholder,
                    cursorColor = AppTheme.colors.text.primary,
                ),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0.25f to "25%", 0.5f to "50%", 0.75f to "75%", 1.0f to "100%").forEach { (pct, label) ->
                    Surface(
                        onClick = { onEvent(PortfolioStore.Event.SetSellPercent(pct)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50.dp),
                        color = AppTheme.colors.background.secondary,
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 12.dp),
                        ) {
                            Text(label, style = AppTheme.textStyle.captionOne, color = AppTheme.colors.text.primary)
                        }
                    }
                }
            }
            Surface(
                onClick = { onEvent(PortfolioStore.Event.Sell) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50.dp),
                color = AppTheme.colors.background.primary,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = 16.dp),
                ) {
                    Text(stringResource(Res.string.action_sell), style = AppTheme.textStyle.bodyOne, color = AppTheme.colors.text.primaryUniform)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
