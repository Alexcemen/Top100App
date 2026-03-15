package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.repository.MexcRepository
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.first
import kotlin.math.pow
import kotlin.math.roundToLong

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
            val truncated = kotlin.math.floor(rawQty * factor) / factor
            val qty = formatDouble(truncated, scale)
            mexcRepository.placeMarketSellByQty(coin.symbol, qty)
        }
    }

    private fun formatDouble(value: Double, decimals: Int): String {
        val factor = 10.0.pow(decimals)
        val long = (value * factor).roundToLong()
        val intPart = long / factor.roundToLong()
        val fracPart = long % factor.roundToLong()
        return "$intPart.${fracPart.toString().padStart(decimals, '0')}"
    }
}
