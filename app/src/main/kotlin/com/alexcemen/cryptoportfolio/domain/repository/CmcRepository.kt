package com.alexcemen.cryptoportfolio.domain.repository

interface CmcRepository {
    /** Returns top coin symbols ordered by market cap. */
    suspend fun getTopCoins(apiKey: String, limit: Int): List<String>

    /** Returns symbol → CMC id map for the top [limit] coins by market cap. */
    suspend fun getCoinIds(apiKey: String, limit: Int): Map<String, Int>
}
