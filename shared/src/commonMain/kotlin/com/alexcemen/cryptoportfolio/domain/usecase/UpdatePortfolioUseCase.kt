package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.model.CoinData
import com.alexcemen.cryptoportfolio.domain.repository.CmcRepository
import com.alexcemen.cryptoportfolio.domain.repository.MexcRepository
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository

private const val CMC_ICON_FETCH_LIMIT = 500

class UpdatePortfolioUseCase constructor(
    private val checkSettings: CheckSettingsUseCase,
    private val settingsRepository: SettingsRepository,
    private val portfolioRepository: PortfolioRepository,
    private val mexcRepository: MexcRepository,
    private val cmcRepository: CmcRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        if (!checkSettings()) throw IllegalStateException("API keys not configured")

        val settings = settingsRepository.getSettings()
        val excludedCoins = settings.excludedCoins

        val cmcIds: Map<String, Int> = runCatching {
            cmcRepository.getCoinIds(settings.cmcApiKey, CMC_ICON_FETCH_LIMIT)
        }.getOrDefault(emptyMap())

        val coins = mexcRepository.getBalances()
            .filter { it.symbol !in excludedCoins }
            .filter { it.valueUsdt >= 0.01 }
            .map { balance ->
                CoinData(
                    symbol = balance.symbol,
                    priceUsdt = balance.priceUsdt,
                    quantity = balance.quantity,
                    logoUrl = cmcIds[balance.symbol]
                        ?.let { "https://s2.coinmarketcap.com/static/img/coins/64x64/$it.png" },
                )
            }

        portfolioRepository.savePortfolio(coins)
    }
}
