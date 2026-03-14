package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.model.AssetBalance
import com.alexcemen.cryptoportfolio.domain.model.CoinData
import com.alexcemen.cryptoportfolio.domain.model.PortfolioData
import com.alexcemen.cryptoportfolio.domain.model.SettingsData
import com.alexcemen.cryptoportfolio.domain.model.TradeSide
import com.alexcemen.cryptoportfolio.domain.repository.MexcRepository
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SellUseCaseTest {

    private val emptySettingsRepo = object : SettingsRepository {
        override suspend fun getSettings() = SettingsData()
        override suspend fun saveSettings(settings: SettingsData) {}
    }

    private val validSettings = SettingsData(
        cmcApiKey = "k", mexcApiKey = "m", mexcApiSecret = "s"
    )
    private val validSettingsRepo = object : SettingsRepository {
        override suspend fun getSettings() = validSettings
        override suspend fun saveSettings(settings: SettingsData) {}
    }

    private val portfolioWithCoins = PortfolioData(
        coins = listOf(
            CoinData("ETH", 2000.0, 1.5),    // $3000 position
            CoinData("BTC", 60000.0, 0.1),   // $6000 position
        ),
        totalUsdt = 9000.0,
    )

    private val fakePortfolioRepo = object : PortfolioRepository {
        override fun getPortfolio(): Flow<PortfolioData> = flow { emit(portfolioWithCoins) }
        override suspend fun savePortfolio(coins: List<CoinData>) {}
    }

    private data class SellCall(val symbol: String, val qty: String)
    private val sellCalls = mutableListOf<SellCall>()

    private val fakeMexcRepository = object : MexcRepository {
        override suspend fun getBalances(): List<AssetBalance> = emptyList()
        override suspend fun getTradableSymbols(): Set<String> = emptySet()
        override suspend fun getAssetPrecisions(): Map<String, Int> = mapOf("ETH" to 6, "BTC" to 6)
        override suspend fun placeMarketOrderByUsdt(symbol: String, side: TradeSide, usdtAmount: Double) {}
        override suspend fun placeMarketSellByQty(symbol: String, qty: String) {
            sellCalls.add(SellCall(symbol, qty))
        }
    }

    @Test
    fun missingKeys_returnsFailure() = runTest {
        val useCase = SellUseCase(
            checkSettings = CheckSettingsUseCase(emptySettingsRepo),
            portfolioRepository = fakePortfolioRepo,
            mexcRepository = fakeMexcRepository,
        )
        assertTrue(useCase(500.0).isFailure)
    }

    @Test
    fun zeroAmount_placesNoOrders() = runTest {
        sellCalls.clear()
        val useCase = SellUseCase(
            checkSettings = CheckSettingsUseCase(validSettingsRepo),
            portfolioRepository = fakePortfolioRepo,
            mexcRepository = fakeMexcRepository,
        )
        val result = useCase(0.0)
        // zero amount → each coin sell USDT < 1.0 threshold → no orders, but no error
        assertTrue(result.isSuccess)
        assertEquals(0, sellCalls.size)
    }

    @Test
    fun happyPath_placesProportionalOrders() = runTest {
        sellCalls.clear()
        val useCase = SellUseCase(
            checkSettings = CheckSettingsUseCase(validSettingsRepo),
            portfolioRepository = fakePortfolioRepo,
            mexcRepository = fakeMexcRepository,
        )
        val result = useCase(900.0)  // 10% of $9000
        assertTrue(result.isSuccess)
        // Should place 2 sell orders (one per coin)
        assertEquals(2, sellCalls.size)
        assertTrue(sellCalls.any { it.symbol == "ETH" })
        assertTrue(sellCalls.any { it.symbol == "BTC" })
    }
}
