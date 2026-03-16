package com.alexcemen.cryptoportfolio.data.repository

import com.alexcemen.cryptoportfolio.data.network.ORDER_TYPE_MARKET
import com.alexcemen.cryptoportfolio.data.network.QUOTE_ASSET
import com.alexcemen.cryptoportfolio.data.network.MexcApiService
import com.alexcemen.cryptoportfolio.data.network.OrderSide
import com.alexcemen.cryptoportfolio.data.network.signMexcQuery
import com.alexcemen.cryptoportfolio.domain.model.AssetBalance
import com.alexcemen.cryptoportfolio.domain.model.TradeSide
import com.alexcemen.cryptoportfolio.domain.repository.MexcRepository
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import com.alexcemen.cryptoportfolio.platform.Logger
import com.alexcemen.cryptoportfolio.platform.currentTimeMillis

class MexcRepositoryImpl(
    private val mexcService: MexcApiService,
    private val settingsRepository: SettingsRepository,
) : MexcRepository {

    override suspend fun getBalances(): List<AssetBalance> {
        val prices = mexcService.getAllPrices()
            .associate { it.symbol to (it.price.toDoubleOrNull() ?: 0.0) }
        val secret = settingsRepository.getSettings().mexcApiSecret
        val timestamp = currentTimeMillis()
        val account = mexcService.getAccount(timestamp, signMexcQuery("timestamp=$timestamp", secret))
        return account.balances.mapNotNull { balance ->
            val quantity = balance.free.toDoubleOrNull() ?: 0.0
            val price = if (balance.asset == QUOTE_ASSET) 1.0
            else prices["${balance.asset}$QUOTE_ASSET"] ?: return@mapNotNull null
            AssetBalance(symbol = balance.asset, quantity = quantity, priceUsdt = price)
        }
    }

    override suspend fun getTradableSymbols(): Set<String> =
        mexcService.getExchangeInfo().symbols
            .filter { it.quoteAsset == QUOTE_ASSET }
            .map { it.baseAsset }
            .toSet()

    override suspend fun getAssetPrecisions(): Map<String, Int> =
        mexcService.getExchangeInfo().symbols
            .filter { it.quoteAsset == QUOTE_ASSET }
            .associate { it.baseAsset to it.baseAssetPrecision }

    override suspend fun placeMarketOrderByUsdt(symbol: String, side: TradeSide, usdtAmount: Double) {
        runCatching {
            val secret = settingsRepository.getSettings().mexcApiSecret
            val timestamp = currentTimeMillis()
            val quoteQty = usdtAmount.toString()
            val mexcSide = if (side == TradeSide.BUY) OrderSide.BUY else OrderSide.SELL
            val signature = signMexcQuery(
                query = "symbol=${symbol}$QUOTE_ASSET&side=${mexcSide.name}&type=$ORDER_TYPE_MARKET&quoteOrderQty=$quoteQty&timestamp=$timestamp",
                secret = secret,
            )
            Logger.d("MexcRepo", "placeMarketOrderByUsdt: ${symbol}$QUOTE_ASSET side=$side quoteOrderQty=$quoteQty")
            mexcService.placeOrder(
                symbol = "${symbol}$QUOTE_ASSET", side = mexcSide, type = ORDER_TYPE_MARKET,
                quoteOrderQty = quoteQty, timestamp = timestamp, signature = signature,
            )
        }.onFailure {
            Logger.e("MexcRepo", "placeMarketOrderByUsdt failed: ${symbol}$QUOTE_ASSET side=$side error=${it.message}")
        }
    }

    override suspend fun placeMarketSellByQty(symbol: String, qty: String) {
        runCatching {
            val secret = settingsRepository.getSettings().mexcApiSecret
            val timestamp = currentTimeMillis()
            val signature = signMexcQuery(
                query = "symbol=${symbol}$QUOTE_ASSET&side=SELL&type=$ORDER_TYPE_MARKET&quantity=$qty&timestamp=$timestamp",
                secret = secret,
            )
            Logger.d("MexcRepo", "placeMarketSellByQty: ${symbol}$QUOTE_ASSET quantity=$qty")
            mexcService.placeOrderByQty(
                symbol = "${symbol}$QUOTE_ASSET", side = OrderSide.SELL, type = ORDER_TYPE_MARKET,
                quantity = qty, timestamp = timestamp, signature = signature,
            )
        }.onFailure {
            Logger.e("MexcRepo", "placeMarketSellByQty failed: ${symbol}$QUOTE_ASSET error=${it.message}")
        }
    }
}
