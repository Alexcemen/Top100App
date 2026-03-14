package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.data.network.MexcApiService
import com.alexcemen.cryptoportfolio.data.network.QUOTE_ASSET
import com.alexcemen.cryptoportfolio.data.network.signMexcQuery
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

        // Fetch all ticker prices from MEXC
        val prices = mexcService.getAllPrices()
            .associate { it.symbol to (it.price.toDoubleOrNull() ?: 0.0) }

        // Fetch MEXC account balances (signed request)
        val timestamp = System.currentTimeMillis()
        val queryString = "timestamp=$timestamp"
        val signature = signMexcQuery(queryString, settings.mexcApiSecret)
        val account = mexcService.getAccount(timestamp, signature)

        val coins = account.balances
            .filter { it.asset !in settings.excludedCoins }
            .mapNotNull { balance ->
                val quantity = balance.free.toDoubleOrNull() ?: 0.0
                val price = prices["${balance.asset}$QUOTE_ASSET"] ?: return@mapNotNull null
                if (quantity * price < 0.01) return@mapNotNull null
                CoinData(symbol = balance.asset, priceUsdt = price, quantity = quantity)
            }

        portfolioRepository.savePortfolio(coins)
    }

}
