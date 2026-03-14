package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.data.network.ORDER_TYPE_MARKET
import com.alexcemen.cryptoportfolio.data.network.QUOTE_ASSET
import com.alexcemen.cryptoportfolio.data.network.MexcApiService
import com.alexcemen.cryptoportfolio.data.network.OrderSide
import com.alexcemen.cryptoportfolio.data.network.signMexcQuery
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import timber.log.Timber
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import retrofit2.HttpException

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

        val precisions = mexcService.getExchangeInfo().symbols
            .filter { it.quoteAsset == QUOTE_ASSET }
            .associate { it.baseAsset to it.baseAssetPrecision }

        portfolio.coins.forEach { coin ->
            val coinShare = coin.totalPositionUsdt / portfolio.totalUsdt
            val coinSellUsdt = usdtAmount * coinShare
            if (coinSellUsdt < 1.0) return@forEach

            runCatching {
                val scale = precisions[coin.symbol] ?: 8
                val qty = BigDecimal(coinSellUsdt / coin.priceUsdt)
                    .setScale(scale, RoundingMode.FLOOR)
                    .toPlainString()
                val timestamp = System.currentTimeMillis()
                val signature = signMexcQuery(
                    query = "symbol=${coin.symbol}$QUOTE_ASSET&side=SELL&type=$ORDER_TYPE_MARKET&quantity=$qty&timestamp=$timestamp",
                    secret = settings.mexcApiSecret
                )
                Timber.d("SELL placeOrder: symbol=${coin.symbol}$QUOTE_ASSET quantity=$qty")
                mexcService.placeOrderByQty(
                    symbol = "${coin.symbol}$QUOTE_ASSET",
                    side = OrderSide.SELL,
                    type = ORDER_TYPE_MARKET,
                    quantity = qty,
                    timestamp = timestamp,
                    signature = signature,
                )
            }.onFailure {
                val body = (it as? HttpException)?.response()?.errorBody()?.string()
                Timber.e("SELL failed: ${coin.symbol}$QUOTE_ASSET error=${it.message} body=$body")
            }
        }
    }

}
