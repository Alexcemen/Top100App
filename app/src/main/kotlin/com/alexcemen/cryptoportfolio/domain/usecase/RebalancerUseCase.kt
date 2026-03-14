package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.data.network.CmcApiService
import com.alexcemen.cryptoportfolio.data.network.ORDER_TYPE_MARKET
import com.alexcemen.cryptoportfolio.data.network.QUOTE_ASSET
import com.alexcemen.cryptoportfolio.data.network.MexcApiService
import com.alexcemen.cryptoportfolio.data.network.OrderSide
import com.alexcemen.cryptoportfolio.data.network.signMexcQuery
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import javax.inject.Inject
import retrofit2.HttpException

class RebalancerUseCase @Inject constructor(
    private val checkSettings: CheckSettingsUseCase,
    private val settingsRepository: SettingsRepository,
    private val mexcService: MexcApiService,
    private val cmcService: CmcApiService,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        if (!checkSettings()) throw IllegalStateException("API keys not configured")

        val settings = settingsRepository.getSettings()

        // Step 1: Fetch CMC top list and MEXC exchange info in parallel
        val (topCmcList, tradableMexc) = coroutineScope {
            val topDeferred = async {
                cmcService.getListings(settings.cmcApiKey, settings.topCoinsLimit)
                    .data.map { it.symbol }
            }
            val infoDeferred = async {
                mexcService.getExchangeInfo().symbols
                    .filter { it.quoteAsset == QUOTE_ASSET }
                    .map { it.baseAsset }
                    .toSet()
            }
            topDeferred.await() to infoDeferred.await()
        }

        // Step 2: Build available coins list
        val availableList = buildAvailableCoins(topCmcList, tradableMexc, settings.excludedCoins.toSet())

        // Fetch prices and balances
        val prices = mexcService.getAllPrices()
            .associate { it.symbol to (it.price.toDoubleOrNull() ?: 0.0) }
        val timestamp = System.currentTimeMillis()
        val signature = signMexcQuery("timestamp=$timestamp", settings.mexcApiSecret)
        val account = mexcService.getAccount(timestamp, signature)

        // Build USDT-value map for held coins
        val balancesInUsdt = mutableMapOf<String, Double>()
        for (balance in account.balances.filter { it.asset != QUOTE_ASSET }) {
            val quantity = balance.free.toDoubleOrNull() ?: 0.0
            val price = prices["${balance.asset}$QUOTE_ASSET"] ?: continue
            val value = (quantity * price).floor2()
            if (value > 1.0) balancesInUsdt[balance.asset] = value
        }

        val mine = balancesInUsdt.keys

        // Step 3: Sell unlisted coins
        val toSell = buildCoinsToSell(mine, availableList)
        for (coin in toSell) {
            val value = balancesInUsdt[coin] ?: continue
            placeOrder(
                coin = coin,
                side = OrderSide.SELL,
                usdtAmount = value,
                secret = settings.mexcApiSecret
            )
        }

        // Step 4: Buy missing coins
        val missingList = availableList.filter { it !in mine }
        if (missingList.isNotEmpty()) {
            val averageValue = if (balancesInUsdt.isEmpty()) 0.0
            else balancesInUsdt.values.average().floor2()
            var remaining = account.balances
                .find { it.asset == QUOTE_ASSET }
                ?.free?.toDoubleOrNull() ?: 0.0
            for (coin in missingList) {
                val toBuy = minOf(averageValue, remaining).floor2()
                if (toBuy < 1.0) break
                placeOrder(
                    coin = coin,
                    side = OrderSide.BUY,
                    usdtAmount = toBuy,
                    secret = settings.mexcApiSecret
                )
                remaining -= toBuy
            }
        }

        // Step 5: Rebalance existing positions
        val eligible = balancesInUsdt.filter { (symbol, _) -> symbol !in settings.excludedCoins }
        if (eligible.isEmpty()) return@runCatching
        val total = eligible.values.sum()
        val target = (total / eligible.size).floor2()

        for ((coin, value) in eligible) {
            val excess = (value - target).floor2()
            if (excess > 1.0) placeOrder(
                coin = coin,
                side = OrderSide.SELL,
                usdtAmount = excess,
                secret = settings.mexcApiSecret
            )
        }
        for ((coin, value) in eligible) {
            val deficit = (target - value).floor2()
            if (deficit > 1.0) placeOrder(
                coin = coin,
                side = OrderSide.BUY,
                usdtAmount = deficit,
                secret = settings.mexcApiSecret
            )
        }
    }

    private suspend fun placeOrder(coin: String, side: OrderSide, usdtAmount: Double, secret: String) {
        runCatching {
            val timestamp = System.currentTimeMillis()
            val quoteQty = usdtAmount.toString()
            val signature = signMexcQuery(
                query = "symbol=${coin}$QUOTE_ASSET&side=${side.name}&type=$ORDER_TYPE_MARKET&quoteOrderQty=$quoteQty&timestamp=$timestamp",
                secret = secret
            )
            Timber.d("REBALANCER placeOrder: symbol=${coin}$QUOTE_ASSET side=$side quoteOrderQty=$quoteQty")
            mexcService.placeOrder(
                symbol = "${coin}$QUOTE_ASSET",
                side = side,
                type = ORDER_TYPE_MARKET,
                quoteOrderQty = quoteQty,
                timestamp = timestamp,
                signature = signature,
            )
        }.onFailure {
            val body = (it as? HttpException)?.response()?.errorBody()?.string()
            Timber.e("REBALANCER placeOrder failed: ${coin}$QUOTE_ASSET side=$side error=${it.message} body=$body")
        }
    }

}
