package com.alexcemen.cryptoportfolio.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CmcListingsResponse(val data: List<CmcCoinDto>)

@Serializable
data class CmcCoinDto(val id: Int, val symbol: String)
