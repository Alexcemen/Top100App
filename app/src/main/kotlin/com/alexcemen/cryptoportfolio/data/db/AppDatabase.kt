package com.alexcemen.cryptoportfolio.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alexcemen.cryptoportfolio.data.db.entity.CoinEntity

@Database(
    entities = [CoinEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun portfolioDao(): PortfolioDao
}
