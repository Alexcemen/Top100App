package com.alexcemen.cryptoportfolio.ui.screen.portfolio.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore
import com.alexcemen.cryptoportfolio.ui.theme.AppTheme
import cryptoportfolio.shared.generated.resources.Res
import cryptoportfolio.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioContent(
    uiState: PortfolioStore.UiState,
    onEvent: (PortfolioStore.Event) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
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
            item { BalanceCard(uiState.totalUsdt) }
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
                            stringResource(Res.string.portfolio_empty),
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
            SellSheet(sellAmountInput = uiState.sellAmountInput, onEvent = onEvent)
        }
    }
}
