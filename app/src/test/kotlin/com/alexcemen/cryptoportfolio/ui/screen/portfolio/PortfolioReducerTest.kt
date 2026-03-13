package com.alexcemen.cryptoportfolio.ui.screen.portfolio

import com.alexcemen.cryptoportfolio.domain.model.CoinData
import com.alexcemen.cryptoportfolio.domain.model.PortfolioData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PortfolioReducerTest {
    private val reducer = PortfolioReducer()

    @Test
    fun emptyPortfolio_mapsCorrectly() {
        val state = PortfolioStore.State(portfolio = PortfolioData(emptyList(), 0.0), isLoading = false)
        val ui = reducer.reduce(state)
        assertTrue(ui.coins.isEmpty())
        assertEquals(0.0, ui.totalUsdt, 0.001)
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
}
