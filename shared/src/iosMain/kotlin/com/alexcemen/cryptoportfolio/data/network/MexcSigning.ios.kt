package com.alexcemen.cryptoportfolio.data.network

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CCHmac
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.CoreCrypto.kCCHmacAlgSHA256

@OptIn(ExperimentalForeignApi::class)
actual fun signMexcQuery(query: String, secret: String): String {
    val keyBytes = secret.encodeToByteArray()
    val dataBytes = query.encodeToByteArray()
    val digestLen = CC_SHA256_DIGEST_LENGTH

    return memScoped {
        val result = allocArray<kotlinx.cinterop.UByteVar>(digestLen)
        keyBytes.usePinned { keyPinned ->
            dataBytes.usePinned { dataPinned ->
                CCHmac(
                    kCCHmacAlgSHA256,
                    keyPinned.addressOf(0),
                    keyBytes.size.convert(),
                    dataPinned.addressOf(0),
                    dataBytes.size.convert(),
                    result,
                )
            }
        }
        (0 until digestLen).joinToString("") {
            result[it].toInt().and(0xFF).toString(16).padStart(2, '0')
        }
    }
}
