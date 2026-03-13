package com.alexcemen.cryptoportfolio.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.alexcemen.cryptoportfolio.domain.model.SettingsData
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val PREFS_FILE = "crypto_secure_prefs"
private const val KEY_CMC = "cmc_api_key"
private const val KEY_MEXC_KEY = "mexc_api_key"
private const val KEY_MEXC_SECRET = "mexc_api_secret"
private const val KEY_TOP_LIMIT = "top_coins_limit"
private const val KEY_EXCLUDED = "excluded_coins"

class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val prefs by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            PREFS_FILE,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override suspend fun getSettings(): SettingsData = withContext(Dispatchers.IO) {
        SettingsData(
            cmcApiKey = prefs.getString(KEY_CMC, "") ?: "",
            mexcApiKey = prefs.getString(KEY_MEXC_KEY, "") ?: "",
            mexcApiSecret = prefs.getString(KEY_MEXC_SECRET, "") ?: "",
            topCoinsLimit = prefs.getInt(KEY_TOP_LIMIT, 20),
            excludedCoins = prefs.getString(KEY_EXCLUDED, "USDT,USDC,BUSD")
                ?.split(",")?.filter { it.isNotBlank() } ?: listOf("USDT", "USDC", "BUSD"),
        )
    }

    override suspend fun saveSettings(settings: SettingsData) = withContext(Dispatchers.IO) {
        prefs.edit()
            .putString(KEY_CMC, settings.cmcApiKey)
            .putString(KEY_MEXC_KEY, settings.mexcApiKey)
            .putString(KEY_MEXC_SECRET, settings.mexcApiSecret)
            .putInt(KEY_TOP_LIMIT, settings.topCoinsLimit)
            .putString(KEY_EXCLUDED, settings.excludedCoins.joinToString(","))
            .apply()
    }
}
