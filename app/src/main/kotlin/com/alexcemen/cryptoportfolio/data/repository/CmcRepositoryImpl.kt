package com.alexcemen.cryptoportfolio.data.repository

import com.alexcemen.cryptoportfolio.data.network.CmcApiService
import com.alexcemen.cryptoportfolio.domain.repository.CmcRepository
import javax.inject.Inject

class CmcRepositoryImpl @Inject constructor(
    private val cmcService: CmcApiService,
) : CmcRepository {
    override suspend fun getTopCoins(apiKey: String, limit: Int): List<String> =
        cmcService.getListings(apiKey, limit).data.map { it.symbol }
}
