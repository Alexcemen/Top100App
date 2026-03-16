package com.alexcemen.cryptoportfolio.di

import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(commonModule, iosModule)
    }
}
