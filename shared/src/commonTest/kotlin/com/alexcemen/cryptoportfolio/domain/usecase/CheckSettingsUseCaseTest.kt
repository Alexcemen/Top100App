package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.model.SettingsData
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckSettingsUseCaseTest {
    private fun makeRepo(settings: SettingsData): SettingsRepository = object : SettingsRepository {
        override suspend fun getSettings() = settings
        override suspend fun saveSettings(settings: SettingsData) {}
    }

    @Test
    fun allKeysPresent_returnsTrue() = runTest {
        val useCase = CheckSettingsUseCase(makeRepo(SettingsData(
            cmcApiKey = "key1", mexcApiKey = "key2", mexcApiSecret = "secret"
        )))
        assertTrue(useCase())
    }

    @Test
    fun cmcKeyEmpty_returnsFalse() = runTest {
        val useCase = CheckSettingsUseCase(makeRepo(SettingsData(
            cmcApiKey = "", mexcApiKey = "key2", mexcApiSecret = "secret"
        )))
        assertFalse(useCase())
    }

    @Test
    fun mexcKeyEmpty_returnsFalse() = runTest {
        val useCase = CheckSettingsUseCase(makeRepo(SettingsData(
            cmcApiKey = "key1", mexcApiKey = "", mexcApiSecret = "secret"
        )))
        assertFalse(useCase())
    }
}
