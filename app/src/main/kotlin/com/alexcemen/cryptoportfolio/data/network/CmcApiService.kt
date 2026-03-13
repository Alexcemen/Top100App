package com.alexcemen.cryptoportfolio.data.network

import com.alexcemen.cryptoportfolio.data.network.dto.CmcListingsResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface CmcApiService {
    @GET("v1/cryptocurrency/listings/latest")
    suspend fun getListings(
        @Header("X-CMC_PRO_API_KEY") apiKey: String,
        @Query("limit") limit: Int,
        @Query("sort") sort: String = "market_cap",
    ): CmcListingsResponse
}
