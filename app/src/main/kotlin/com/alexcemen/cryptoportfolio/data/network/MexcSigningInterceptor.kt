package com.alexcemen.cryptoportfolio.data.network

import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class MexcSigningInterceptor @Inject constructor(
    private val settingsRepository: SettingsRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val settings = runBlocking { settingsRepository.getSettings() }
        val original = chain.request()

        val request = original.newBuilder()
            .addHeader("X-MEXC-APIKEY", settings.mexcApiKey)
            .build()

        return chain.proceed(request)
    }

    fun sign(queryString: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
        return mac.doFinal(queryString.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
