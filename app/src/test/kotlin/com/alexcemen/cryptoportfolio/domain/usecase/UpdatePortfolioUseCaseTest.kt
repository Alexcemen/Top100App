package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.data.network.CmcApiService
import com.alexcemen.cryptoportfolio.data.network.MexcApiService
import com.alexcemen.cryptoportfolio.data.network.dto.CmcCoinDto
import com.alexcemen.cryptoportfolio.data.network.dto.CmcListingsResponse
import com.alexcemen.cryptoportfolio.data.network.dto.MexcAccountResponse
import com.alexcemen.cryptoportfolio.data.network.dto.MexcBalanceDto
import com.alexcemen.cryptoportfolio.data.network.dto.MexcExchangeInfoResponse
import com.alexcemen.cryptoportfolio.data.network.dto.MexcOrderRequest
import com.alexcemen.cryptoportfolio.data.network.dto.MexcTickerPriceDto
import com.alexcemen.cryptoportfolio.domain.model.CoinData
import com.alexcemen.cryptoportfolio.domain.model.PortfolioData
import com.alexcemen.cryptoportfolio.domain.model.SettingsData
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

    private val fakeMexcService = object : MexcApiService {
        override suspend fun getAccount(timestamp: Long, signature: String) =
            MexcAccountResponse(listOf(
                MexcBalanceDto("ETH", "1.5", "0.0"),
                MexcBalanceDto("USDT", "100.0", "0.0"),
                MexcBalanceDto("SOL", "0.0", "0.0"),  // zero balance — should be excluded
            ))
        override suspend fun getExchangeInfo() = MexcExchangeInfoResponse(emptyList())
        override suspend fun getAllPrices() = listOf(
            MexcTickerPriceDto("ETHUSDT", "2000.0"),
            MexcTickerPriceDto("SOLUSDT", "150.0"),
        )
        override suspend fun placeOrder(order: MexcOrderRequest, timestamp: Long, signature: String): Any = Unit
    }

    private val fakeCmcService = object : CmcApiService {
        override suspend fun getListings(apiKey: String, limit: Int, sort: String) =
            CmcListingsResponse(listOf(CmcCoinDto("BTC"), CmcCoinDto("ETH"), CmcCoinDto("SOL")))
    }

    @Test
    fun missingKeys_returnsFailure() = runTest {
        val useCase = UpdatePortfolioUseCase(
            checkSettings = CheckSettingsUseCase(emptySettingsRepo),
            settingsRepo = emptySettingsRepo,
            portfolioRepo = fakePortfolioRepo,
            mexcService = fakeMexcService,
            cmcService = fakeCmcService,
        )
        val result = useCase()
        assertTrue(result.isFailure)
    }

    @Test
    fun happyPath_savesCoins() = runTest {
        val useCase = UpdatePortfolioUseCase(
            checkSettings = CheckSettingsUseCase(validSettingsRepo),
            settingsRepo = validSettingsRepo,
            portfolioRepo = fakePortfolioRepo,
            mexcService = fakeMexcService,
            cmcService = fakeCmcService,
        )
        val result = useCase()
        assertTrue(result.isSuccess)
        assertTrue(savedCoins != null)
        // ETH has 1.5 qty, price 2000 = $3000 position > threshold
        // USDT is excluded
        // SOL has 0 qty so no position
        assertTrue(savedCoins!!.any { it.symbol == "ETH" })
        assertTrue(savedCoins!!.none { it.symbol == "USDT" })
    }
}
