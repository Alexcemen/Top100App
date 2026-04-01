package com.alexcemen.cryptoportfolio.platform

import androidx.room.RoomDatabase
import com.alexcemen.cryptoportfolio.data.db.AppDatabase

actual fun getDatabaseBuilder(context: PlatformContext): RoomDatabase.Builder<AppDatabase> {
    error("Room is not supported on web. Use WebPortfolioRepositoryImpl instead.")
}
