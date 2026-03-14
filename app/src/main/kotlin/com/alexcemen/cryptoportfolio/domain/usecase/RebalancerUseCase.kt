package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.data.network.CmcApiService
import com.alexcemen.cryptoportfolio.data.network.MexcApiService
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class RebalancerUseCase @Inject constructor(
    private val checkSettings: CheckSettingsUseCase,
    private val settingsRepo: SettingsRepository,
    private val portfolioRepo: PortfolioRepository,
    private val mexcService: MexcApiService,
    private val cmcService: CmcApiService,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        if (!checkSettings()) throw IllegalStateException("API keys not configured")

        val settings = settingsRepo.getSettings()

        // Step 1: Fetch CMC top list and MEXC exchange info in parallel
        val (topCmc, tradableMexc) = coroutineScope {
            val topDeferred = async {
                cmcService.getListings(settings.cmcApiKey, settings.topCoinsLimit)
                    .data.map { it.symbol }
            }
            val infoDeferred = async {
                mexcService.getExchangeInfo().symbols
                    .filter { it.quoteAsset == "USDT" }
                    .map { it.baseAsset }
                    .toSet()
            }
            topDeferred.await() to infoDeferred.await()
        }

        // Step 2: Build available coins list
        val available = buildAvailableCoins(topCmc, tradableMexc, settings.excludedCoins.toSet())

        // Fetch prices and balances
        val prices = mexcService.getAllPrices()
            .associate { it.symbol to (it.price.toDoubleOrNull() ?: 0.0) }
        val timestamp = System.currentTimeMillis()
        val signature = signQuery("timestamp=$timestamp", settings.mexcApiSecret)
        val account = mexcService.getAccount(timestamp, signature)

        // Build USDT-value map for held coins
        val balancesInUsdt = mutableMapOf<String, Double>()
        for (balance in account.balances.filter { it.asset != "USDT" }) {
            val qty = balance.free.toDoubleOrNull() ?: 0.0
            val price = prices["${balance.asset}USDT"] ?: continue
            val value = (qty * price).floor2()
            if (value > 0.0) balancesInUsdt[balance.asset] = value
        }

        val mine = balancesInUsdt.keys

        // Step 3: Sell unlisted coins
        val toSell = buildCoinsToSell(mine, available)
        for (coin in toSell) {
            val value = balancesInUsdt[coin] ?: continue
            if (value > 1.0) placeOrder(coin, "SELL", usdtAmount = value, secret = settings.mexcApiSecret)
        }

        // Step 4: Buy missing coins
        val missing = available.filter { it !in mine }
        if (missing.isNotEmpty()) {
            val avgValue = if (balancesInUsdt.isEmpty()) 0.0
            else balancesInUsdt.values.average().floor2()
            var remaining = account.balances
                .find { it.asset == "USDT" }
                ?.free?.toDoubleOrNull() ?: 0.0
            for (coin in missing) {
                val toBuy = minOf(avgValue, remaining).floor2()
                if (toBuy < 1.0) break
                placeOrder(coin, "BUY", usdtAmount = toBuy, secret = settings.mexcApiSecret)
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
            if (excess > 1.0) placeOrder(coin, "SELL", usdtAmount = excess, secret = settings.mexcApiSecret)
        }
        for ((coin, value) in eligible) {
            if (value < 1.0) continue
            val deficit = (target - value).floor2()
            if (deficit > 1.0) placeOrder(coin, "BUY", usdtAmount = deficit, secret = settings.mexcApiSecret)
        }
    }

    private suspend fun placeOrder(coin: String, side: String, usdtAmount: Double, secret: String) {
        val ts = System.currentTimeMillis()
        val quoteQty = usdtAmount.toString()
        val sig = signQuery("symbol=${coin}USDT&side=$side&type=MARKET&quoteOrderQty=$quoteQty&timestamp=$ts", secret)
        Timber.d("REBALANCER placeOrder: symbol=${coin}USDT side=$side quoteOrderQty=$quoteQty")
        mexcService.placeOrder(
            symbol = "${coin}USDT",
            side = side,
            type = "MARKET",
            quoteOrderQty = quoteQty,
            timestamp = ts,
            signature = sig,
        )
    }

    private fun signQuery(query: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(query.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
