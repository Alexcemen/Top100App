package com.alexcemen.cryptoportfolio.platform

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alexcemen.cryptoportfolio.data.db.AppDatabase

actual fun getDatabaseBuilder(context: PlatformContext): RoomDatabase.Builder<AppDatabase> =
    Room.databaseBuilder(context, AppDatabase::class.java, "crypto_portfolio.db")
