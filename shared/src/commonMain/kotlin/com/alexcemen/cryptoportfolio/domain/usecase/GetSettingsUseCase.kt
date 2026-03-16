package com.alexcemen.cryptoportfolio.domain.usecase

import com.alexcemen.cryptoportfolio.domain.repository.SettingsRepository

class GetSettingsUseCase constructor (
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() = settingsRepository.getSettings()
}
