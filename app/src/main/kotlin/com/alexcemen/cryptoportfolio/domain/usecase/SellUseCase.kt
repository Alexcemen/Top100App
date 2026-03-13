package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.data.network.MexcApiService
import javax.inject.Inject

class SellUseCase @Inject constructor(
    private val checkSettings: CheckSettingsUseCase,
    private val settingsRepo: com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository,
    private val portfolioRepo: com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository,
    private val mexcService: MexcApiService,
) {
    suspend operator fun invoke(usdtAmount: Double): Result<Unit> = runCatching {
        throw NotImplementedError("SellUseCase not yet implemented")
    }
}
