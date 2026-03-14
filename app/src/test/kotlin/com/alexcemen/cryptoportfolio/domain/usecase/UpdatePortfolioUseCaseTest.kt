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
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdatePortfolioUseCaseTest {

    private val validSettings = SettingsData(
        cmcApiKey = "cmc_key",
        mexcApiKey = "mexc_key",
        mexcApiSecret = "mexc_secret",
        topCoinsLimit = 3,
        excludedCoins = listOf("USDT"),
    )

    private val emptySettingsRepo = object : SettingsRepository {
        override suspend fun getSettings() = SettingsData()
        override suspend fun saveSettings(settings: SettingsData) {}
    }

    private val validSettingsRepo = object : SettingsRepository {
        override suspend fun getSettings() = validSettings
        override suspend fun saveSettings(settings: SettingsData) {}
    }

    private var savedCoins: List<CoinData>? = null
    private val fakePortfolioRepo = object : PortfolioRepository {
        override fun getPortfolio(): Flow<PortfolioData> = flow { emit(PortfolioData(emptyList(), 0.0)) }
        override suspend fun savePortfolio(coins: List<CoinData>) { savedCoins = coins }
    }

    private val fakeMexcRepository = object : MexcRepository {
        override suspend fun getBalances(): List<AssetBalance> = listOf(
            AssetBalance("ETH", quantity = 1.5, priceUsdt = 2000.0),
            AssetBalance("USDT", quantity = 100.0, priceUsdt = 1.0),
            AssetBalance("SOL", quantity = 0.0, priceUsdt = 150.0),  // zero balance — should be excluded
        )
        override suspend fun getTradableSymbols(): Set<String> = setOf("ETH", "SOL")
        override suspend fun getAssetPrecisions(): Map<String, Int> = emptyMap()
        override suspend fun placeMarketOrderByUsdt(symbol: String, side: TradeSide, usdtAmount: Double) {}
        override suspend fun placeMarketSellByQty(symbol: String, qty: String) {}
    }


    @Test
    fun missingKeys_returnsFailure() = runTest {
        val useCase = UpdatePortfolioUseCase(
            checkSettings = CheckSettingsUseCase(emptySettingsRepo),
            settingsRepository = emptySettingsRepo,
            portfolioRepository = fakePortfolioRepo,
            mexcRepository = fakeMexcRepository,
        )
        val result = useCase()
        assertTrue(result.isFailure)
    }

    @Test
    fun happyPath_savesCoins() = runTest {
        val useCase = UpdatePortfolioUseCase(
            checkSettings = CheckSettingsUseCase(validSettingsRepo),
            settingsRepository = validSettingsRepo,
            portfolioRepository = fakePortfolioRepo,
            mexcRepository = fakeMexcRepository,
        )
        val result = useCase()
        assertTrue(result.isSuccess)
        assertTrue(savedCoins != null)
        // ETH has 1.5 qty, price 2000 = $3000 position > threshold
        // USDT is excluded
        // SOL has 0 qty so no position (valueUsdt = 0 < 0.01)
        assertTrue(savedCoins!!.any { it.symbol == "ETH" })
        assertTrue(savedCoins!!.none { it.symbol == "USDT" })
    }
}
