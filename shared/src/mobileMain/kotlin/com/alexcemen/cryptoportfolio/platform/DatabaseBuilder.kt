package com.alexcemen.cryptoportfolio.platform

import androidx.room.RoomDatabase
import com.alexcemen.cryptoportfolio.data.db.AppDatabase

expect fun getDatabaseBuilder(context: PlatformContext): RoomDatabase.Builder<AppDatabase>
