package com.alexcemen.cryptoportfolio.domain.model

data class AssetBalance(
    val symbol: String,
    val quantity: Double,
    val priceUsdt: Double,
) {
    val valueUsdt: Double get() = quantity * priceUsdt
}
