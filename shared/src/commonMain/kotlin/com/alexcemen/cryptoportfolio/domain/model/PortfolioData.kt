package com.alexcemen.cryptoportfolio.domain.model

data class PortfolioData(
    val coins: List<CoinData>,
    val totalUsdt: Double,
)
