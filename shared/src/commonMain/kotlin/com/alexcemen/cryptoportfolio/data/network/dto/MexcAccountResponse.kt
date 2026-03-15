package com.alexcemen.cryptoportfolio.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class MexcAccountResponse(val balances: List<MexcBalanceDto>)

@Serializable
data class MexcBalanceDto(val asset: String, val free: String, val locked: String)
