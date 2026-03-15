package com.alexcemen.cryptoportfolio.ui.screen.portfolio

import com.alexcemen.cryptoportfolio.domain.model.CoinData
import com.alexcemen.cryptoportfolio.domain.model.PortfolioData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PortfolioReducerTest {
    private val reducer = PortfolioReducer()

    @Test
    fun emptyPortfolio_mapsCorrectly() {
        val state = PortfolioStore.State(portfolio = PortfolioData(emptyList(), 0.0), isLoading = false)
        val ui = reducer.reduce(state)
        assertTrue(ui.coins.isEmpty())
        assertEquals("$0.00", ui.totalUsdt)
        assertFalse(ui.isLoading)
    }

    @Test
    fun loadingState_reflected() {
        val state = PortfolioStore.State(isLoading = true)
        assertTrue(reducer.reduce(state).isLoading)
    }

    @Test
    fun coinMapping_formatsCorrectly() {
        val coin = CoinData(symbol = "ETH", priceUsdt = 2000.5, quantity = 1.5)
        val state = PortfolioStore.State(
            portfolio = PortfolioData(listOf(coin), coin.totalPositionUsdt),
            isLoading = false
        )
        val ui = reducer.reduce(state)
        assertEquals(1, ui.coins.size)
        assertEquals("ETH", ui.coins[0].symbol)
        assertTrue(ui.coins[0].priceUsdt.contains("2000"))
        assertTrue(ui.coins[0].quantity.contains("1.5"))
    }

    @Test
    fun logoUrl_mappedThroughToCoinUi() {
        val coin = CoinData(
            symbol = "ETH",
            priceUsdt = 2000.0,
            quantity = 1.0,
            logoUrl = "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
        )
        val state = PortfolioStore.State(
            portfolio = PortfolioData(listOf(coin), coin.totalPositionUsdt),
        )
        val ui = reducer.reduce(state)
        assertEquals(
            "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png",
            ui.coins[0].logoUrl
        )
    }

    @Test
    fun avatarColorIndex_isInValidRange() {
        val coins = listOf("BTC", "ETH", "SOL", "DOGE", "ADA").map { symbol ->
            CoinData(symbol = symbol, priceUsdt = 1.0, quantity = 1.0)
        }
        val state = PortfolioStore.State(portfolio = PortfolioData(coins, 5.0))
        val ui = reducer.reduce(state)
        ui.coins.forEach { coin ->
            assertTrue("avatarColorIndex must be in [0, 6]", coin.avatarColorIndex in 0..6)
        }
    }

    @Test
    fun totalUsdt_formattedAsCurrency() {
        val state = PortfolioStore.State(portfolio = PortfolioData(emptyList(), 18916.43))
        val ui = reducer.reduce(state)
        assertEquals("$18,916.43", ui.totalUsdt)
    }

    @Test
    fun logoUrl_nullWhenNotSet() {
        val coin = CoinData(symbol = "BTC", priceUsdt = 60000.0, quantity = 0.1)
        val state = PortfolioStore.State(
            portfolio = PortfolioData(listOf(coin), coin.totalPositionUsdt),
        )
        val ui = reducer.reduce(state)
        assertNull(ui.coins[0].logoUrl)
    }
}
