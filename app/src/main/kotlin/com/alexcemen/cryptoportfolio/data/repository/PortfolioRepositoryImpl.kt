package com.alexcemen.cryptoportfolio.data.repository

import com.alexcemen.cryptoportfolio.data.db.PortfolioDao
import com.alexcemen.cryptoportfolio.data.db.entity.CoinEntity
import com.alexcemen.cryptoportfolio.domain.model.CoinData
import com.alexcemen.cryptoportfolio.domain.model.PortfolioData
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PortfolioRepositoryImpl @Inject constructor(
    private val dao: PortfolioDao
) : PortfolioRepository {

    override fun getPortfolio(): Flow<PortfolioData> =
        dao.getAll().map { entities ->
            val coins = entities.map { it.toDomain() }
            PortfolioData(
                coins = coins,
                totalUsdt = coins.sumOf { it.totalPositionUsdt }
            )
        }

    override suspend fun savePortfolio(coins: List<CoinData>) {
        dao.replaceAll(coins.map { it.toEntity() })
    }

    private fun CoinEntity.toDomain() = CoinData(symbol, priceUsdt, quantity)
    private fun CoinData.toEntity() = CoinEntity(symbol, priceUsdt, quantity)
}
