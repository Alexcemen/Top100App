package com.alexcemen.cryptoportfolio.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.alexcemen.cryptoportfolio.data.db.entity.CoinEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolio_table")
    fun getAll(): Flow<List<CoinEntity>>

    @Query("DELETE FROM portfolio_table")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(coins: List<CoinEntity>)

    @Transaction
    suspend fun replaceAll(coins: List<CoinEntity>) {
        deleteAll()
        insertAll(coins)
    }
}
