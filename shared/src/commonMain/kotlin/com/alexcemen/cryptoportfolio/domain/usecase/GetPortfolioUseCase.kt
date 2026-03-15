package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository

class GetPortfolioUseCase constructor(
    private val portfolioRepository: PortfolioRepository
) {
    operator fun invoke() = portfolioRepository.getPortfolio()
}
