package com.alexcemen.cryptoportfolio.data.network.dto

data class CmcListingsResponse(val data: List<CmcCoinDto>)
data class CmcCoinDto(val symbol: String)
