package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository

class CheckSettingsUseCase constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): Boolean {
        val settingsData = settingsRepository.getSettings()
        return settingsData.cmcApiKey.isNotBlank()
                && settingsData.mexcApiKey.isNotBlank()
                && settingsData.mexcApiSecret.isNotBlank()
    }
}
