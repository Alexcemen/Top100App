package com.alexcemen.cryptoportfolio.di

import com.alexcemen.cryptoportfolio.data.network.CmcApiService
import com.alexcemen.cryptoportfolio.data.network.MexcApiService
import com.alexcemen.cryptoportfolio.data.network.createCmcHttpClient
import com.alexcemen.cryptoportfolio.data.network.createMexcHttpClient
import com.alexcemen.cryptoportfolio.data.repository.CmcRepositoryImpl
import com.alexcemen.cryptoportfolio.data.repository.MexcRepositoryImpl
import com.alexcemen.cryptoportfolio.data.repository.PortfolioRepositoryImpl
import com.alexcemen.cryptoportfolio.data.repository.SettingsRepositoryImpl
import com.alexcemen.cryptoportfolio.domain.repository.CmcRepository
import com.alexcemen.cryptoportfolio.domain.repository.MexcRepository
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import com.alexcemen.cryptoportfolio.domain.usecase.CheckSettingsUseCase
import com.alexcemen.cryptoportfolio.domain.usecase.GetPortfolioUseCase
import com.alexcemen.cryptoportfolio.domain.usecase.GetSettingsUseCase
import com.alexcemen.cryptoportfolio.domain.usecase.RebalancerUseCase
import com.alexcemen.cryptoportfolio.domain.usecase.SaveSettingsUseCase
import com.alexcemen.cryptoportfolio.domain.usecase.SellUseCase
import com.alexcemen.cryptoportfolio.domain.usecase.UpdatePortfolioUseCase
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioReducer
import com.alexcemen.cryptoportfolio.ui.screen.portfolio.PortfolioScreenModel
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsReducer
import com.alexcemen.cryptoportfolio.ui.screen.settings.SettingsScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val commonModule = module {
    // Network
    single(named("cmc")) { createCmcHttpClient() }
    single(named("mexc")) { createMexcHttpClient() }
    single { CmcApiService(get(named("cmc"))) }
    single {
        MexcApiService(
            client = get(named("mexc")),
            apiKeyProvider = { get<SettingsRepository>().getSettings().mexcApiKey },
        )
    }

    // Database
    single { get<com.alexcemen.cryptoportfolio.data.db.AppDatabase>().portfolioDao() }

    // Repositories
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class
    singleOf(::PortfolioRepositoryImpl) bind PortfolioRepository::class
    singleOf(::CmcRepositoryImpl) bind CmcRepository::class
    singleOf(::MexcRepositoryImpl) bind MexcRepository::class

    // Use cases
    factoryOf(::CheckSettingsUseCase)
    factoryOf(::GetPortfolioUseCase)
    factoryOf(::GetSettingsUseCase)
    factoryOf(::SaveSettingsUseCase)
    factoryOf(::UpdatePortfolioUseCase)
    factoryOf(::SellUseCase)
    factoryOf(::RebalancerUseCase)

    // Reducers
    factoryOf(::PortfolioReducer)
    factoryOf(::SettingsReducer)

    // ScreenModels
    factoryOf(::PortfolioScreenModel)
    factoryOf(::SettingsScreenModel)
}
