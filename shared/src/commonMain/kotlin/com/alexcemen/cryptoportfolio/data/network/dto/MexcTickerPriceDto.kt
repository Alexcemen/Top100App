package com.alexcemen.cryptoportfolio.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class MexcTickerPriceDto(val symbol: String, val price: String)
