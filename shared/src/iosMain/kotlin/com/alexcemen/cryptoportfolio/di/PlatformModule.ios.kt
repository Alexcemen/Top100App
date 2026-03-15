package com.alexcemen.cryptoportfolio.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.alexcemen.cryptoportfolio.data.db.AppDatabase
import com.alexcemen.cryptoportfolio.platform.PlatformContext
import com.alexcemen.cryptoportfolio.platform.SecureStorage
import com.alexcemen.cryptoportfolio.platform.getDatabaseBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.module

val iosModule = module {
    single { SecureStorage() }
    single {
        getDatabaseBuilder(PlatformContext())
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}
