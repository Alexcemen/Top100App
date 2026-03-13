package com.alexcemen.cryptoportfolio.domain.repository

import com.alexcemen.cryptoportfolio.domain.model.CoinData
import com.alexcemen.cryptoportfolio.domain.model.PortfolioData
import kotlinx.coroutines.flow.Flow

interface PortfolioRepository {
    fun getPortfolio(): Flow<PortfolioData>
    suspend fun savePortfolio(coins: List<CoinData>)
}
