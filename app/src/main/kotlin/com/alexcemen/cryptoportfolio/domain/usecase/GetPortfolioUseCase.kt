package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.repository.PortfolioRepository
import javax.inject.Inject

class GetPortfolioUseCase @Inject constructor(private val repo: PortfolioRepository) {
    operator fun invoke() = repo.getPortfolio()
}
