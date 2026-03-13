package com.alexcemen.cryptoportfolio.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alexcemen.cryptoportfolio.data.db.entity.CoinEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PortfolioDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: PortfolioDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), AppDatabase::class.java
        ).build()
        dao = db.portfolioDao()
    }

    @After
    fun teardown() { db.close() }

    @Test
    fun insertAndGetCoins() = runTest {
        val coins = listOf(
            CoinEntity("ETH", 3000.0, 0.5),
            CoinEntity("SOL", 150.0, 10.0),
        )
        dao.replaceAll(coins)
        val result = dao.getAll().first()
        assertEquals(2, result.size)
        assertEquals("ETH", result.find { it.symbol == "ETH" }?.symbol)
    }

    @Test
    fun replaceAllClearsPrevious() = runTest {
        dao.replaceAll(listOf(CoinEntity("BTC", 60000.0, 0.1)))
        dao.replaceAll(listOf(CoinEntity("ETH", 3000.0, 1.0)))
        val result = dao.getAll().first()
        assertEquals(1, result.size)
        assertEquals("ETH", result[0].symbol)
    }
}
