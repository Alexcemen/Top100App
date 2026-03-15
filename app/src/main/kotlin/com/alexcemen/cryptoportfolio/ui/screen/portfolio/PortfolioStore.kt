package com.alexcemen.cryptoportfolio.ui.screen.portfolio

import com.alexcemen.cryptoportfolio.domain.model.PortfolioData
import com.alexcemen.cryptoportfolio.ui.mvi.MviEffect
import com.alexcemen.cryptoportfolio.ui.mvi.MviEvent
import com.alexcemen.cryptoportfolio.ui.mvi.MviSideEffect
import com.alexcemen.cryptoportfolio.ui.mvi.MviState
import com.alexcemen.cryptoportfolio.ui.mvi.MviUiState

object PortfolioStore {
    data class State(
        val portfolio: PortfolioData = PortfolioData(emptyList(), 0.0),
        val isLoading: Boolean = false,
        val showSellSheet: Boolean = false,
        val sellAmountInput: String = "",
    ) : MviState

    data class UiState(
        val coins: List<CoinUi>,
        val totalUsdt: String,
        val isLoading: Boolean,
        val showSellSheet: Boolean,
        val sellAmountInput: String,
    ) : MviUiState

    data class CoinUi(
        val symbol: String,
        val priceUsdt: String,
        val quantity: String,
        val totalPositionUsdt: String,
        val logoUrl: String? = null,
        val avatarColorIndex: Int = 0,
    )

    sealed interface Event : MviEvent {
        data object Update : Event
        data object Rebalance : Event
        data object OpenSellSheet : Event
        data object CloseSellSheet : Event
        data class SetSellAmount(val amount: String) : Event
        data class SetSellPercent(val percent: Float) : Event
        data object Sell : Event
        data object NavigateToSettings : Event
    }

    sealed interface Effect : MviEffect {
        data class SetPortfolio(val portfolio: PortfolioData) : Effect
        data class SetLoading(val isLoading: Boolean) : Effect
        data class SetShowSellSheet(val show: Boolean) : Effect
        data class SetSellAmount(val amount: String) : Effect
        data class ShowSnackbar(val message: String) : Effect
    }

    sealed interface SideEffect : MviSideEffect {
        data class ShowSnackbar(val message: String) : SideEffect
        data object NavigateToSettings : SideEffect
    }
}
