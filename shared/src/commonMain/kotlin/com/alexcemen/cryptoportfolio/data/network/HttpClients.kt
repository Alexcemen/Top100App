package com.alexcemen.cryptoportfolio.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect fun cmcBaseUrl(): String
expect fun mexcBaseUrl(): String

fun createCmcHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(Logging) { level = LogLevel.INFO }
    defaultRequest { url(cmcBaseUrl()) }
}

fun createMexcHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(Logging) { level = LogLevel.INFO }
    defaultRequest { url(mexcBaseUrl()) }
}
