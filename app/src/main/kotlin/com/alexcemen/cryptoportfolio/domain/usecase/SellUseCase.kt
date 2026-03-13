package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.data.network.MexcApiService
import com.alexcemen.cryptoportfolio.data.network.dto.MexcOrderRequest
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class SellUseCase @Inject constructor(
    private val checkSettings: CheckSettingsUseCase,
    private val settingsRepo: SettingsRepository,
    private val portfolioRepo: PortfolioRepository,
    private val mexcService: MexcApiService,
) {
    suspend operator fun invoke(usdtAmount: Double): Result<Unit> = runCatching {
        if (!checkSettings()) throw IllegalStateException("API keys not configured")
        if (usdtAmount <= 0) throw IllegalArgumentException("Amount must be > 0")

        val settings = settingsRepo.getSettings()
        val portfolio = portfolioRepo.getPortfolio().first()

        if (portfolio.coins.isEmpty()) throw IllegalStateException("Portfolio is empty")

        portfolio.coins.forEach { coin ->
            val coinShare = coin.totalPositionUsdt / portfolio.totalUsdt
            val coinSellUsdt = usdtAmount * coinShare
            if (coinSellUsdt < 1.0) return@forEach

            val qty = coinSellUsdt / coin.priceUsdt
            val timestamp = System.currentTimeMillis()
            val queryString = "symbol=${coin.symbol}USDT&side=SELL&type=MARKET&quantity=${"%.6f".format(qty)}&timestamp=$timestamp"
            val signature = signQuery(queryString, settings.mexcApiSecret)
            mexcService.placeOrder(
                MexcOrderRequest(
                    symbol = "${coin.symbol}USDT",
                    side = "SELL",
                    quantity = "%.6f".format(qty),
                ),
                timestamp,
                signature,
            )
        }
    }

    private fun signQuery(query: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(query.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
