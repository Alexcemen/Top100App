package com.alexcemen.cryptoportfolio.domain.model

data class SettingsData(
    val cmcApiKey: String = "",
    val mexcApiKey: String = "",
    val mexcApiSecret: String = "",
    val topCoinsLimit: Int = 20,
    val excludedCoins: List<String> = listOf("USDT", "USDC", "BUSD"),
)
