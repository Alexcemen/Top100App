package com.alexcemen.cryptoportfolio.data.repository

import com.alexcemen.cryptoportfolio.data.network.CmcApiService
import com.alexcemen.cryptoportfolio.domain.repository.CmcRepository

class CmcRepositoryImpl(
    private val cmcService: CmcApiService,
) : CmcRepository {
    override suspend fun getTopCoins(apiKey: String, limit: Int): List<String> =
        cmcService.getListings(apiKey, limit).data.map { it.symbol }

    override suspend fun getCoinIds(apiKey: String, limit: Int): Map<String, Int> =
        cmcService.getListings(apiKey, limit).data.associate { it.symbol to it.id }
}
