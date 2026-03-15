package com.alexcemen.cryptoportfolio.ui.screen.portfolio

import com.alexcemen.cryptoportfolio.domain.usecase.GetPortfolioUseCase
import com.alexcemen.cryptoportfolio.domain.usecase.RebalancerUseCase
import com.alexcemen.cryptoportfolio.domain.usecase.SellUseCase
import com.alexcemen.cryptoportfolio.domain.usecase.UpdatePortfolioUseCase
import com.alexcemen.cryptoportfolio.platform.Logger
import com.alexcemen.cryptoportfolio.ui.mvi.ScreenModel
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore.Effect
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore.Event
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore.SideEffect
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore.State
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioStore.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class PortfolioScreenModel(
    private val getPortfolio: GetPortfolioUseCase,
    private val updatePortfolio: UpdatePortfolioUseCase,
    private val sell: SellUseCase,
    private val rebalancer: RebalancerUseCase,
    reducer: PortfolioReducer,
) : ScreenModel<State, Event, SideEffect, Effect, UiState>(reducer) {

    override fun createState() = State()

    init {
        scope.launch {
            getPortfolio().collect { portfolio ->
                forceEffect(Effect.SetPortfolio(portfolio))
            }
        }
    }

    override fun handleEvent(currentState: State, intent: Event): Flow<Effect> = flow {
        when (intent) {
            Event.Update -> {
                emit(Effect.SetLoading(true))
                val result = updatePortfolio()
                emit(Effect.SetLoading(false))
                if (result.isFailure) {
                    val e = result.exceptionOrNull()
                    Logger.e("Portfolio", "UPDATE error: ${e?.message}")
                    emit(Effect.ShowSnackbar(e?.message ?: "Update failed"))
                }
            }
            Event.Rebalance -> {
                emit(Effect.SetLoading(true))
                val result = rebalancer()
                emit(Effect.SetLoading(false))
                if (result.isFailure) {
                    val e = result.exceptionOrNull()
                    Logger.e("Portfolio", "REBALANCER error: ${e?.message}")
                    emit(Effect.ShowSnackbar(e?.message ?: "Rebalance failed"))
                }
            }
            Event.OpenSellSheet -> emit(Effect.SetShowSellSheet(true))
            Event.CloseSellSheet -> emit(Effect.SetShowSellSheet(false))
            is Event.SetSellAmount -> emit(Effect.SetSellAmount(intent.amount))
            is Event.SetSellPercent -> {
                val amount = currentState.portfolio.totalUsdt * intent.percent
                emit(Effect.SetSellAmount("%.2f".format(amount)))
            }
            Event.Sell -> {
                emit(Effect.SetShowSellSheet(false))
                emit(Effect.SetLoading(true))
                val amount = currentState.sellAmountInput.toDoubleOrNull() ?: 0.0
                val result = sell(amount)
                emit(Effect.SetLoading(false))
                if (result.isFailure) emit(Effect.ShowSnackbar(result.exceptionOrNull()?.message ?: "Sell failed"))
            }
            Event.NavigateToSettings -> {
                scope.launch { sendSideEffect(SideEffect.NavigateToSettings) }
            }
        }
    }

    override fun handleEffect(currentState: State, effect: Effect): State = when (effect) {
        is Effect.SetPortfolio -> currentState.copy(portfolio = effect.portfolio)
        is Effect.SetLoading -> currentState.copy(isLoading = effect.isLoading)
        is Effect.SetShowSellSheet -> currentState.copy(showSellSheet = effect.show)
        is Effect.SetSellAmount -> currentState.copy(sellAmountInput = effect.amount)
        is Effect.ShowSnackbar -> currentState.also {
            scope.launch { sendSideEffect(SideEffect.ShowSnackbar(effect.message)) }
        }
    }
}
