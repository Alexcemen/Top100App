package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.data.network.CmcApiService
import com.alexcemen.cryptoportfolio.data.network.MexcApiService
import javax.inject.Inject

class RebalancerUseCase @Inject constructor(
    private val checkSettings: CheckSettingsUseCase,
    private val settingsRepo: com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository,
    private val portfolioRepo: com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository,
    private val mexcService: MexcApiService,
    private val cmcService: CmcApiService,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        throw NotImplementedError("RebalancerUseCase not yet implemented")
    }
}
