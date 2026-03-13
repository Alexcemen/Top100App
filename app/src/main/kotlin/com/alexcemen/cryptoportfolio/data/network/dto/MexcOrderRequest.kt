package com.alexcemen.cryptoportfolio.data.network.dto

data class MexcOrderRequest(
    val symbol: String,
    val side: String,
    val type: String = "MARKET",
    val quoteOrderQty: String? = null,
    val quantity: String? = null,
)
