package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.data.network.MexcApiService
import com.alexcemen.cryptoportfolio.data.network.dto.MexcAccountResponse
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

    private val ordersPlaced = mutableListOf<MexcOrderRequest>()
    private val fakeMexcService = object : MexcApiService {
        override suspend fun getAccount(timestamp: Long, signature: String) = MexcAccountResponse(emptyList())
        override suspend fun getExchangeInfo() = MexcExchangeInfoResponse(emptyList())
        override suspend fun getAllPrices(): List<MexcTickerPriceDto> = emptyList()
        override suspend fun placeOrder(order: MexcOrderRequest, timestamp: Long, signature: String): Any {
            ordersPlaced.add(order)
            return Unit
        }
    }

    @Test
    fun missingKeys_returnsFailure() = runTest {
        val useCase = SellUseCase(
            checkSettings = CheckSettingsUseCase(emptySettingsRepo),
            settingsRepo = emptySettingsRepo,
            portfolioRepo = fakePortfolioRepo,
            mexcService = fakeMexcService,
        )
        assertTrue(useCase(500.0).isFailure)
    }

    @Test
    fun zeroAmount_returnsFailure() = runTest {
        val useCase = SellUseCase(
            checkSettings = CheckSettingsUseCase(validSettingsRepo),
            settingsRepo = validSettingsRepo,
            portfolioRepo = fakePortfolioRepo,
            mexcService = fakeMexcService,
        )
        assertTrue(useCase(0.0).isFailure)
    }

    @Test
    fun happyPath_placesProportionalOrders() = runTest {
        val useCase = SellUseCase(
            checkSettings = CheckSettingsUseCase(validSettingsRepo),
            settingsRepo = validSettingsRepo,
            portfolioRepo = fakePortfolioRepo,
            mexcService = fakeMexcService,
        )
        val result = useCase(900.0)  // 10% of $9000
        assertTrue(result.isSuccess)
        // Should place 2 orders (one per coin)
        assertTrue(ordersPlaced.size == 2)
        assertTrue(ordersPlaced.all { it.side == "SELL" })
        assertTrue(ordersPlaced.all { it.type == "MARKET" })
    }
}
