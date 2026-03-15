package com.alexcemen.cryptoportfolio.domain.repository

import com.alexcemen.cryptoportfolio.domain.model.AssetBalance
import com.alexcemen.cryptoportfolio.domain.model.TradeSide

interface MexcRepository {
    suspend fun getBalances(): List<AssetBalance>

    suspend fun getTradableSymbols(): Set<String>

    suspend fun getAssetPrecisions(): Map<String, Int>

    suspend fun placeMarketOrderByUsdt(symbol: String, side: TradeSide, usdtAmount: Double)

    suspend fun placeMarketSellByQty(symbol: String, qty: String)
}
