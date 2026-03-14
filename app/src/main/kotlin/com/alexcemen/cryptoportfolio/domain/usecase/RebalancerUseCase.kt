package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.data.network.CmcApiService
import com.alexcemen.cryptoportfolio.data.network.ORDER_TYPE_MARKET
import com.alexcemen.cryptoportfolio.data.network.QUOTE_ASSET
import com.alexcemen.cryptoportfolio.data.network.MexcApiService
import com.alexcemen.cryptoportfolio.data.network.OrderSide
import com.alexcemen.cryptoportfolio.data.network.signMexcQuery
import com.alexcemen.cryptoportfolio.data.network.dto.MexcAccountResponse
import com.alexcemen.cryptoportfolio.data.network.dto.MexcBalanceDto
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

        val (topCmcList, tradableMexc) = fetchMarketData(settings.cmcApiKey, settings.topCoinsLimit)
        val availableList = buildAvailableCoins(topCmcList, tradableMexc, settings.excludedCoins.toSet())

        val (prices, account) = fetchPricesAndAccount(settings.mexcApiSecret)
        val balancesInUsdt = buildBalancesInUsdt(account.balances, prices)

        sellUnlistedCoins(balancesInUsdt, availableList, settings.mexcApiSecret)
        buyMissingCoins(balancesInUsdt, availableList, account.balances, settings.mexcApiSecret)
        rebalancePositions(balancesInUsdt, settings.excludedCoins, account.balances, settings.mexcApiSecret)
    }

    private suspend fun fetchMarketData(
        cmcApiKey: String,
        topCoinsLimit: Int,
    ): Pair<List<String>, Set<String>> = coroutineScope {
        val topDeferred = async {
            cmcService.getListings(cmcApiKey, topCoinsLimit).data.map { it.symbol }
        }
        val infoDeferred = async {
            mexcService.getExchangeInfo().symbols
                .filter { it.quoteAsset == QUOTE_ASSET }
                .map { it.baseAsset }
                .toSet()
        }
        topDeferred.await() to infoDeferred.await()
    }

    private suspend fun fetchPricesAndAccount(secret: String): Pair<Map<String, Double>, MexcAccountResponse> {
        val prices = mexcService.getAllPrices()
            .associate { it.symbol to (it.price.toDoubleOrNull() ?: 0.0) }
        val timestamp = System.currentTimeMillis()
        val signature = signMexcQuery("timestamp=$timestamp", secret)
        val account = mexcService.getAccount(timestamp, signature)
        return prices to account
    }

    private fun buildBalancesInUsdt(
        balances: List<MexcBalanceDto>,
        prices: Map<String, Double>,
    ): Map<String, Double> {
        val result = mutableMapOf<String, Double>()
        for (balance in balances.filter { it.asset != QUOTE_ASSET }) {
            val quantity = balance.free.toDoubleOrNull() ?: 0.0
            val price = prices["${balance.asset}$QUOTE_ASSET"] ?: continue
            val value = (quantity * price).floor2()
            if (value > 1.0) result[balance.asset] = value
        }
        return result
    }

    private suspend fun sellUnlistedCoins(
        balancesInUsdt: Map<String, Double>,
        availableList: List<String>,
        secret: String,
    ) {
        val toSell = buildCoinsToSell(balancesInUsdt.keys, availableList)
        for (coin in toSell) {
            val value = balancesInUsdt[coin] ?: continue
            placeOrder(coin = coin, side = OrderSide.SELL, usdtAmount = value, secret = secret)
        }
    }

    private suspend fun buyMissingCoins(
        balancesInUsdt: Map<String, Double>,
        availableList: List<String>,
        balances: List<MexcBalanceDto>,
        secret: String,
    ) {
        val missingList = availableList.filter { it !in balancesInUsdt.keys }
        if (missingList.isEmpty()) return

        val averageValue = if (balancesInUsdt.isEmpty()) 0.0
        else balancesInUsdt.values.average().floor2()
        var remaining = balances.find { it.asset == QUOTE_ASSET }?.free?.toDoubleOrNull() ?: 0.0

        for (coin in missingList) {
            val toBuy = minOf(averageValue, remaining).floor2()
            if (toBuy < 1.0) break
            placeOrder(coin = coin, side = OrderSide.BUY, usdtAmount = toBuy, secret = secret)
            remaining -= toBuy
        }
    }

    private suspend fun rebalancePositions(
        balancesInUsdt: Map<String, Double>,
        excludedCoins: List<String>,
        balances: List<MexcBalanceDto>,
        secret: String,
    ) {
        val eligible = balancesInUsdt.filter { (symbol, _) -> symbol !in excludedCoins }
        if (eligible.isEmpty()) return

        val freeUsdt = balances.find { it.asset == QUOTE_ASSET }?.free?.toDoubleOrNull() ?: 0.0
        val target = ((eligible.values.sum() + freeUsdt) / eligible.size).floor2()

        for ((coin, value) in eligible) {
            val excess = (value - target).floor2()
            if (excess > 1.0) placeOrder(coin = coin, side = OrderSide.SELL, usdtAmount = excess, secret = secret)
        }
        for ((coin, value) in eligible) {
            val deficit = (target - value).floor2()
            if (deficit > 1.0) placeOrder(coin = coin, side = OrderSide.BUY, usdtAmount = deficit, secret = secret)
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
