package com.alexcemen.cryptoportfolio.data.network

import com.alexcemen.cryptoportfolio.data.network.dto.CmcListingsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter

class CmcApiService(private val client: HttpClient) {
    suspend fun getListings(apiKey: String, limit: Int, sort: String = "market_cap"): CmcListingsResponse {
        return client.get("v1/cryptocurrency/listings/latest") {
            header("X-CMC_PRO_API_KEY", apiKey)
            parameter("limit", limit)
            parameter("sort", sort)
        }.body()
    }
}
