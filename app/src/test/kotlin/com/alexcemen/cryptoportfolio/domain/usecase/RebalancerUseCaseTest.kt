package com.alexcemen.cryptoportfolio.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RebalancerUseCaseTest {

    @Test
    fun buildAvailableCoins_excludesStablecoins() {
        val top = listOf("BTC", "ETH", "USDT", "SOL")
        val tradable = setOf("BTC", "ETH", "USDT", "SOL")
        val excluded = setOf("USDT", "USDC")
        val result = buildAvailableCoins(top, tradable, excluded)
        assertFalse("USDT" in result)
        assertTrue("BTC" in result)
        assertTrue("ETH" in result)
        assertTrue("SOL" in result)
    }

    @Test
    fun buildAvailableCoins_preservesOrder() {
        val top = listOf("C", "A", "B")
        val tradable = setOf("A", "B", "C")
        val excluded = emptySet<String>()
        val result = buildAvailableCoins(top, tradable, excluded)
        assertEquals(listOf("C", "A", "B"), result)
    }

    @Test
    fun buildCoinsToSell_returnsCoinsNotInAvailable() {
        val mine = setOf("BTC", "ETH", "DOGE")
        val available = listOf("BTC", "ETH", "SOL")
        val result = buildCoinsToSell(mine, available)
        assertTrue("DOGE" in result)
        assertFalse("BTC" in result)
        assertFalse("ETH" in result)
    }

    @Test
    fun buildCoinsToSell_excludedCoinsNotSold() {
        val mine = setOf("BTC", "USDT", "DOGE")
        val available = listOf("BTC", "ETH")
        val result = buildCoinsToSell(mine, available)
        assertFalse("USDT" in result)
        assertTrue("DOGE" in result)
    }
}
