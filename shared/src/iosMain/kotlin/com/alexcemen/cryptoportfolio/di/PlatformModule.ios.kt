package com.alexcemen.cryptoportfolio.di

import com.alexcemen.cryptoportfolio.data.db.AppDatabase
import com.alexcemen.cryptoportfolio.platform.PlatformContext
import com.alexcemen.cryptoportfolio.platform.SecureStorage
import com.alexcemen.cryptoportfolio.platform.getDatabaseBuilder
import org.koin.dsl.module

val iosModule = module {
    single { SecureStorage() }
    single {
        getDatabaseBuilder(PlatformContext())
            .build()
    }
}
