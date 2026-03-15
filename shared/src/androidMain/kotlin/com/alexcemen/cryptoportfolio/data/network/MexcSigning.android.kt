package com.alexcemen.cryptoportfolio.data.network

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

actual fun signMexcQuery(query: String, secret: String): String {
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(secret.toByteArray(), "HmacSHA256"))
    return mac.doFinal(query.toByteArray()).joinToString("") { "%02x".format(it) }
}
