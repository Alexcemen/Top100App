package com.alexcemen.cryptoportfolio.data.network.dto

data class MexcExchangeInfoResponse(val symbols: List<MexcSymbolDto>)
data class MexcSymbolDto(val baseAsset: String, val quoteAsset: String, val status: String, val baseAssetPrecision: Int = 8)
