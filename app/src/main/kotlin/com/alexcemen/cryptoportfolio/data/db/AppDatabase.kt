package com.alexcemen.cryptoportfolio.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alexcemen.cryptoportfolio.data.db.entity.CoinEntity

private const val DATABASE_VERSION = 2

@Database(
    entities = [CoinEntity::class],
    version = DATABASE_VERSION
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun portfolioDao(): PortfolioDao
}
