package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import javax.inject.Inject

class GetPortfolioUseCase @Inject constructor(
    private val portfolioRepository: PortfolioRepository
) {
    operator fun invoke() = portfolioRepository.getPortfolio()
}
