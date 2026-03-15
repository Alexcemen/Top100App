package com.alexcemen.cryptoportfolio.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class MexcExchangeInfoResponse(val symbols: List<MexcSymbolDto>)

@Serializable
data class MexcSymbolDto(
    val baseAsset: String,
    val quoteAsset: String,
    val status: String,
    val baseAssetPrecision: Int = 8,
)
