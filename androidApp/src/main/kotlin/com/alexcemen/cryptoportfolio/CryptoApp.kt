package com.alexcemen.cryptoportfolio

import android.app.Application
import com.alexcemen.cryptoportfolio.di.androidModule
import com.alexcemen.cryptoportfolio.di.commonModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CryptoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CryptoApp)
            modules(commonModule, androidModule)
        }
    }
}
