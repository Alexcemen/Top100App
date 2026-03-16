package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.repository.MexcRepository
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import com.alexcemen.cryptoportfolio.platform.formatNumber
import kotlinx.coroutines.flow.first
import kotlin.math.floor
import kotlin.math.pow

class SellUseCase constructor(
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
            val rawQty = coinSellUsdt / coin.priceUsdt
            val scale = precisions[coin.symbol] ?: 8
            val factor = 10.0.pow(scale)
            val truncated = floor(rawQty * factor) / factor
            val qty = formatNumber(truncated, scale)
            mexcRepository.placeMarketSellByQty(coin.symbol, qty)
        }
    }
}
