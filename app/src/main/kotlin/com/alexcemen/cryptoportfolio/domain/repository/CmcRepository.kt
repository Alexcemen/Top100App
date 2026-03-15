package com.alexcemen.cryptoportfolio.domain.repository

interface CmcRepository {
    suspend fun getTopCoins(apiKey: String, limit: Int): List<String>

    suspend fun getCoinIds(apiKey: String, limit: Int): Map<String, Int>
}
