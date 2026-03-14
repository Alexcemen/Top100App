package com.alexcemen.cryptoportfolio.domain.repository

interface CmcRepository {
    /** Returns top coin symbols ordered by market cap. */
    suspend fun getTopCoins(apiKey: String, limit: Int): List<String>
}
