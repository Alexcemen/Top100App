package com.alexcemen.cryptoportfolio.di

import com.alexcemen.cryptoportfolio.data.repository.WebPortfolioRepositoryImpl
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import com.alexcemen.cryptoportfolio.platform.SecureStorage
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val webModule = module {
    single { SecureStorage() }
    singleOf(::WebPortfolioRepositoryImpl) bind PortfolioRepository::class
}
