package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.model.CoinData
import com.alexcemen.cryptoportfolio.domain.repository.MexcRepository
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdatePortfolioUseCase @Inject constructor(
    private val checkSettings: CheckSettingsUseCase,
    private val settingsRepository: SettingsRepository,
    private val portfolioRepository: PortfolioRepository,
    private val mexcRepository: MexcRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        if (!checkSettings()) throw IllegalStateException("API keys not configured")

        val excludedCoins = settingsRepository.getSettings().excludedCoins
        val coins = mexcRepository.getBalances()
            .filter { it.symbol !in excludedCoins }
            .filter { it.valueUsdt >= 0.01 }
            .map { CoinData(symbol = it.symbol, priceUsdt = it.priceUsdt, quantity = it.quantity) }

        portfolioRepository.savePortfolio(coins)
    }
}
