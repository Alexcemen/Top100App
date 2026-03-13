package com.alexcemen.cryptoportfolio.data.network

import com.alexcemen.cryptoportfolio.data.network.dto.MexcAccountResponse
import com.alexcemen.cryptoportfolio.data.network.dto.MexcExchangeInfoResponse
import com.alexcemen.cryptoportfolio.data.network.dto.MexcOrderRequest
import com.alexcemen.cryptoportfolio.data.network.dto.MexcTickerPriceDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MexcApiService {
    @GET("api/v3/account")
    suspend fun getAccount(
        @Query("timestamp") timestamp: Long,
        @Query("signature") signature: String,
    ): MexcAccountResponse

    @GET("api/v3/exchangeInfo")
    suspend fun getExchangeInfo(): MexcExchangeInfoResponse

    @GET("api/v3/ticker/price")
    suspend fun getAllPrices(): List<MexcTickerPriceDto>

    @POST("api/v3/order")
    suspend fun placeOrder(
        @Body order: MexcOrderRequest,
        @Query("timestamp") timestamp: Long,
        @Query("signature") signature: String,
    ): Any
}
