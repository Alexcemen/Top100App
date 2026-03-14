package com.alexcemen.cryptoportfolio.di

import com.alexcemen.cryptoportfolio.data.repository.CmcRepositoryImpl
import com.alexcemen.cryptoportfolio.data.repository.MexcRepositoryImpl
import com.alexcemen.cryptoportfolio.data.repository.PortfolioRepositoryImpl
import com.alexcemen.cryptoportfolio.data.repository.SettingsRepositoryImpl
import com.alexcemen.cryptoportfolio.domain.repository.CmcRepository
import com.alexcemen.cryptoportfolio.domain.repository.MexcRepository
import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindPortfolioRepository(impl: PortfolioRepositoryImpl): PortfolioRepository
    @Binds abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
    @Binds abstract fun bindMexcRepository(impl: MexcRepositoryImpl): MexcRepository
    @Binds abstract fun bindCmcRepository(impl: CmcRepositoryImpl): CmcRepository
}
