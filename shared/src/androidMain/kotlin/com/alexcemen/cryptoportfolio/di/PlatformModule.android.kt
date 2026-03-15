package com.alexcemen.cryptoportfolio.di

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alexcemen.cryptoportfolio.data.db.AppDatabase
import com.alexcemen.cryptoportfolio.platform.PlatformContext
import com.alexcemen.cryptoportfolio.platform.SecureStorage
import com.alexcemen.cryptoportfolio.platform.getDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE portfolio_table ADD COLUMN cmcId INTEGER")
    }
}

val androidModule = module {
    single { SecureStorage(androidContext()) }
    single {
        getDatabaseBuilder(PlatformContext(androidContext()))
            .addMigrations(MIGRATION_1_2)
            .build()
    }
}
