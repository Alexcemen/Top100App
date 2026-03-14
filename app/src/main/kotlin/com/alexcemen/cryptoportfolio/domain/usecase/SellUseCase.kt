package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.repository.MexcRepository
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class SellUseCase @Inject constructor(
    private val checkSettings: CheckSettingsUseCase,
    private val portfolioRepository: PortfolioRepository,
    private val mexcRepository: MexcRepository,
) {
    suspend operator fun invoke(usdtAmount: Double): Result<Unit> = runCatching {
        if (!checkSettings()) throw IllegalStateException("API keys not configured")

        val portfolio = portfolioRepository.getPortfolio().first()
        if (portfolio.coins.isEmpty()) throw IllegalStateException("Portfolio is empty")

        val precisions = mexcRepository.getAssetPrecisions()

        portfolio.coins.forEach { coin ->
            val coinSellUsdt = usdtAmount * (coin.totalPositionUsdt / portfolio.totalUsdt)
            if (coinSellUsdt < 1.0) return@forEach
            val qty = BigDecimal(coinSellUsdt / coin.priceUsdt)
                .setScale(precisions[coin.symbol] ?: 8, RoundingMode.FLOOR)
                .toPlainString()
            mexcRepository.placeMarketSellByQty(coin.symbol, qty)
        }
    }
}
