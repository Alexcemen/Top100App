package com.alexcemen.cryptoportfolio.data.network.dto

data class MexcAccountResponse(
    val balances: List<MexcBalanceDto>
)

data class MexcBalanceDto(
    val asset: String,
    val free: String,
    val locked: String
)
