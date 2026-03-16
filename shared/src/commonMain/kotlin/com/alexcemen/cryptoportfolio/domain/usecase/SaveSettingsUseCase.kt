package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.model.SettingsData
import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository

class SaveSettingsUseCase constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(settings: SettingsData) = settingsRepository.saveSettings(settings)
}
