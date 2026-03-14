package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.data.network.ORDER_TYPE_MARKET
import com.alexcemen.cryptoportfolio.data.network.QUOTE_ASSET
import com.alexcemen.cryptoportfolio.data.network.MexcApiService
import com.alexcemen.cryptoportfolio.data.network.OrderSide
import com.alexcemen.cryptoportfolio.data.network.signMexcQuery
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SellUseCase @Inject constructor(
    private val checkSettings: CheckSettingsUseCase,
    private val settingsRepository: SettingsRepository,
    private val portfolioRepository: PortfolioRepository,
    private val mexcService: MexcApiService,
) {
    suspend operator fun invoke(usdtAmount: Double): Result<Unit> = runCatching {
        if (!checkSettings()) throw IllegalStateException("API keys not configured")

        val settings = settingsRepository.getSettings()
        val portfolio = portfolioRepository.getPortfolio().first()

        if (portfolio.coins.isEmpty()) throw IllegalStateException("Portfolio is empty")

        portfolio.coins.forEach { coin ->
            val coinShare = coin.totalPositionUsdt / portfolio.totalUsdt
            val coinSellUsdt = usdtAmount * coinShare
            if (coinSellUsdt < 1.0) return@forEach

            val quoteQty = coinSellUsdt.toString()
            val timestamp = System.currentTimeMillis()
            val signature = signMexcQuery(
                query = "symbol=${coin.symbol}USDT&side=SELL&type=$ORDER_TYPE_MARKET&quoteOrderQty=$quoteQty&timestamp=$timestamp",
                secret = settings.mexcApiSecret
            )
            mexcService.placeOrder(
                symbol = "${coin.symbol}$QUOTE_ASSET",
                side = OrderSide.SELL,
                type = ORDER_TYPE_MARKET,
                quoteOrderQty = quoteQty,
                timestamp = timestamp,
                signature = signature,
            )
        }
    }

}
