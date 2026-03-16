package com.alexcemen.cryptoportfolio.data.network

import com.alexcemen.cryptoportfolio.data.network.dto.MexcAccountResponse
import com.alexcemen.cryptoportfolio.data.network.dto.MexcExchangeInfoResponse
import com.alexcemen.cryptoportfolio.data.network.dto.MexcTickerPriceDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import kotlinx.serialization.json.JsonObject

class MexcApiService(
    private val client: HttpClient,
    private val apiKeyProvider: suspend () -> String,
) {
    suspend fun getAccount(timestamp: Long, signature: String): MexcAccountResponse {
        return client.get("api/v3/account") {
            header("X-MEXC-APIKEY", apiKeyProvider())
            parameter("timestamp", timestamp)
            parameter("signature", signature)
        }.body()
    }

    suspend fun getExchangeInfo(): MexcExchangeInfoResponse {
        return client.get("api/v3/exchangeInfo").body()
    }

    suspend fun getAllPrices(): List<MexcTickerPriceDto> {
        return client.get("api/v3/ticker/price").body()
    }

    suspend fun placeOrder(
        symbol: String, side: OrderSide, type: String,
        quoteOrderQty: String, timestamp: Long, signature: String,
    ): JsonObject {
        return client.post("api/v3/order") {
            header("X-MEXC-APIKEY", apiKeyProvider())
            parameter("symbol", symbol)
            parameter("side", side.name)
            parameter("type", type)
            parameter("quoteOrderQty", quoteOrderQty)
            parameter("timestamp", timestamp)
            parameter("signature", signature)
        }.body()
    }

    suspend fun placeOrderByQty(
        symbol: String, side: OrderSide, type: String,
        quantity: String, timestamp: Long, signature: String,
    ): JsonObject {
        return client.post("api/v3/order") {
            header("X-MEXC-APIKEY", apiKeyProvider())
            parameter("symbol", symbol)
            parameter("side", side.name)
            parameter("type", type)
            parameter("quantity", quantity)
            parameter("timestamp", timestamp)
            parameter("signature", signature)
        }.body()
    }
}
