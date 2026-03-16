package com.alexcemen.cryptoportfolio.platform

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

actual class SecureStorage(context: Context) {
    private val prefs: SharedPreferences by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "crypto_secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    actual fun getString(key: String, default: String): String =
        prefs.getString(key, default) ?: default

    actual fun getInt(key: String, default: Int): Int =
        prefs.getInt(key, default)

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }
}
