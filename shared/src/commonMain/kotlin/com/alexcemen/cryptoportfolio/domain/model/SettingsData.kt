package com.alexcemen.cryptoportfolio.domain.model

data class SettingsData(
    val cmcApiKey: String = "",
    val mexcApiKey: String = "",
    val mexcApiSecret: String = "",
    val topCoinsLimit: Int = 100,
    val excludedCoins: List<String> = listOf("FDUSD","USD1", "PYUSD", "USDC", "DAI", "USDe")
)
