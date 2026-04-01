package com.alexcemen.cryptoportfolio.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.alexcemen.cryptoportfolio.data.db.entity.CoinEntity

private const val DATABASE_VERSION = 2

@Database(
    entities = [CoinEntity::class],
    version = DATABASE_VERSION
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun portfolioDao(): PortfolioDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
