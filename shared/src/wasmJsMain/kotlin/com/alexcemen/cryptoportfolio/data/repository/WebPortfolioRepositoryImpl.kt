package com.alexcemen.cryptoportfolio.data.repository

import com.alexcemen.cryptoportfolio.domain.model.CoinData
import com.alexcemen.cryptoportfolio.domain.model.PortfolioData
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class CoinJson(
    val symbol: String,
    val priceUsdt: Double,
    val quantity: Double,
    val logoUrl: String? = null,
)

class WebPortfolioRepositoryImpl : PortfolioRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val flow = MutableStateFlow(loadPortfolio())

    override fun getPortfolio(): Flow<PortfolioData> = flow

    override suspend fun savePortfolio(coins: List<CoinData>) {
        val jsonList = coins.map { CoinJson(it.symbol, it.priceUsdt, it.quantity, it.logoUrl) }
        localStorage.setItem(STORAGE_KEY, json.encodeToString(jsonList))
        flow.value = toPortfolioData(coins)
    }

    private fun loadPortfolio(): PortfolioData {
        val raw = localStorage.getItem(STORAGE_KEY) ?: return PortfolioData(emptyList(), 0.0)
        return try {
            val coins = json.decodeFromString<List<CoinJson>>(raw).map {
                CoinData(it.symbol, it.priceUsdt, it.quantity, it.logoUrl)
            }
            toPortfolioData(coins)
        } catch (_: Exception) {
            PortfolioData(emptyList(), 0.0)
        }
    }

    private fun toPortfolioData(coins: List<CoinData>) =
        PortfolioData(coins = coins, totalUsdt = coins.sumOf { it.totalPositionUsdt })

    private companion object {
        const val STORAGE_KEY = "portfolio_data"
    }
}
