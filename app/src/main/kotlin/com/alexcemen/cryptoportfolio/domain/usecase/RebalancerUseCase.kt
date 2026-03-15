package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.model.TradeSide
import com.alexcemen.cryptoportfolio.domain.model.USDT
import com.alexcemen.cryptoportfolio.domain.repository.CmcRepository
import com.alexcemen.cryptoportfolio.domain.repository.MexcRepository
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class RebalancerUseCase @Inject constructor(
    private val checkSettings: CheckSettingsUseCase,
    private val settingsRepository: SettingsRepository,
    private val mexcRepository: MexcRepository,
    private val cmcRepository: CmcRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        if (!checkSettings()) throw IllegalStateException("API keys not configured")

        val settings = settingsRepository.getSettings()

        val (topCmcList, tradableMexc) = coroutineScope {
            val topDeferred = async { cmcRepository.getTopCoins(settings.cmcApiKey, settings.topCoinsLimit) }
            val infoDeferred = async { mexcRepository.getTradableSymbols() }
            topDeferred.await() to infoDeferred.await()
        }
        val availableList = buildAvailableCoins(topCmcList, tradableMexc, settings.excludedCoins.toSet())

        val balances = mexcRepository.getBalances()
        val balancesInUsdt = balances
            .filter { it.symbol != USDT && it.valueUsdt > 1.0 }
            .associate { it.symbol to it.valueUsdt }
        val freeUsdt = balances.find { it.symbol == USDT }?.quantity ?: 0.0

        sellUnlistedCoins(balancesInUsdt, availableList)
        buyMissingCoins(balancesInUsdt, availableList, freeUsdt)
        rebalancePositions(balancesInUsdt, settings.excludedCoins, freeUsdt)
    }

    private suspend fun sellUnlistedCoins(
        balancesInUsdt: Map<String, Double>,
        availableList: List<String>,
    ) {
        val toSell = buildCoinsToSell(balancesInUsdt.keys, availableList)
        for (coin in toSell) {
            val value = balancesInUsdt[coin] ?: continue
            mexcRepository.placeMarketOrderByUsdt(coin, TradeSide.SELL, value)
        }
    }

    private suspend fun buyMissingCoins(
        balancesInUsdt: Map<String, Double>,
        availableList: List<String>,
        freeUsdt: Double,
    ) {
        val missingList = availableList.filter { it !in balancesInUsdt.keys }
        if (missingList.isEmpty()) return

        val averageValue = if (balancesInUsdt.isEmpty()) 0.0
        else balancesInUsdt.values.average().floor2()
        var remaining = freeUsdt

        for (coin in missingList) {
            val toBuy = minOf(averageValue, remaining).floor2()
            if (toBuy < 1.0) break
            mexcRepository.placeMarketOrderByUsdt(coin, TradeSide.BUY, toBuy)
            remaining -= toBuy
        }
    }

    private suspend fun rebalancePositions(
        balancesInUsdt: Map<String, Double>,
        excludedCoins: List<String>,
        freeUsdt: Double,
    ) {
        val eligible = balancesInUsdt.filter { (symbol, _) -> symbol !in excludedCoins }
        if (eligible.isEmpty()) return

        val target = ((eligible.values.sum() + freeUsdt) / eligible.size).floor2()

        for ((coin, value) in eligible) {
            val excess = (value - target).floor2()
            if (excess > 1.0) mexcRepository.placeMarketOrderByUsdt(coin, TradeSide.SELL, excess)
        }
        for ((coin, value) in eligible) {
            val deficit = (target - value).floor2()
            if (deficit > 1.0) mexcRepository.placeMarketOrderByUsdt(coin, TradeSide.BUY, deficit)
        }
    }
}
