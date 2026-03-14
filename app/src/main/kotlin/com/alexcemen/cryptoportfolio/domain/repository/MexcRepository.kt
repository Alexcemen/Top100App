package com.alexcemen.cryptoportfolio.domain.repository

import com.alexcemen.cryptoportfolio.domain.model.AssetBalance
import com.alexcemen.cryptoportfolio.domain.model.TradeSide

interface MexcRepository {
    /** All held assets (including USDT with priceUsdt = 1.0), free quantity only. */
    suspend fun getBalances(): List<AssetBalance>

    /** Base asset symbols that have a USDT trading pair on MEXC. */
    suspend fun getTradableSymbols(): Set<String>

    /** Map of baseAsset → decimal precision for quantity. */
    suspend fun getAssetPrecisions(): Map<String, Int>

    /** Place a market order (buy/sell) for a given USDT amount. Logs and swallows errors. */
    suspend fun placeMarketOrderByUsdt(symbol: String, side: TradeSide, usdtAmount: Double)

    /** Place a market sell order for an exact asset quantity. Logs and swallows errors. */
    suspend fun placeMarketSellByQty(symbol: String, qty: String)
}
