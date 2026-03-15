package com.alexcemen.cryptoportfolio.ui.screen.portfolio

import com.alexcemen.cryptoportfolio.ui.mvi.Reducer
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

class PortfolioReducer @Inject constructor() : Reducer<PortfolioStore.State, PortfolioStore.UiState> {
    override fun reduce(state: PortfolioStore.State) = PortfolioStore.UiState(
        coins = state.portfolio.coins.sortedByDescending { it.totalPositionUsdt }.map { coin ->
            PortfolioStore.CoinUi(
                symbol = coin.symbol,
                priceUsdt = "$%.4f".format(Locale.US, coin.priceUsdt),
                quantity = "%.6f".format(Locale.US, coin.quantity),
                totalPositionUsdt = "$%.2f".format(Locale.US, coin.totalPositionUsdt),
                logoUrl = coin.logoUrl,
                avatarColorIndex = abs(coin.symbol.hashCode()) % AVATAR_COLORS_COUNT,
            )
        },
        totalUsdt = NumberFormat.getCurrencyInstance(Locale.US).format(state.portfolio.totalUsdt),
        isLoading = state.isLoading,
        showSellSheet = state.showSellSheet,
        sellAmountInput = state.sellAmountInput,
    )

    private companion object {
        const val AVATAR_COLORS_COUNT = 7
    }
}
