package com.alexcemen.cryptoportfolio.domain.model

data class CoinData(
    val symbol: String,
    val priceUsdt: Double,
    val quantity: Double,
    val logoUrl: String? = null,
) {
    val totalPositionUsdt: Double get() = priceUsdt * quantity
}
