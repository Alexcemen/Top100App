package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.data.network.MexcApiService
import com.alexcemen.cryptoportfolio.data.network.QUOTE_ASSET
import com.alexcemen.cryptoportfolio.data.network.signMexcQuery
import com.alexcemen.cryptoportfolio.data.network.dto.MexcAccountResponse
import com.alexcemen.cryptoportfolio.data.network.dto.MexcBalanceDto
import com.alexcemen.cryptoportfolio.domain.model.CoinData
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdatePortfolioUseCase @Inject constructor(
    private val checkSettings: CheckSettingsUseCase,
    private val settingsRepository: SettingsRepository,
    private val portfolioRepository: PortfolioRepository,
    private val mexcService: MexcApiService,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        if (!checkSettings()) throw IllegalStateException("API keys not configured")

        val settings = settingsRepository.getSettings()
        val (prices, account) = fetchPricesAndAccount(settings.mexcApiSecret)
        val coins = mapToCoinData(account.balances, prices, settings.excludedCoins)

        portfolioRepository.savePortfolio(coins)
    }

    private suspend fun fetchPricesAndAccount(secret: String): Pair<Map<String, Double>, MexcAccountResponse> {
        val prices = mexcService.getAllPrices()
            .associate { it.symbol to (it.price.toDoubleOrNull() ?: 0.0) }
        val timestamp = System.currentTimeMillis()
        val signature = signMexcQuery("timestamp=$timestamp", secret)
        val account = mexcService.getAccount(timestamp, signature)
        return prices to account
    }

    private fun mapToCoinData(
        balances: List<MexcBalanceDto>,
        prices: Map<String, Double>,
        excludedCoins: List<String>,
    ): List<CoinData> = balances
        .filter { it.asset !in excludedCoins }
        .mapNotNull { balance ->
            val quantity = balance.free.toDoubleOrNull() ?: 0.0
            val price = if (balance.asset == QUOTE_ASSET) 1.0
                else prices["${balance.asset}$QUOTE_ASSET"] ?: return@mapNotNull null
            if (quantity * price < 0.01) return@mapNotNull null
            CoinData(symbol = balance.asset, priceUsdt = price, quantity = quantity)
        }
}
