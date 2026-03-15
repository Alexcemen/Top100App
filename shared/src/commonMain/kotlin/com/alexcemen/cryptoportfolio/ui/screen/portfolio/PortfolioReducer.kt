package com.alexcemen.cryptoportfolio.ui.screen.portfolio

import com.alexcemen.cryptoportfolio.ui.mvi.Reducer
import kotlin.math.abs

class PortfolioReducer : Reducer<PortfolioStore.State, PortfolioStore.UiState> {
    override fun reduce(state: PortfolioStore.State) = PortfolioStore.UiState(
        coins = state.portfolio.coins.sortedByDescending { it.totalPositionUsdt }.map { coin ->
            PortfolioStore.CoinUi(
                symbol = coin.symbol,
                priceUsdt = "${"$"}%.4f".format(coin.priceUsdt),
                quantity = "%.6f".format(coin.quantity),
                totalPositionUsdt = "${"$"}%.2f".format(coin.totalPositionUsdt),
                logoUrl = coin.logoUrl,
                avatarColorIndex = abs(coin.symbol.hashCode()) % AVATAR_COLORS_COUNT,
            )
        },
        totalUsdt = "${"$"}%.2f".format(state.portfolio.totalUsdt),
        isLoading = state.isLoading,
        showSellSheet = state.showSellSheet,
        sellAmountInput = state.sellAmountInput,
    )

    private companion object {
        const val AVATAR_COLORS_COUNT = 7
    }
}
