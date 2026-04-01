package com.alexcemen.cryptoportfolio.platform

import kotlinx.browser.localStorage

actual class SecureStorage {
    actual fun getString(key: String, default: String): String =
        localStorage.getItem(key) ?: default

    actual fun getInt(key: String, default: Int): Int =
        localStorage.getItem(key)?.toIntOrNull() ?: default

    actual fun putString(key: String, value: String) {
        localStorage.setItem(key, value)
    }

    actual fun putInt(key: String, value: Int) {
        localStorage.setItem(key, value.toString())
    }
}
