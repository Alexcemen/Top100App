package com.alexcemen.cryptoportfolio.data.repository

import com.alexcemen.cryptoportfolio.domain.model.SettingsData
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import com.alexcemen.cryptoportfolio.platform.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val KEY_CMC = "cmc_api_key"
private const val KEY_MEXC_KEY = "mexc_api_key"
private const val KEY_MEXC_SECRET = "mexc_api_secret"
private const val KEY_TOP_LIMIT = "top_coins_limit"
private const val KEY_EXCLUDED = "excluded_coins"

class SettingsRepositoryImpl(
    private val secureStorage: SecureStorage,
) : SettingsRepository {

    override suspend fun getSettings(): SettingsData = withContext(Dispatchers.Default) {
        SettingsData(
            cmcApiKey = secureStorage.getString(KEY_CMC, ""),
            mexcApiKey = secureStorage.getString(KEY_MEXC_KEY, ""),
            mexcApiSecret = secureStorage.getString(KEY_MEXC_SECRET, ""),
            topCoinsLimit = secureStorage.getInt(KEY_TOP_LIMIT, 100),
            excludedCoins = secureStorage.getString(KEY_EXCLUDED, "FDUSD,USD1,PYUSD,USDC,DAI,USDe")
                .split(",").filter { it.isNotBlank() },
        )
    }

    override suspend fun saveSettings(settings: SettingsData) = withContext(Dispatchers.Default) {
        secureStorage.putString(KEY_CMC, settings.cmcApiKey)
        secureStorage.putString(KEY_MEXC_KEY, settings.mexcApiKey)
        secureStorage.putString(KEY_MEXC_SECRET, settings.mexcApiSecret)
        secureStorage.putInt(KEY_TOP_LIMIT, settings.topCoinsLimit)
        secureStorage.putString(KEY_EXCLUDED, settings.excludedCoins.joinToString(","))
    }
}
